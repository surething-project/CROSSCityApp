package pt.ulisboa.tecnico.cross.ui.home;

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

import pt.ulisboa.tecnico.cross.databinding.FragmentRoutesListBinding;

public class RoutesFragment extends Fragment {

  private static final int COLUMN_COUNT = 2;
  private static final String ARG_ROUTE_CATEGORY = "ROUTE_CATEGORY";

  private FragmentRoutesListBinding binding;
  private int routeCategory;

  public static RoutesFragment newInstance(@StringRes int routeCategory) {
    RoutesFragment fragment = new RoutesFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_ROUTE_CATEGORY, routeCategory);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      routeCategory = getArguments().getInt(ARG_ROUTE_CATEGORY);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentRoutesListBinding.inflate(inflater, container, false);
    RecyclerView recyclerView = binding.list;
    Context context = recyclerView.getContext();

    if (COLUMN_COUNT <= 1) {
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
    } else {
      recyclerView.setLayoutManager(new GridLayoutManager(context, COLUMN_COUNT));
    }
    recyclerView.setAdapter(new RoutesRecyclerViewAdapter(this, routeCategory));
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
