package pt.ulisboa.tecnico.cross.ui.poisselection;

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

import pt.ulisboa.tecnico.cross.databinding.FragmentPoisSelectionItemBinding;
import pt.ulisboa.tecnico.cross.model.poi.POI;
import pt.ulisboa.tecnico.cross.model.waypoint.ExtendedWaypoint;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;

/** {@link RecyclerView.Adapter} that can display a {@link POI}. */
public class POIsSelectionRecyclerViewAdapter
    extends RecyclerView.Adapter<POIsSelectionRecyclerViewAdapter.ViewHolder> {

  private final Fragment fragment;
  private final CROSSViewModel viewModel;
  private List<ExtendedWaypoint> waypointCollection;

  public POIsSelectionRecyclerViewAdapter(Fragment fragment) {
    this.fragment = fragment;
    this.viewModel = new ViewModelProvider(fragment.requireActivity()).get(CROSSViewModel.class);
    this.viewModel
        .getWaypointCollection()
        .observe(fragment.getViewLifecycleOwner(), this::updateWaypointCollection);
  }

  @SuppressLint("NotifyDataSetChanged")
  private synchronized void updateWaypointCollection(List<ExtendedWaypoint> waypointCollection) {
    this.waypointCollection = waypointCollection;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        FragmentPoisSelectionItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ExtendedWaypoint waypoint = waypointCollection.get(position);
    holder.itemView.setOnClickListener(v -> navigateToPOI(v, waypoint));
    holder.binding.verified.setVisibility(View.GONE);
    holder.binding.startVisit.setVisibility(View.GONE);
    holder.binding.rejected.setVisibility(View.GONE);
    holder.binding.incomplete.setVisibility(View.GONE);
    holder.binding.pendingSubmission.setVisibility(View.GONE);
    if (waypoint.wasVisitVerified()) {
      holder.binding.verified.setVisibility(View.VISIBLE);
    } else {
      holder.binding.startVisit.setVisibility(View.VISIBLE);
      holder.binding.startVisit.setOnClickListener(v -> navigateToVisitStart(v, waypoint));
      if (waypoint.wasVisitRejected()) {
        holder.binding.rejected.setVisibility(View.VISIBLE);
      } else if (waypoint.isVisitIncomplete()) {
        holder.binding.incomplete.setVisibility(View.VISIBLE);
      } else if (waypoint.wasVisitInitiated()) {
        holder.binding.pendingSubmission.setVisibility(View.VISIBLE);
      }
    }
    Glide.with(fragment)
        .load(waypoint.poi.imageUrl)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(holder.binding.image);
    holder.binding.name.setText(waypoint.poi.getName(fragment.getContext()));
  }

  private void navigateToPOI(View view, ExtendedWaypoint waypoint) {
    viewModel.setForegroundWaypoint(waypoint);
    Navigation.findNavController(view)
        .navigate(POIsSelectionFragmentDirections.actionPoisSelectionToPoi());
  }

  private void navigateToVisitStart(View view, ExtendedWaypoint waypoint) {
    viewModel.setForegroundWaypoint(waypoint);
    Navigation.findNavController(view)
        .navigate(POIsSelectionFragmentDirections.actionPoisSelectionToVisitStart());
  }

  @Override
  public synchronized int getItemCount() {
    return waypointCollection.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final FragmentPoisSelectionItemBinding binding;

    public ViewHolder(FragmentPoisSelectionItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
