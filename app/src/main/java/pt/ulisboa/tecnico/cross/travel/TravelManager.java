package pt.ulisboa.tecnico.cross.travel;

import static androidx.work.BackoffPolicy.LINEAR;
import static androidx.work.WorkRequest.MIN_BACKOFF_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.account.KeyValueDataManager;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.api.exceptions.APIException;
import pt.ulisboa.tecnico.cross.api.trip.domain.TripSubmissionResponse;
import pt.ulisboa.tecnico.cross.model.peerendorsement.PeerEndorsement;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.model.trip.Trip;
import pt.ulisboa.tecnico.cross.model.visit.Visit;
import pt.ulisboa.tecnico.cross.model.wifiapevidence.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import timber.log.Timber;

public class TravelManager {

  private final MutableLiveData<Boolean> update;

  public static TravelManager get() {
    return TravelManagerHolder.INSTANCE;
  }

  public TravelManager() {
    this.update = new MutableLiveData<>(null);
  }

  public LiveData<Boolean> newUpdate() {
    update.setValue(null);
    return update;
  }

  public TripSubmissionResponse submitTrip(
      Trip trip,
      List<Visit> visits,
      Map<String, List<WiFiAPEvidence>> wiFiAPEvidencesGroupedByVisitId,
      Map<String, List<PeerEndorsement>> peerEndorsementsGroupedByVisitId)
      throws IOException {
    TripSubmissionResponse response = null;
    APIException apiException = null;
    try {
      response =
          APIManager.get()
              .getTripAPI()
              .createOrUpdateTrip(
                  trip, visits, wiFiAPEvidencesGroupedByVisitId, peerEndorsementsGroupedByVisitId);

      trip.completed = response.completedTrip;
      updateScoreboard();
      updateGems(response.awardedGems);
      updateOwnedBadges(response.awardedBadges);
    } catch (APIException e) {
      apiException = e;
    }

    for (Visit visit : visits) {
      visit.verified =
          response != null
              && response.visitVerificationStatus.get(visit.id) == R.string.visit_verified;
      visit.rejected = !visit.verified;
    }

    WiFiAPEvidence[] wiFiAPEvidences =
        wiFiAPEvidencesGroupedByVisitId.values().stream()
            .flatMap(Collection::stream)
            .toArray(WiFiAPEvidence[]::new);
    PeerEndorsement[] peerEndorsements =
        peerEndorsementsGroupedByVisitId.values().stream()
            .flatMap(Collection::stream)
            .toArray(PeerEndorsement[]::new);

    CROSSCityApp.get()
        .db()
        .runInTransaction(
            () -> {
              if (trip.completed) CROSSCityApp.get().db().tripDao().updateAll(trip);
              CROSSCityApp.get().db().visitDao().updateAll(visits.toArray(new Visit[0]));
              CROSSCityApp.get().db().wiFiAPEvidenceDao().deleteAll(wiFiAPEvidences);
              CROSSCityApp.get().db().peerEndorsementDao().deleteAll(peerEndorsements);
            });

    if (apiException != null) throw apiException;
    return response;
  }

  public static void updateScoreboard() {
    CROSSViewModel viewModel = CROSSCityApp.get().getViewModel();
    if (viewModel != null) new Thread(viewModel::updateScoreboard).start();
  }

  public static void updateGems(int awardedGems) {
    int gems = KeyValueDataManager.get().getInt("gems", 0) + awardedGems;
    KeyValueDataManager.get().edit().putInt("gems", gems).apply();

    CROSSViewModel viewModel = CROSSCityApp.get().getViewModel();
    if (viewModel != null) viewModel.updateGems(gems);
  }

  public static void updateOwnedBadges(List<String> awardedBadges) {
    Set<String> ownedBadges = new HashSet<>(awardedBadges);
    ownedBadges.addAll(KeyValueDataManager.get().getStringSet("ownedBadges", new HashSet<>()));
    KeyValueDataManager.get().edit().putStringSet("ownedBadges", ownedBadges).apply();

    CROSSViewModel viewModel = CROSSCityApp.get().getViewModel();
    if (viewModel != null) viewModel.updateOwnedBadges(ownedBadges);
  }

