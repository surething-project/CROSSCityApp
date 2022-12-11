package pt.ulisboa.tecnico.cross.ui.scoreboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.MainActivity;
import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.databinding.FragmentScoreboardBinding;

public class ScoreboardFragment extends Fragment {

  public static int[] SCOREBOARD_CATEGORIES =
      new int[] {R.string.all_time, R.string.seasonal, R.string.weekly};

  private FragmentScoreboardBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentScoreboardBinding.inflate(inflater, container, false);
    binding.usersViewpager.setAdapter(new ScoreboardViewPager2Adapter(this));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    new TabLayoutMediator(
            binding.categoriesTabs,
            binding.usersViewpager,
            (tab, position) -> tab.setText(getString(SCOREBOARD_CATEGORIES[position])))
        .attach();

    ScoreboardFragmentArgs args = ScoreboardFragmentArgs.fromBundle(getArguments());
    if (args.getCategory() != 0) {
      binding.usersViewpager.setCurrentItem(
          Arrays.stream(SCOREBOARD_CATEGORIES)
              .boxed()
              .collect(Collectors.toList())
              .indexOf(args.getCategory()));
    }
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((MainActivity) requireActivity())
        .getOptionsMenu()
        .findItem(R.id.action_information_score)
        .setVisible(true);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ((MainActivity) requireActivity())
        .getOptionsMenu()
        .findItem(R.id.action_information_score)
        .setVisible(false);
    binding = null;
  }
}
