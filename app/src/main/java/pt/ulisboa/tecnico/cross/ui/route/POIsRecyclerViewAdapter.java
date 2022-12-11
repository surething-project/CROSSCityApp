package pt.ulisboa.tecnico.cross.ui.route;

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

import pt.ulisboa.tecnico.cross.databinding.FragmentPoisItemBinding;
import pt.ulisboa.tecnico.cross.model.poi.POI;
import pt.ulisboa.tecnico.cross.model.waypoint.ExtendedWaypoint;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;

/** {@link RecyclerView.Adapter} that can display a {@link POI}. */
public class POIsRecyclerViewAdapter
    extends RecyclerView.Adapter<POIsRecyclerViewAdapter.ViewHolder> {

  private final Fragment fragment;
  private final CROSSViewModel viewModel;
  private List<ExtendedWaypoint> waypointCollection;

  public POIsRecyclerViewAdapter(Fragment fragment) {
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
        FragmentPoisItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ExtendedWaypoint waypoint = waypointCollection.get(position);
    holder.itemView.setOnClickListener(v -> navigateToPOI(v, waypoint));
    Glide.with(fragment)
        .load(waypoint.poi.imageUrl)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(holder.binding.image);
    holder.binding.name.setText(waypoint.poi.getName(fragment.getContext()));
  }

  private void navigateToPOI(View view, ExtendedWaypoint waypoint) {
    viewModel.setForegroundWaypoint(waypoint);
    Navigation.findNavController(view).navigate(RouteFragmentDirections.actionRouteToPoi());
  }

  @Override
  public synchronized int getItemCount() {
    return waypointCollection.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final FragmentPoisItemBinding binding;

    public ViewHolder(FragmentPoisItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
