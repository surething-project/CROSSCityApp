package pt.ulisboa.tecnico.cross.ui.fullscreen;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FullscreenViewModel extends ViewModel {

  private final MutableLiveData<Boolean> isFullscreen;

  public FullscreenViewModel() {
    isFullscreen = new MutableLiveData<>(false);
  }

  public synchronized void setFullscreenState(boolean isFullscreen) {
    this.isFullscreen.setValue(isFullscreen);
  }

  public synchronized void toggleFullscreenState() {
    isFullscreen.setValue(!isFullscreen.getValue());
  }

  public synchronized LiveData<Boolean> isFullscreen() {
    return isFullscreen;
  }
}
