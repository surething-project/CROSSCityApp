package pt.ulisboa.tecnico.cross.ui.home;

import static pt.ulisboa.tecnico.cross.ui.home.HomeFragment.ROUTE_CATEGORIES;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomeViewPager2Adapter extends FragmentStateAdapter {

  public HomeViewPager2Adapter(Fragment fragment) {
    super(fragment);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return RoutesFragment.newInstance(ROUTE_CATEGORIES[position]);
  }

  @Override
  public int getItemCount() {
    return ROUTE_CATEGORIES.length;
  }
}
