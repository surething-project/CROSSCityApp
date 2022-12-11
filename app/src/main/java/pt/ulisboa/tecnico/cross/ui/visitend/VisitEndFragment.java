package pt.ulisboa.tecnico.cross.ui.visitend;

import static pt.ulisboa.tecnico.cross.ui.visitstart.VisitStartFragment.disableButton;
import static pt.ulisboa.tecnico.cross.ui.visitstart.VisitStartFragment.enableButton;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.MainActivity.STRATEGY;
import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.api.exceptions.APIException;
import pt.ulisboa.tecnico.cross.api.trip.domain.TripSubmissionResponse;
import pt.ulisboa.tecnico.cross.ble.BLEService;
import pt.ulisboa.tecnico.cross.databinding.FragmentVisitEndBinding;
import pt.ulisboa.tecnico.cross.model.peerendorsement.PeerEndorsement;
import pt.ulisboa.tecnico.cross.model.route.ExtendedRoute;
import pt.ulisboa.tecnico.cross.model.visit.Visit;
import pt.ulisboa.tecnico.cross.model.waypoint.ExtendedWaypoint;
import pt.ulisboa.tecnico.cross.model.wifiapevidence.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.peertopeer.PeerManager;
import pt.ulisboa.tecnico.cross.travel.TravelManager;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import pt.ulisboa.tecnico.cross.ui.fullscreen.FullscreenFragment;
import pt.ulisboa.tecnico.cross.ui.poisselection.POIsSelectionFragment;
import pt.ulisboa.tecnico.cross.wifi.WiFiScanService;
import pt.ulisboa.tecnico.cross.wifi.WiFiScanner;
import timber.log.Timber;

public class VisitEndFragment extends FullscreenFragment {

  private FragmentVisitEndBinding binding;
  private CROSSViewModel viewModel;
  private Handler elapsedTimeTimer;
  private ExtendedWaypoint waypoint;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentVisitEndBinding.inflate(inflater, container, false);
    onFragmentBindingCreated(binding.contentLayout, null);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();

    viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
    viewModel
        .getForegroundWaypoint()
        .observe(
            getViewLifecycleOwner(),
            waypoint -> {
              if (waypoint == null) {
                Timber.e("Current point of interest not defined.");
                if (!Navigation.findNavController(view).popBackStack()) requireActivity().finish();
                return;
              }
              if (actionBar != null) {
                actionBar.setTitle(waypoint.poi.getName(getContext()));
              }
              synchronized (this) {
                this.waypoint = waypoint;
              }
              binding.endVisit.setOnClickListener(this::endVisit);
            });

