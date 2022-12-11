package pt.ulisboa.tecnico.cross.ui.scoreboard;

import static pt.ulisboa.tecnico.cross.ui.scoreboard.ScoreboardFragment.SCOREBOARD_CATEGORIES;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ScoreboardViewPager2Adapter extends FragmentStateAdapter {

  public ScoreboardViewPager2Adapter(Fragment fragment) {
    super(fragment);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return ProfilesFragment.newInstance(SCOREBOARD_CATEGORIES[position]);
  }

  @Override
  public int getItemCount() {
    return SCOREBOARD_CATEGORIES.length;
  }
}
