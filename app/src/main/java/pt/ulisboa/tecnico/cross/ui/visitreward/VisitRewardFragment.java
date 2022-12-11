package pt.ulisboa.tecnico.cross.ui.visitreward;

import static pt.ulisboa.tecnico.cross.ui.profile.ProfileFragment.displayBadges;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.databinding.FragmentVisitRewardBinding;
import pt.ulisboa.tecnico.cross.model.badging.Badge;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import pt.ulisboa.tecnico.cross.ui.fullscreen.FullscreenFragment;
import pt.ulisboa.tecnico.cross.ui.poisselection.POIsSelectionFragment;

public class VisitRewardFragment extends FullscreenFragment {

  private FragmentVisitRewardBinding binding;
  private Handler treasureOpening;
  private CROSSViewModel viewModel;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentVisitRewardBinding.inflate(inflater, container, false);
    onFragmentBindingCreated(binding.contentLayout, null);
    return binding.getRoot();
  }

  @SuppressLint("DefaultLocale")
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);

    binding.treasureAnimation.playAnimation();
    treasureOpening.postDelayed(this::openTreasure, TimeUnit.SECONDS.toMillis(1));

    VisitRewardFragmentArgs args = VisitRewardFragmentArgs.fromBundle(getArguments());
    binding.gems.setText(String.format("+%d", args.getAwardedGems()));
    binding.score.setText(String.format("+%d", args.getAwardedScore()));
    binding.gemsTip.setOnClickListener(View::performLongClick);
    binding.xpTip.setOnClickListener(View::performLongClick);

    binding.badgesContainer.setVisibility(
        args.getAwardedBadges().length > 0 ? View.VISIBLE : View.GONE);
    if (args.getAwardedBadges().length > 0) {
      viewModel
          .getBadges()
          .observe(
              getViewLifecycleOwner(),
              badges -> {
                List<Badge> awardedBadges =
                    badges.stream()
                        .filter(
                            badge ->
                                Arrays.stream(args.getAwardedBadges())
                                    .anyMatch(awardedBadge -> badge.id.equals(awardedBadge)))
                        .collect(Collectors.toList());
                displayBadges(
                    binding.numberOfBadges,
                    binding.badges,
                    R.drawable.border_blue,
                    awardedBadges,
                    this::navigateToBadge);
              });
    }

    binding.claim.setOnClickListener(
        (v) -> {
          NavController navController = Navigation.findNavController(v);
          try {
            /** If the {@link POIsSelectionFragment} is not backstached, throws an exception. */
            navController.getBackStackEntry(R.id.nav_pois_selection);
            navController.navigate(
                VisitRewardFragmentDirections.cyclicActionVisitRewardToPoisSelection());
          } catch (IllegalArgumentException e) {
            navController.navigate(
                VisitRewardFragmentDirections.actionVisitRewardToPoisSelection());
          }
        });
  }

  private void openTreasure() {
    binding.treasureAnimation.setVisibility(View.GONE);
    binding.treasureAnimation.cancelAnimation();
    binding.rewards.setVisibility(View.VISIBLE);
    binding.claim.setVisibility(View.VISIBLE);
    binding.gemsAnimation.playAnimation();
  }

  private void navigateToBadge(View view, Badge badge) {
    viewModel.setForegroundBadge(badge);
    Navigation.findNavController(view)
        .navigate(VisitRewardFragmentDirections.actionVisitRewardToBadge());
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    treasureOpening = new Handler(Looper.getMainLooper());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    treasureOpening.removeCallbacksAndMessages(null);
    treasureOpening = null;
    binding = null;
  }
}
