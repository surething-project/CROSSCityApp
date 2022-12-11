package pt.ulisboa.tecnico.cross.ui.scoreboard;

import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.ALL_TIME;
import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.SEASONAL;
import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.WEEKLY;
import static pt.ulisboa.tecnico.cross.ui.profile.ProfileFragment.displayProfile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.account.model.LoggedInUser;
import pt.ulisboa.tecnico.cross.databinding.FragmentScoreboardListBinding;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import timber.log.Timber;

public class ProfilesFragment extends Fragment {

  private static final int COLUMN_COUNT = 1;
  private static final String ARG_SCOREBOARD_CATEGORY = "SCOREBOARD_CATEGORY";

  private FragmentScoreboardListBinding binding;
  private CROSSViewModel viewModel;
  private CATEGORY scoreboardCategory;

  public static ProfilesFragment newInstance(@StringRes int scoreboardCategory) {
    ProfilesFragment fragment = new ProfilesFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_SCOREBOARD_CATEGORY, scoreboardCategory);
    fragment.setArguments(bundle);
    return fragment;
  }

  @SuppressLint("NonConstantResourceId")
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      switch (getArguments().getInt(ARG_SCOREBOARD_CATEGORY)) {
        case R.string.all_time:
          scoreboardCategory = ALL_TIME;
          break;
        case R.string.seasonal:
          scoreboardCategory = SEASONAL;
          break;
        case R.string.weekly:
          scoreboardCategory = WEEKLY;
          break;
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
    binding = FragmentScoreboardListBinding.inflate(inflater, container, false);
    RecyclerView recyclerView = binding.list;
    Context context = recyclerView.getContext();

    if (COLUMN_COUNT <= 1) {
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
    } else {
      recyclerView.setLayoutManager(new GridLayoutManager(context, COLUMN_COUNT));
    }
    recyclerView.setAdapter(new ProfilesRecyclerViewAdapter(this, scoreboardCategory));

    binding.refresh.setOnRefreshListener(
        () ->
            new Thread(
                    () -> {
                      viewModel.updateScoreboard();
                      binding.refresh.setRefreshing(false);
                    })
                .start());
    binding.refresh.setColorSchemeResources(
        android.R.color.holo_blue_bright,
        android.R.color.holo_green_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_red_light);

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel
        .getScoreboard()
        .observe(
            getViewLifecycleOwner(),
            userScores -> {
              LoggedInUser user = LoginManager.get().getLoggedInUser().getValue();
              if (user == null) {
                Timber.w("The user is not logged in.");
                return;
              }
              ScoreboardProfile profile =
                  userScores.stream()
                      .filter(
                          us ->
                              us.category == scoreboardCategory
                                  && us.username.equals(user.getUsername()))
                      .findAny()
                      .orElse(null);
              displayProfile(binding.position, binding.score, profile);
            });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
