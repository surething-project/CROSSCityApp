package pt.ulisboa.tecnico.cross.wifi;

import android.net.wifi.WifiManager;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import timber.log.Timber;

public class WiFiHelper {

  public static WiFiHelper get() {
    return WiFiHelperHolder.INSTANCE;
  }

  private WiFiHelper() {}

  void switchOn() {
    if (getManager() == null) {
      Timber.e("Wi-Fi manager manager could not be initialized.");
      return;
    }
    if (!getManager().isScanAlwaysAvailable()
        && !getManager().isWifiEnabled()
        && !getManager().setWifiEnabled(true)) {
      CROSSCityApp.get().showToast("Please turn on Wi-Fi.");
    }
    WiFiScanner.get().startScan();
  }

  void switchOff() {
    WiFiScanner.get().stopScan();
  }

  public WifiManager getManager() {
    return WiFiManagerHolder.INSTANCE;
  }

  private static class WiFiHelperHolder {
    private static final WiFiHelper INSTANCE = new WiFiHelper();
  }

  private static class WiFiManagerHolder {
    private static final WifiManager INSTANCE =
        CROSSCityApp.get().getSystemService(WifiManager.class);
  }
}
