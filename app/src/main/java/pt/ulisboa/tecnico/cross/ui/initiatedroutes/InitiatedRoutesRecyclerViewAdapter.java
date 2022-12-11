package pt.ulisboa.tecnico.cross.ui.initiatedroutes;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.databinding.FragmentInitiatedRoutesItemBinding;
import pt.ulisboa.tecnico.cross.model.route.ExtendedRoute;
import pt.ulisboa.tecnico.cross.model.route.Route;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;

/** {@link RecyclerView.Adapter} that can display a {@link Route,TripProgress}. */
public class InitiatedRoutesRecyclerViewAdapter
    extends RecyclerView.Adapter<InitiatedRoutesRecyclerViewAdapter.ViewHolder> {

  private final Fragment fragment;
  private final CROSSViewModel viewModel;
  private final InitiatedRoutesViewModel initiatedRoutesViewModel;
  private List<ExtendedRoute> initiatedRoutes;
  private Map<String, TripProgress> tripsProgress;

  public InitiatedRoutesRecyclerViewAdapter(Fragment fragment) {
    this.fragment = fragment;
    this.viewModel = new ViewModelProvider(fragment.requireActivity()).get(CROSSViewModel.class);
    this.viewModel
        .getRouteCollection()
        .observe(fragment.getViewLifecycleOwner(), this::updateInitiatedRoutes);
    this.initiatedRoutesViewModel =
        new ViewModelProvider(fragment).get(InitiatedRoutesViewModel.class);
    this.initiatedRoutesViewModel
        .getTripsProgress()
        .observe(fragment.getViewLifecycleOwner(), this::updateTripsProgress);
  }

  private synchronized void updateInitiatedRoutes(List<ExtendedRoute> routeCollection) {
    this.initiatedRoutes =
        routeCollection.stream()
            .filter(route -> route.wasInitiated() && !route.wasCompleted())
            .collect(Collectors.toList());
    this.initiatedRoutesViewModel.asyncUpdateTripsProgress(this.initiatedRoutes);
    // Notification that the dataset has changed is called in the subsequent method when the route
    // progress is also updated
  }

  @SuppressLint("NotifyDataSetChanged")
  private synchronized void updateTripsProgress(Map<String, TripProgress> tripsProgress) {
    this.tripsProgress = tripsProgress;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        FragmentInitiatedRoutesItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public synchronized void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ExtendedRoute route = initiatedRoutes.get(position);
    holder.itemView.setOnClickListener(v -> navigateToPOIsSelection(v, route));
    Glide.with(fragment)
        .load(route.route.imageUrl)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(holder.binding.image);
    holder.binding.name.setText(route.route.getName(fragment.getContext()));
    TripProgress tripProgress = tripsProgress.getOrDefault(route.route.id, null);
    if (tripProgress == null || tripProgress.getNumberOfVisits() == 0) return;
    holder.binding.tripProgress.setVisibility(View.VISIBLE);
    holder.binding.tripProgress.setText(
        fragment.getString(
            R.string.trip_progress,
            tripProgress.getNumberOfVisits(),
            tripProgress.getNumberOfWaypoints()));
    holder.binding.tripProgressBar.setVisibility(View.VISIBLE);
    holder.binding.tripProgressBar.setProgress(tripProgress.getProgress());
  }

  private void navigateToPOIsSelection(View view, ExtendedRoute route) {
    viewModel.setForegroundRoute(route);
    Navigation.findNavController(view)
        .navigate(InitiatedRoutesFragmentDirections.actionInitiatedRoutesToPoisSelection());
  }

  @Override
  public synchronized int getItemCount() {
    return initiatedRoutes.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public FragmentInitiatedRoutesItemBinding binding;

    public ViewHolder(FragmentInitiatedRoutesItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