  public void enqueueSyncWork() {
    OneTimeWorkRequest travelSyncWorkRequest =
        new OneTimeWorkRequest.Builder(TravelSyncWorker.class)
            .setConstraints(
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(
                LINEAR,
                // Ten seconds
                MIN_BACKOFF_MILLIS,
                MILLISECONDS)
            .build();
    WorkManager.getInstance(CROSSCityApp.get()).enqueue(travelSyncWorkRequest);
  }

  synchronized boolean sync() {
    final Set<Trip> latestTrips = new HashSet<>();
    final Set<Visit> latestVisits = new HashSet<>();
    final List<ScoreboardProfile> scoreboard;
    try {
      APIManager.get().getTripAPI().getTrips(latestTrips, latestVisits);
      scoreboard = APIManager.get().getScoreboardAPI().getScoreboard();
      Timber.i("Travel synchronization was successful.");
    } catch (IOException e) {
      Timber.e(e, "Travel synchronization failed.");
      update.postValue(false);
      return false;
    }
    CROSSCityApp.get()
        .db()
        .runInTransaction(() -> updateTravel(latestTrips, latestVisits, scoreboard));
    update.postValue(true);
    return true;
  }

  private void updateTravel(
      Set<Trip> latestTrips, Set<Visit> latestVisits, List<ScoreboardProfile> scoreboard) {
    // Fetch known

    List<Trip> knownTrips = CROSSCityApp.get().db().tripDao().getAll();
    List<Visit> knownVisits = CROSSCityApp.get().db().visitDao().getAll();

    // Update known

    Set<Trip> updatedTrips = new HashSet<>(latestTrips);
    updatedTrips.retainAll(knownTrips);
    CROSSCityApp.get().db().tripDao().updateAll(updatedTrips.toArray(new Trip[0]));

    Set<Visit> updatedVisits = new HashSet<>(latestVisits);
    updatedVisits.retainAll(knownVisits);
    CROSSCityApp.get().db().visitDao().updateAll(updatedVisits.toArray(new Visit[0]));

    // Insert new

    Set<Trip> newTrips = new HashSet<>(latestTrips);
    knownTrips.forEach(newTrips::remove);
    CROSSCityApp.get().db().tripDao().insertAll(newTrips.toArray(new Trip[0]));

    Set<Visit> newVisits = new HashSet<>(latestVisits);
    knownVisits.forEach(newVisits::remove);
    CROSSCityApp.get().db().visitDao().insertAll(newVisits.toArray(new Visit[0]));

    // Update scoreboard

    CROSSCityApp.get().db().scoreboardProfileDao().delete();
    CROSSCityApp.get()
        .db()
        .scoreboardProfileDao()
        .insertAll(scoreboard.toArray(new ScoreboardProfile[0]));
  }

  public void enqueueSubmitWork() {
    OneTimeWorkRequest travelSubmitWorkRequest =
        new OneTimeWorkRequest.Builder(TravelSubmitWorker.class)
            .setConstraints(
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(
                LINEAR,
                // Ten seconds
                MIN_BACKOFF_MILLIS,
                MILLISECONDS)
            .build();
    WorkManager.getInstance(CROSSCityApp.get()).enqueue(travelSubmitWorkRequest);
  }

  synchronized boolean submitUnsubmittedVisits() {
    List<Visit> allUnsubmittedVisits = CROSSCityApp.get().db().visitDao().getUnsubmittedVisits();
    if (allUnsubmittedVisits.isEmpty()) return true;
    String[] allUnsubmittedVisitIds =
        allUnsubmittedVisits.stream().map(visit -> visit.id).toArray(String[]::new);

    List<WiFiAPEvidence> allWiFiAPEvidences =
        CROSSCityApp.get().db().wiFiAPEvidenceDao().getAllByVisitId(allUnsubmittedVisitIds);
    Map<String, List<WiFiAPEvidence>> allWiFiAPEvidencesGroupedByVisitId =
        allWiFiAPEvidences.stream()
            .collect(Collectors.groupingBy(wiFiAPEvidence -> wiFiAPEvidence.visitId));

    List<PeerEndorsement> allPeerEndorsements =
        CROSSCityApp.get().db().peerEndorsementDao().getAllByVisitId(allUnsubmittedVisitIds);
    Map<String, List<PeerEndorsement>> allPeerEndorsementsGroupedByVisitId =
        allPeerEndorsements.stream()
            .collect(Collectors.groupingBy(peerEndorsement -> peerEndorsement.visitId));

    Map<String, List<Visit>> unsubmittedVisitsGroupedByTripId =
        allUnsubmittedVisits.stream().collect(Collectors.groupingBy(visit -> visit.tripId));
    List<Trip> allUnsubmittedTrips =
        CROSSCityApp.get()
            .db()
            .tripDao()
            .getAllById(unsubmittedVisitsGroupedByTripId.keySet().toArray(new String[0]));

    boolean anySubmissionSuccessful = false;
    boolean successful = true;
    for (Trip unsubmittedTrip : allUnsubmittedTrips) {
      try {
        submitTrip(
            unsubmittedTrip,
            unsubmittedVisitsGroupedByTripId.get(unsubmittedTrip.id),
            allWiFiAPEvidencesGroupedByVisitId,
            allPeerEndorsementsGroupedByVisitId);
        anySubmissionSuccessful = true;
        Timber.i("Travel submission was successful.");
      } catch (APIException e) {
        anySubmissionSuccessful = true;
        Timber.w(e, "Travel submission returned an API exception.");
      } catch (IOException e) {
        successful = false;
        Timber.w(e, "Travel submission failed.");
      }
    }
    if (anySubmissionSuccessful) {
      CROSSCityApp.get()
          .showToast(
              CROSSCityApp.get()
                  .getString(
                      successful
                          ? R.string.visit_submission_successful
                          : R.string.visit_submission_partially_successful));
      update.postValue(true);
    }
    return successful;
  }

  private static class TravelManagerHolder {
    private static final TravelManager INSTANCE = new TravelManager();
  }
}
