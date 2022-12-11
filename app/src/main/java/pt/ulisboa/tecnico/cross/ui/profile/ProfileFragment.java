package pt.ulisboa.tecnico.cross.ui.profile;

import static android.text.TextUtils.TruncateAt.END;
import static androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT;
import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.ALL_TIME;
import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.SEASONAL;
import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.WEEKLY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.databinding.FragmentBadgesItemBinding;
import pt.ulisboa.tecnico.cross.databinding.FragmentProfileBinding;
import pt.ulisboa.tecnico.cross.model.badging.Badge;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;

public class ProfileFragment extends Fragment {

  private FragmentProfileBinding binding;
  private CROSSViewModel viewModel;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentProfileBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ProfileFragmentArgs args = ProfileFragmentArgs.fromBundle(getArguments());
    String username =
        args.getUsername() != null
            ? args.getUsername()
            : LoginManager.get().getLoggedInUser().getValue().getUsername();
    binding.username.setText(username);

    if (args.getUsername() == null) {
      binding.allTime.setOnClickListener(v -> navigateToScoreboard(v, R.string.all_time));
      binding.seasonal.setOnClickListener(v -> navigateToScoreboard(v, R.string.seasonal));
      binding.weekly.setOnClickListener(v -> navigateToScoreboard(v, R.string.weekly));
    } else {
      binding.allTime.setClickable(false);
      binding.seasonal.setClickable(false);
      binding.weekly.setClickable(false);
    }

    viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
    viewModel
        .getScoreboard()
        .observe(
            getViewLifecycleOwner(),
            scoreboard -> {
              List<ScoreboardProfile> profiles =
                  scoreboard.stream()
                      .filter(profile -> profile.username.equals(username))
                      .collect(Collectors.toList());
              ScoreboardProfile allTimeProfile =
                  profiles.stream()
                      .filter(profile -> profile.category == ALL_TIME)
                      .findAny()
                      .orElse(null);
              ScoreboardProfile seasonalProfile =
                  profiles.stream()
                      .filter(profile -> profile.category == SEASONAL)
                      .findAny()
                      .orElse(null);
              ScoreboardProfile weeklyProfile =
                  profiles.stream()
                      .filter(profile -> profile.category == WEEKLY)
                      .findAny()
                      .orElse(null);

              displayProfile(binding.allTimePosition, binding.allTimeScore, allTimeProfile);
              displayProfile(binding.seasonalPosition, binding.seasonalScore, seasonalProfile);
              displayProfile(binding.weeklyPosition, binding.weeklyScore, weeklyProfile);

              viewModel.getBadges().removeObservers(getViewLifecycleOwner());
              if (allTimeProfile == null) {
                binding.numberOfBadges.setText(getString(R.string.badges_earned, 0));
                return;
              }
              viewModel
                  .getBadges()
                  .observe(
                      getViewLifecycleOwner(),
                      badges -> {
                        List<Badge> ownedBadges =
                            badges.stream()
                                .filter(badge -> allTimeProfile.ownedBadges.contains(badge.id))
                                .collect(Collectors.toList());
                        displayBadges(
                            binding.numberOfBadges,
                            binding.badges,
                            R.drawable.border_primary_variant,
                            ownedBadges,
                            this::navigateToBadge);
                      });
            });
  }

  public static void displayProfile(TextView position, TextView score, ScoreboardProfile profile) {
    if (profile != null) {
      position.setVisibility(View.VISIBLE);
      adornPosition(position.getContext(), position, profile.position);
      score.setText(String.valueOf(profile.score));
    } else {
      position.setVisibility(View.GONE);
      score.setText(String.valueOf(0));
    }
  }

  @SuppressLint("DefaultLocale")
  public static void adornPosition(Context context, TextView view, int position) {
    view.setText(String.format("#%d", position));
    switch (position) {
      case 1:
        view.setTextColor(ContextCompat.getColorStateList(context, R.color.black));
        view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.yellow));
        break;
      case 2:
        view.setTextColor(ContextCompat.getColorStateList(context, R.color.white));
        view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
        break;
      case 3:
        view.setTextColor(ContextCompat.getColorStateList(context, R.color.white));
        view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.brown));
        break;
    }
  }

  public static void displayBadges(
      TextView numberOfBadges,
      LinearLayout linearLayout,
      @DrawableRes int drawable,
      List<Badge> badges,
      BiConsumer<View, Badge> navigateToBadge) {
    numberOfBadges.setText(
        numberOfBadges.getContext().getString(R.string.badges_earned, badges.size()));
    linearLayout.removeAllViews();
    for (Badge badge : badges) {
      FragmentBadgesItemBinding itemBinding =
          FragmentBadgesItemBinding.inflate(
              LayoutInflater.from(linearLayout.getContext()), linearLayout, false);
      itemBinding.getRoot().setLayoutParams(new LayoutParams(400, WRAP_CONTENT));
      itemBinding
          .getRoot()
          .setBackground(ContextCompat.getDrawable(linearLayout.getContext(), drawable));

      Glide.with(linearLayout)
          .load(badge.imageUrl)
          .centerCrop()
          .transition(DrawableTransitionOptions.withCrossFade())
          .into(itemBinding.image);

      itemBinding.name.setMaxLines(1);
      itemBinding.name.setEllipsize(END);
      itemBinding.name.setText(badge.getName(linearLayout.getContext()));

      itemBinding.getRoot().setOnClickListener(v -> navigateToBadge.accept(v, badge));
      linearLayout.addView(itemBinding.getRoot());
    }
  }

  private void navigateToScoreboard(View view, @StringRes int category) {
    Navigation.findNavController(view)
        .navigate(ProfileFragmentDirections.actionProfileToScoreboard().setCategory(category));
  }

  private void navigateToBadge(View view, Badge badge) {
    viewModel.setForegroundBadge(badge);
    Navigation.findNavController(view).navigate(ProfileFragmentDirections.actionProfileToBadge());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
