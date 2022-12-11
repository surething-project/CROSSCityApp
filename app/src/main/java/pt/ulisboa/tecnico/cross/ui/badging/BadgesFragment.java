package pt.ulisboa.tecnico.cross.ui.badging;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.ulisboa.tecnico.cross.databinding.FragmentBadgesListBinding;

public class BadgesFragment extends Fragment {

  private static final int COLUMN_COUNT = 2;
  private static final String ARG_BADGING_CATEGORY = "BADGING_CATEGORY";

  private FragmentBadgesListBinding binding;
  private int badgingCategory;

  public static BadgesFragment newInstance(@StringRes int badgingCategory) {
    BadgesFragment fragment = new BadgesFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_BADGING_CATEGORY, badgingCategory);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      badgingCategory = getArguments().getInt(ARG_BADGING_CATEGORY);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentBadgesListBinding.inflate(inflater, container, false);
    RecyclerView recyclerView = binding.list;
    Context context = recyclerView.getContext();

    if (COLUMN_COUNT <= 1) {
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
    } else {
      recyclerView.setLayoutManager(new GridLayoutManager(context, COLUMN_COUNT));
    }
    recyclerView.setAdapter(new BadgesRecyclerViewAdapter(this, badgingCategory));
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