    updateElapsedTime();
    int strategy = viewModel.selectedStrategy().getValue();
    if (STRATEGY.WIFI_SCAN.test(strategy)) {
      binding.numberOfWifiScans.setVisibility(View.VISIBLE);
      binding.numberOfWifiEvidenceCollected.setVisibility(View.VISIBLE);
      WiFiScanner.get().getNumberOfScans().observe(getViewLifecycleOwner(), this::setFeedback);
    }
    if (STRATEGY.BLE_SCAN.test(strategy)) {
      binding.numberOfPeerEvidenceCollected.setVisibility(View.VISIBLE);
      binding.numberOfPeerEndorsementsIssued.setVisibility(View.VISIBLE);
      PeerManager.get()
          .getNumberOfEvidenceCollected()
          .observe(
              getViewLifecycleOwner(),
              numberOfEvidenceCollected ->
                  binding.numberOfPeerEvidenceCollected.setText(
                      getString(R.string.peer_evidences_collected, numberOfEvidenceCollected)));
      PeerManager.get()
          .getNumberOfEndorsementsIssued()
          .observe(
              getViewLifecycleOwner(),
              numberOfEndorsementsIssued ->
                  binding.numberOfPeerEndorsementsIssued.setText(
                      getString(R.string.peer_endorsements_issued, numberOfEndorsementsIssued)));
    }
  }

  private void updateElapsedTime() {
    synchronized (this) {
      if (waypoint != null) {
        long secondsLeft =
            waypoint.waypoint.stayForSeconds
                - TimeUnit.MILLISECONDS.toSeconds(
                    System.currentTimeMillis() - waypoint.visit.entryMillis);
        if (secondsLeft > 0) {
          binding.minimumVisitTime.setVisibility(View.VISIBLE);
          binding.minimumVisitTime.setText(
              getString(R.string.minimum_visit_time, DateUtils.formatElapsedTime(secondsLeft)));
        } else {
          binding.minimumVisitTime.setVisibility(View.GONE);
          enableButton(binding.endVisit);
          return;
        }
      } else binding.minimumVisitTime.setVisibility(View.GONE);
    }
    elapsedTimeTimer.postDelayed(this::updateElapsedTime, TimeUnit.SECONDS.toMillis(1));
  }

  private void setFeedback(int numberOfScans) {
    binding.numberOfWifiScans.setText(getString(R.string.wifi_scans_performed, numberOfScans));
    binding.numberOfWifiEvidenceCollected.setText(
        getString(R.string.wifi_evidences_collected, WiFiScanner.get().getNumberOfScanResults()));
  }

  private void endVisit(View view) {
    disableButton(binding.endVisit);
    binding.loading.setVisibility(View.VISIBLE);
    Runnable onCompleted =
        () -> {
          int strategy = viewModel.selectedStrategy().getValue();
          if (STRATEGY.WIFI_SCAN.test(strategy)) WiFiScanService.stop();
          if (STRATEGY.BLE_SCAN.test(strategy)) BLEService.stop();
          viewModel.endVisit();
          binding.loading.setVisibility(View.GONE);
          NavController navController = Navigation.findNavController(view);
          try {
            /** If the {@link POIsSelectionFragment} is not backstached, throws an exception. */
            navController.getBackStackEntry(R.id.nav_pois_selection);
            navController.navigate(
                VisitEndFragmentDirections.cyclicActionVisitEndToPoisSelection());
          } catch (IllegalArgumentException e) {
            navController.navigate(VisitEndFragmentDirections.actionVisitEndToPoisSelection());
          }
        };

    Consumer<TripSubmissionResponse> onSuccess =
        (response) -> {
          int strategy = viewModel.selectedStrategy().getValue();
          if (STRATEGY.WIFI_SCAN.test(strategy)) WiFiScanService.stop();
          if (STRATEGY.BLE_SCAN.test(strategy)) BLEService.stop();
          viewModel.endVisit();
          binding.loading.setVisibility(View.GONE);
          Navigation.findNavController(view)
              .navigate(
                  VisitEndFragmentDirections.actionVisitEndToVisitReward(
                      response.awardedGems,
                      response.awardedScore,
                      response.awardedBadges.toArray(new String[0])));
        };

    Consumer<String> onFailure =
        (message) -> {
          binding.loading.setVisibility(View.GONE);
          enableButton(binding.endVisit);
          Drawable drawable =
              DrawableCompat.wrap(
                  ContextCompat.getDrawable(getContext(), R.drawable.baseline_thumb_down_alt_24));
          int red = ContextCompat.getColor(getContext(), R.color.red);
          DrawableCompat.setTint(drawable, red);
          AlertDialog alertDialog =
              new AlertDialog.Builder(getContext())
                  .setCancelable(false)
                  .setIcon(drawable)
                  .setTitle(R.string.visit_rejected)
                  .setMessage(
                      message
                          + "\nAre you sure you want to end the visit? It will not be verified.")
                  .setPositiveButton("Continue", (dialog, which) -> dialog.dismiss())
                  .setNegativeButton(
                      "End",
                      (dialog, which) -> {
                        onCompleted.run();
                        dialog.dismiss();
                      })
                  .create();
          alertDialog.show();
          alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(red);
        };
    new Thread(() -> submitVisit(onCompleted, onSuccess, onFailure)).start();
  }

  private void submitVisit(
      Runnable onCompleted,
      Consumer<TripSubmissionResponse> onSuccess,
      Consumer<String> onFailure) {
    ExtendedRoute route = viewModel.getForegroundRoute().getValue();
    waypoint.visit.leaveMillis = System.currentTimeMillis();
    List<WiFiAPEvidence> wiFiAPEvidences = collectScanResults(waypoint.visit);
    List<PeerEndorsement> peerEndorsements = collectPeerEndorsements(waypoint.visit);
    try {
      TripSubmissionResponse response =
          TravelManager.get()
              .submitTrip(
                  route.trip,
                  Collections.singletonList(waypoint.visit),
                  Collections.singletonMap(waypoint.visit.id, wiFiAPEvidences),
                  Collections.singletonMap(waypoint.visit.id, peerEndorsements));

      int visitVerificationRes = response.visitVerificationStatus.get(waypoint.visit.id);
      if (visitVerificationRes == R.string.visit_verified) {
        CROSSCityApp.get().showToast(getString(visitVerificationRes));
        if (route.trip.completed) CROSSCityApp.get().showToast(getString(R.string.trip_completed));
        requireActivity().runOnUiThread(() -> onSuccess.accept(response));
      } else {
        requireActivity()
            .runOnUiThread(
                () ->
                    onFailure.accept(
                        getString(
                            visitVerificationRes,
                            DateUtils.formatElapsedTime(waypoint.waypoint.stayForSeconds))));
      }
    } catch (APIException e) {
      requireActivity().runOnUiThread(() -> onFailure.accept(e.getMessage()));
    } catch (IOException e) {
      CROSSCityApp.get().showToast(getString(R.string.visit_submission_failed, e.getMessage()));
      enqueueVisitSubmission(waypoint.visit, wiFiAPEvidences, peerEndorsements);
      requireActivity().runOnUiThread(onCompleted);
    }
  }

  private List<WiFiAPEvidence> collectScanResults(Visit visit) {
    return WiFiScanner.get().collectScanResults().stream()
        .map(
            result ->
                new WiFiAPEvidence(visit.id, result.bssid, result.ssid, result.sightingMillis))
        .collect(Collectors.toList());
  }

  private List<PeerEndorsement> collectPeerEndorsements(Visit visit) {
    return PeerManager.get().collectEndorsements().stream()
        .map(endorsement -> new PeerEndorsement(visit.id, endorsement))
        .collect(Collectors.toList());
  }

  private void enqueueVisitSubmission(
      Visit visit, List<WiFiAPEvidence> wiFiAPEvidences, List<PeerEndorsement> peerEndorsements) {
    CROSSCityApp.get()
        .db()
        .runInTransaction(
            () -> {
              CROSSCityApp.get()
                  .db()
                  .wiFiAPEvidenceDao()
                  .insertAll(wiFiAPEvidences.toArray(new WiFiAPEvidence[0]));
              CROSSCityApp.get()
                  .db()
                  .peerEndorsementDao()
                  .insertAll(peerEndorsements.toArray(new PeerEndorsement[0]));
              CROSSCityApp.get().db().visitDao().updateAll(visit);
            });

    TravelManager.get().enqueueSubmitWork();
    CROSSCityApp.get().showToast(getString(R.string.visit_queued));
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    elapsedTimeTimer = new Handler(Looper.getMainLooper());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    elapsedTimeTimer.removeCallbacksAndMessages(null);
    elapsedTimeTimer = null;
    binding = null;
  }
}
