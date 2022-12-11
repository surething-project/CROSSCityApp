package pt.ulisboa.tecnico.cross.ui.badge;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.cross.account.KeyValueDataManager;
import pt.ulisboa.tecnico.cross.databinding.FragmentBadgeBinding;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import timber.log.Timber;

public class BadgeFragment extends Fragment {

  private FragmentBadgeBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentBadgeBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    CROSSViewModel viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
    viewModel
        .getForegroundBadge()
        .observe(
            getViewLifecycleOwner(),
            badge -> {
              if (badge == null) {
                Timber.e("Current badge not defined.");
                if (!Navigation.findNavController(view).popBackStack()) requireActivity().finish();
                return;
              }
              Set<String> ownedBadges =
                  KeyValueDataManager.get().getStringSet("ownedBadges", new HashSet<>());
              boolean owned = ownedBadges.contains(badge.id);

              Glide.with(this)
                  .load(badge.imageUrl)
                  .centerCrop()
                  .transition(DrawableTransitionOptions.withCrossFade())
                  .into(binding.badgeImage);
              if (!owned) grayOut(binding.badgeImage);
              binding.badgeName.setText(badge.getName(getContext()));
              binding.badgeDescription.setText(
                  owned ? badge.getAchievement(getContext()) : badge.getQuest(getContext()));
              binding.animation.setVisibility(owned ? View.VISIBLE : View.GONE);
            });
  }

  public static void grayOut(ImageView image) {
    ColorMatrix matrix = new ColorMatrix();
    matrix.setSaturation(0);
    image.setColorFilter(new ColorMatrixColorFilter(matrix));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
