package pt.ulisboa.tecnico.cross.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

  public static int[] ROUTE_CATEGORIES = new int[] {R.string.untraveled, R.string.traveled};

  private FragmentHomeBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    binding.routesViewpager.setAdapter(new HomeViewPager2Adapter(this));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    new TabLayoutMediator(
            binding.categoriesTabs,
            binding.routesViewpager,
            (tab, position) -> tab.setText(getString(ROUTE_CATEGORIES[position])))
        .attach();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
