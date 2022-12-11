package pt.ulisboa.tecnico.cross.ui.fullscreen;

import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public abstract class FullscreenFragment extends Fragment {

  private static final long HIDE_DELAY_MILLIS = 500;
  private final Handler hideHandler = new Handler();

  private FullscreenViewModel fullscreenViewModel;
  private View contentLayout;
  private View controlsLayout;

  /** Must be called in {@link Fragment#onCreateView} when fragment binding is created */
  public void onFragmentBindingCreated(@NonNull View contentLayout, @Nullable View controlsLayout) {
    this.contentLayout = contentLayout;
    this.controlsLayout = controlsLayout;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    fullscreenViewModel = new ViewModelProvider(this).get(FullscreenViewModel.class);
    fullscreenViewModel.isFullscreen().observe(getViewLifecycleOwner(), this::applyFullscreenState);
    if (contentLayout != null) {
      contentLayout.setOnClickListener(v -> fullscreenViewModel.toggleFullscreenState());
    }
  }

  private void hideDelayed() {
    hideHandler.removeCallbacksAndMessages(null);
    hideHandler.postDelayed(this::hide, HIDE_DELAY_MILLIS);
  }

  private void hide() {
    fullscreenViewModel.setFullscreenState(true);
  }

  private void show() {
    fullscreenViewModel.setFullscreenState(false);
  }

  private void applyFullscreenState(boolean isFullscreen) {
    hideHandler.removeCallbacksAndMessages(null);
    setWindowSystemUiVisibility(
        isFullscreen
            ? View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            : 0);
    if (controlsLayout != null) {
      controlsLayout.setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
    }
  }

  @SuppressLint("SourceLockedOrientationActivity")
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  public void onResume() {
    super.onResume();
    addOrClearWindowFlags(true);
    showOrHideActionBar(false);
    hideDelayed();
  }

  @Override
  public void onPause() {
    super.onPause();
    addOrClearWindowFlags(false);
    showOrHideActionBar(true);
    show();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    contentLayout = null;
    controlsLayout = null;
  }

  private void addOrClearWindowFlags(boolean add) {
    Window window = requireActivity().getWindow();
    if (window == null) return;
    if (add) window.addFlags(FLAG_LAYOUT_NO_LIMITS);
    else window.clearFlags(FLAG_LAYOUT_NO_LIMITS);
  }

  private void setWindowSystemUiVisibility(int visibility) {
    Window window = requireActivity().getWindow();
    if (window == null) return;
    window.getDecorView().setSystemUiVisibility(visibility);
  }

  private void showOrHideActionBar(boolean show) {
    ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
    if (actionBar == null) return;
    if (show) actionBar.show();
    else actionBar.hide();
  }
}
