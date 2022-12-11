package pt.ulisboa.tecnico.cross.ui.scoreboard;

import static pt.ulisboa.tecnico.cross.ui.profile.ProfileFragment.adornPosition;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.databinding.FragmentScoreboardItemBinding;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;

/** {@link RecyclerView.Adapter} that can display a {@link ScoreboardProfile}. */
public class ProfilesRecyclerViewAdapter
    extends RecyclerView.Adapter<ProfilesRecyclerViewAdapter.ViewHolder> {

  private final Fragment fragment;
  private final CROSSViewModel viewModel;
  private final CATEGORY scoreboardCategory;
  private List<ScoreboardProfile> scoreboard;

  public ProfilesRecyclerViewAdapter(Fragment fragment, CATEGORY scoreboardCategory) {
    this.fragment = fragment;
    this.scoreboardCategory = scoreboardCategory;
    this.viewModel = new ViewModelProvider(fragment.requireActivity()).get(CROSSViewModel.class);
    this.viewModel
        .getScoreboard()
        .observe(fragment.getViewLifecycleOwner(), this::updateScoreboard);
  }

  @SuppressLint("NotifyDataSetChanged")
  private synchronized void updateScoreboard(List<ScoreboardProfile> scoreboard) {
    this.scoreboard =
        scoreboard.stream()
            .filter(profile -> profile.category == scoreboardCategory)
            .collect(Collectors.toList());
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(
        FragmentScoreboardItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public synchronized void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ScoreboardProfile profile = scoreboard.get(position);
    holder.itemView.setOnClickListener(v -> navigateToBadge(v, profile));
    adornPosition(fragment.getContext(), holder.binding.position, profile.position);
    holder.binding.username.setText(profile.username);
    holder.binding.score.setText(String.valueOf(profile.score));
  }

  private void navigateToBadge(View view, ScoreboardProfile profile) {
    Navigation.findNavController(view)
        .navigate(
            ScoreboardFragmentDirections.actionScoreboardToProfile().setUsername(profile.username));
  }

  @Override
  public synchronized int getItemCount() {
    return scoreboard.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public final FragmentScoreboardItemBinding binding;

    public ViewHolder(FragmentScoreboardItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
