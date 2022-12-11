package pt.ulisboa.tecnico.cross.ui.visitstart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.Locale;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.MainActivity;
import pt.ulisboa.tecnico.cross.MainActivity.STRATEGY;
import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.ble.BLEService;
import pt.ulisboa.tecnico.cross.databinding.FragmentVisitStartBinding;
import pt.ulisboa.tecnico.cross.model.route.ExtendedRoute;
import pt.ulisboa.tecnico.cross.model.trip.Trip;
import pt.ulisboa.tecnico.cross.model.visit.Visit;
import pt.ulisboa.tecnico.cross.model.waypoint.ExtendedWaypoint;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import pt.ulisboa.tecnico.cross.ui.fullscreen.FullscreenFragment;
import pt.ulisboa.tecnico.cross.wifi.WiFiScanService;
import timber.log.Timber;

public class VisitStartFragment extends FullscreenFragment {

  private FragmentVisitStartBinding binding;
  private CROSSViewModel viewModel;
  private ExtendedWaypoint waypoint;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentVisitStartBinding.inflate(inflater, container, false);
    onFragmentBindingCreated(binding.contentLayout, null);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();

    viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
    viewModel.visitAllowed().observe(getViewLifecycleOwner(), this::setStartVisitEnabled);
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

              binding.directions.setOnClickListener(
                  v ->
                      startActivity(
                          new Intent(
                              Intent.ACTION_VIEW,
                              Uri.parse(
                                  String.format(
                                      Locale.US,
                                      "https://www.google.com/maps/dir/?api=1&destination=%f,%f&travelmode=walking",
                                      waypoint.poi.worldCoord[0],
                                      waypoint.poi.worldCoord[1])))));
              binding.startVisit.setOnClickListener(this::startVisit);
              Glide.with(this)
                  .load(waypoint.poi.imageUrl)
                  .centerCrop()
                  .transition(DrawableTransitionOptions.withCrossFade())
                  .into(binding.poiImage);
            });
  }

  private void setStartVisitEnabled(boolean enabled) {
    if (enabled) enableButton(binding.startVisit);
    else disableButton(binding.startVisit);
  }

  private void startVisit(View view) {
    binding.loading.setVisibility(View.VISIBLE);
    int strategy = viewModel.selectedStrategy().getValue();
    if (STRATEGY.WIFI_SCAN.test(strategy)) WiFiScanService.start();
    if (STRATEGY.BLE_SCAN.test(strategy)) BLEService.start(waypoint);
    Runnable onCompleted =
        () -> {
          binding.loading.setVisibility(View.GONE);
          Navigation.findNavController(view)
              .navigate(VisitStartFragmentDirections.actionVisitStartToVisitEnd());
        };
    new Thread(() -> createVisit(onCompleted)).start();
  }

  private void createVisit(Runnable onCompleted) {
    ExtendedRoute route = viewModel.getForegroundRoute().getValue();

    if (!route.wasInitiated()) {
      route.trip = new Trip(route.route.id, false);
      CROSSCityApp.get().db().tripDao().insertAll(route.trip);
    }

    if (!waypoint.wasVisitInitiated()) {
      waypoint.visit =
          new Visit(route.trip.id, waypoint.poi.id, System.currentTimeMillis(), 0, false, false);
      CROSSCityApp.get().db().visitDao().insertAll(waypoint.visit);
    } else {
      waypoint.visit.entryMillis = System.currentTimeMillis();
      waypoint.visit.leaveMillis = 0;
      waypoint.visit.rejected = false;
      CROSSCityApp.get().db().visitDao().updateAll(waypoint.visit);
    }

    requireActivity().runOnUiThread(onCompleted);
  }

  public static void disableButton(Button button) {
    button.setEnabled(false);
    button.setBackgroundColor(ContextCompat.getColor(button.getContext(), R.color.grey_lighten));
    button.setAlpha(.8F);
  }

  public static void enableButton(Button button) {
    button.setAlpha(1);
    button.setBackgroundColor(ContextCompat.getColor(button.getContext(), R.color.blue));
    button.setEnabled(true);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int strategy =
        Integer.parseInt(
            PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("strategies", "3"));
    ((MainActivity) requireActivity()).setupStrategy(strategy);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    binding = null;
  }
}
