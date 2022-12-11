package pt.ulisboa.tecnico.cross.ui.home;

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
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.databinding.FragmentRoutesItemBinding;
import pt.ulisboa.tecnico.cross.model.route.ExtendedRoute;
import pt.ulisboa.tecnico.cross.model.route.Route;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;

/** {@link RecyclerView.Adapter} that can display a {@link Route}. */
public class RoutesRecyclerViewAdapter
    extends RecyclerView.Adapter<RoutesRecyclerViewAdapter.ViewHolder> {

  private final Fragment fragment;
  private final CROSSViewModel viewModel;
  private final int routeCategory;
  private List<ExtendedRoute> routeCollection;

  public RoutesRecyclerViewAdapter(Fragment fragment, int routeCategory) {
    this.fragment = fragment;
    this.routeCategory = routeCategory;
    this.viewModel = new ViewModelProvider(fragment.requireActivity()).get(CROSSViewModel.class);
    this.viewModel
        .getRouteCollection()
        .observe(fragment.getViewLifecycleOwner(), this::updateRouteCollection);
  }

  @SuppressLint("NotifyDataSetChanged")
  private synchronized void updateRouteCollection(List<ExtendedRoute> routeCollection) {
    this.routeCollection =
        routeCollection.stream()
            .filter(
                route ->
                    routeCategory == R.string.untraveled && !route.wasInitiated()
                        || routeCategory == R.string.traveled && route.wasCompleted())
            .collect(Collectors.toList());
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        FragmentRoutesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public synchronized void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ExtendedRoute route = routeCollection.get(position);
    holder.itemView.setOnClickListener(v -> navigateToRoute(v, route));
    Glide.with(fragment)
        .load(route.route.imageUrl)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(holder.binding.image);
    holder.binding.name.setText(route.route.getName(fragment.getContext()));
  }

  private void navigateToRoute(View view, ExtendedRoute route) {
    viewModel.setForegroundRoute(route);
    Navigation.findNavController(view).navigate(HomeFragmentDirections.actionHomeToRoute());
  }

  @Override
  public synchronized int getItemCount() {
    return routeCollection.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final FragmentRoutesItemBinding binding;

    public ViewHolder(FragmentRoutesItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
