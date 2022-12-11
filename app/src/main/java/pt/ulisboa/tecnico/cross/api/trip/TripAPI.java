package pt.ulisboa.tecnico.cross.api.trip;

import androidx.annotation.StringRes;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.api.CallWrapper;
import pt.ulisboa.tecnico.cross.api.trip.domain.TripSubmissionResponse;
import pt.ulisboa.tecnico.cross.contract.Evidence;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass.CreateOrUpdateTripResponse;
import pt.ulisboa.tecnico.cross.model.peerendorsement.PeerEndorsement;
import pt.ulisboa.tecnico.cross.model.trip.Trip;
import pt.ulisboa.tecnico.cross.model.visit.Visit;
import pt.ulisboa.tecnico.cross.model.wifiapevidence.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition;
import retrofit2.Retrofit;
import timber.log.Timber;

public class TripAPI {

  private final TripService tripService;

  public TripAPI(Retrofit retrofit) {
    tripService = retrofit.create(TripService.class);
  }

  public void getTrips(Set<Trip> outputTrips, Set<Visit> outputVisits) throws IOException {
    TripOuterClass.GetTripsResponse response =
        new CallWrapper<>(tripService.getTrips(APIManager.get().getJwt())).execute();
    for (TripOuterClass.Trip trip : response.getTripsList()) {
      outputTrips.add(fromProtobuf(trip));
      for (TripOuterClass.Visit visit : trip.getVisitsList()) {
        outputVisits.add(fromProtobuf(trip, visit));
      }
    }
  }

  public TripSubmissionResponse createOrUpdateTrip(
      Trip trip,
      List<Visit> visits,
      Map<String, List<WiFiAPEvidence>> wiFiAPEvidencesGroupedByVisitId,
      Map<String, List<PeerEndorsement>> peerEndorsementsGroupedByVisitId)
      throws IOException {
    TripOuterClass.Trip tripProtobuf =
        toProtobuf(trip, visits, wiFiAPEvidencesGroupedByVisitId, peerEndorsementsGroupedByVisitId);
    Timber.i("Trip to be submitted: %s", tripProtobuf);

    CreateOrUpdateTripResponse response =
        new CallWrapper<>(tripService.createOrUpdateTrip(APIManager.get().getJwt(), tripProtobuf))
            .execute();
    return new TripSubmissionResponse(
        response.getCompletedTrip(),
        response.getVisitVerificationStatusMap().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> fromProtobuf(e.getValue()))),
        response.getAwardedScore(),
        response.getAwardedGems(),
        response.getAwardedBadgesList());
  }

  private Trip fromProtobuf(TripOuterClass.Trip trip) {
    return new Trip(trip.getId(), trip.getRouteId(), trip.getCompleted());
  }

  private Visit fromProtobuf(TripOuterClass.Trip trip, TripOuterClass.Visit visit) {
    return new Visit(
        visit.getId(),
        trip.getId(),
        visit.getPoiId(),
        fromProtobuf(visit.getEntryTime()),
        fromProtobuf(visit.getLeaveTime()),
        true,
        false);
  }

  private @StringRes int fromProtobuf(
      TripOuterClass.VisitVerificationStatus visitVerificationStatus) {
    switch (visitVerificationStatus) {
      case NOT_ENOUGH_CONFIDENCE:
        return R.string.not_enough_confidence;
      case SHORT_DURATION:
        return R.string.short_duration;
    }
    return R.string.visit_verified;
  }

  private TripOuterClass.Trip toProtobuf(
      Trip trip,
      List<Visit> visits,
      Map<String, List<WiFiAPEvidence>> wiFiAPEvidencesGroupedByVisitId,
      Map<String, List<PeerEndorsement>> peerEndorsementsGroupedByVisitId) {
    return TripOuterClass.Trip.newBuilder()
        .setId(trip.id)
        .setRouteId(trip.routeId)
        .addAllVisits(
            visits.stream()
                .map(
                    visit ->
                        toProtobuf(
                            visit,
                            wiFiAPEvidencesGroupedByVisitId.getOrDefault(
                                visit.id, new ArrayList<>()),
                            peerEndorsementsGroupedByVisitId.getOrDefault(
                                visit.id, new ArrayList<>())))
                .collect(Collectors.toList()))
        .build();
  }

  private TripOuterClass.Visit toProtobuf(
      Visit visit, List<WiFiAPEvidence> wiFiAPEvidences, List<PeerEndorsement> peerEndorsements) {
    return TripOuterClass.Visit.newBuilder()
        .setId(visit.id)
        .setPoiId(visit.poiId)
        .setEntryTime(toProtobuf(visit.entryMillis))
        .setLeaveTime(toProtobuf(visit.leaveMillis))
        .addAllVisitEvidences(
            wiFiAPEvidences.stream().map(this::toProtobuf).collect(Collectors.toList()))
        .addAllVisitEvidences(
            peerEndorsements.stream().map(this::toProtobuf).collect(Collectors.toList()))
        .build();
  }

  private Evidence.VisitEvidence toProtobuf(WiFiAPEvidence wiFiAPEvidence) {
    return Evidence.VisitEvidence.newBuilder()
        .setWiFiAPEvidence(
            Evidence.WiFiAPEvidence.newBuilder()
                .setBssid(wiFiAPEvidence.bssid)
                .setSsid(wiFiAPEvidence.ssid)
                .setSightingMillis(wiFiAPEvidence.sightingMillis)
                .build())
        .build();
  }

  private Evidence.VisitEvidence toProtobuf(PeerEndorsement peerEndorsement) {
    try {
      return Evidence.VisitEvidence.newBuilder()
          .setPeerEndorsement(
              PeerEndorsementAcquisition.PeerEndorsement.parseFrom(peerEndorsement.endorsement))
          .build();
    } catch (InvalidProtocolBufferException e) {
      Timber.e(e, "This can never happen.");
      return null;
    }
  }

  private long fromProtobuf(Timestamp timestamp) {
    Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    return instant.toEpochMilli();
  }

  private Timestamp toProtobuf(long millis) {
    Instant instant = Instant.ofEpochMilli(millis);
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }
}
