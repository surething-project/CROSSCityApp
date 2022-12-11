package pt.ulisboa.tecnico.cross.ui.poisselection;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pt.ulisboa.tecnico.cross.databinding.FragmentPoisSelectionListBinding;

public class POIsSelectionFragment extends Fragment {

  private FragmentPoisSelectionListBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPoisSelectionListBinding.inflate(inflater, container, false);
    RecyclerView recyclerView = binding.list;
    Context context = recyclerView.getContext();

    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    recyclerView.setAdapter(new POIsSelectionRecyclerViewAdapter(this));
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
