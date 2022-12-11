package pt.ulisboa.tecnico.cross.ui.badging;

import static pt.ulisboa.tecnico.cross.ui.badging.BadgingFragment.BADGING_CATEGORIES;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BadgingViewPager2Adapter extends FragmentStateAdapter {

  public BadgingViewPager2Adapter(Fragment fragment) {
    super(fragment);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return BadgesFragment.newInstance(BADGING_CATEGORIES[position]);
  }

  @Override
  public int getItemCount() {
    return BADGING_CATEGORIES.length;
  }
}
