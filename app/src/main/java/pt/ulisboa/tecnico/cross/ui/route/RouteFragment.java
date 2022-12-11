package pt.ulisboa.tecnico.cross.ui.route;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.databinding.FragmentRouteBinding;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import timber.log.Timber;

public class RouteFragment extends Fragment {

  private FragmentRouteBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentRouteBinding.inflate(inflater, container, false);
    RecyclerView recyclerView = binding.poisList;
    Context context = recyclerView.getContext();

    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    recyclerView.setAdapter(new POIsRecyclerViewAdapter(this));
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    CROSSViewModel viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
    viewModel
        .getForegroundRoute()
        .observe(
            getViewLifecycleOwner(),
            route -> {
              if (route == null) {
                Timber.e("Current route not defined.");
                if (!Navigation.findNavController(view).popBackStack()) requireActivity().finish();
                return;
              }

              Glide.with(this)
                  .load(route.route.imageUrl)
                  .centerCrop()
                  .transition(DrawableTransitionOptions.withCrossFade())
                  .into(binding.routeImage);
              binding.routeName.setText(route.route.getName(getContext()));
              binding.routeDescription.setText(route.route.getDescription(getContext()));

              binding.startRoute.setVisibility(View.GONE);
              if (!route.wasCompleted()) {
                binding.startRoute.setVisibility(View.VISIBLE);
                binding.startRoute.setOnClickListener(
                    v ->
                        Navigation.findNavController(v)
                            .navigate(
                                LoginManager.get().isLoggedIn()
                                    ? RouteFragmentDirections.actionRouteToPoisSelection()
                                    : RouteFragmentDirections.actionRouteToLogin()));
              }
            });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
