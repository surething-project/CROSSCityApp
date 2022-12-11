package pt.ulisboa.tecnico.cross.ui.poi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.databinding.FragmentPoiBinding;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import timber.log.Timber;

public class POIFragment extends Fragment {

  private FragmentPoiBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPoiBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    CROSSViewModel viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
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

              Glide.with(this)
                  .load(waypoint.poi.imageUrl)
                  .centerCrop()
                  .transition(DrawableTransitionOptions.withCrossFade())
                  .into(binding.poiImage);
              binding.poiName.setText(waypoint.poi.getName(getContext()));
              binding.poiDescription.setText(waypoint.poi.getDescription(getContext()));

              if (waypoint.poi.webUrl != null) {
                binding.webUrlLayout.setVisibility(View.VISIBLE);
                binding.webUrl.setOnClickListener(
                    v ->
                        startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(waypoint.poi.webUrl))));
              }

              binding.startVisit.setVisibility(View.GONE);
              if (!waypoint.wasVisitVerified()) {
                binding.startVisit.setVisibility(View.VISIBLE);
                binding.startVisit.setOnClickListener(
                    v ->
                        Navigation.findNavController(v)
                            .navigate(
                                LoginManager.get().isLoggedIn()
                                    ? POIFragmentDirections.actionPoiToVisitStart()
                                    : POIFragmentDirections.actionPoiToLogin()));
              }
            });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
