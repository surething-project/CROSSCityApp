package pt.ulisboa.tecnico.cross.ui.badging;

import static pt.ulisboa.tecnico.cross.ui.badge.BadgeFragment.grayOut;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.account.KeyValueDataManager;
import pt.ulisboa.tecnico.cross.databinding.FragmentBadgesItemBinding;
import pt.ulisboa.tecnico.cross.model.badging.Badge;
import pt.ulisboa.tecnico.cross.model.route.Route;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;

/** {@link RecyclerView.Adapter} that can display a {@link Route}. */
public class BadgesRecyclerViewAdapter
    extends RecyclerView.Adapter<BadgesRecyclerViewAdapter.ViewHolder> {

  private final Fragment fragment;
  private final CROSSViewModel viewModel;
  private final int badgingCategory;
  private List<Badge> badges;

  public BadgesRecyclerViewAdapter(Fragment fragment, int badgingCategory) {
    this.fragment = fragment;
    this.badgingCategory = badgingCategory;
    this.viewModel = new ViewModelProvider(fragment.requireActivity()).get(CROSSViewModel.class);
    this.viewModel.getBadges().observe(fragment.getViewLifecycleOwner(), this::updateBadges);
  }

  @SuppressLint("NotifyDataSetChanged")
  private synchronized void updateBadges(List<Badge> badges) {
    Set<String> ownedBadges =
        KeyValueDataManager.get().getStringSet("ownedBadges", new HashSet<>());
    this.badges =
        badges.stream()
            .filter(
                badge ->
                    ownedBadges.contains(badge.id)
                        ? badgingCategory == R.string.earned
                        : badgingCategory == R.string.collection)
            .collect(Collectors.toList());
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        FragmentBadgesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public synchronized void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Badge badge = badges.get(position);
    holder.itemView.setOnClickListener(v -> navigateToBadge(v, badge));
    Glide.with(fragment)
        .load(badge.imageUrl)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(holder.binding.image);
    if (badgingCategory == R.string.collection) grayOut(holder.binding.image);
    holder.binding.name.setText(badge.getName(fragment.getContext()));
  }

  private void navigateToBadge(View view, Badge badge) {
    viewModel.setForegroundBadge(badge);
    Navigation.findNavController(view).navigate(BadgingFragmentDirections.actionBadgingToBadge());
  }

  @Override
  public synchronized int getItemCount() {
    return badges.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final FragmentBadgesItemBinding binding;

    public ViewHolder(FragmentBadgesItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
