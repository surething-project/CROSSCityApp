package pt.ulisboa.tecnico.cross.ui.initiatedroutes;

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

import pt.ulisboa.tecnico.cross.databinding.FragmentInitiatedRoutesListBinding;

public class InitiatedRoutesFragment extends Fragment {

  private FragmentInitiatedRoutesListBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentInitiatedRoutesListBinding.inflate(inflater, container, false);
    RecyclerView recyclerView = binding.list;
    Context context = recyclerView.getContext();

    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    recyclerView.setAdapter(new InitiatedRoutesRecyclerViewAdapter(this));
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
