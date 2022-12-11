package pt.ulisboa.tecnico.cross.ui.badging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.databinding.FragmentBadgingBinding;

public class BadgingFragment extends Fragment {

  public static int[] BADGING_CATEGORIES = new int[] {R.string.earned, R.string.collection};

  private FragmentBadgingBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentBadgingBinding.inflate(inflater, container, false);
    binding.badgesViewpager.setAdapter(new BadgingViewPager2Adapter(this));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    new TabLayoutMediator(
            binding.categoriesTabs,
            binding.badgesViewpager,
            (tab, position) -> tab.setText(getString(BADGING_CATEGORIES[position])))
        .attach();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
