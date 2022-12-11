package pt.ulisboa.tecnico.cross.wifi;

import static android.net.wifi.WifiManager.EXTRA_RESULTS_UPDATED;
import static android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import timber.log.Timber;

// https://developer.android.com/guide/topics/connectivity/wifi-scan
public class WiFiScanner {

  private final long millisBetweenScans;
  private final Handler periodicScanningHandler;
  private final List<WiFiScanResult> scanResults;
  private final MutableLiveData<Integer> numberOfScans;

  private boolean scanning;

  public static WiFiScanner get() {
    return WiFiScannerHolder.INSTANCE;
  }

  private WiFiScanner() {
    millisBetweenScans =
        TimeUnit.SECONDS.toMillis(
            Long.parseLong(CROSSCityApp.get().getProperty("WIFI_SCAN_PERIOD_SECONDS")));
    periodicScanningHandler = new Handler(Looper.getMainLooper());
    scanResults = Collections.synchronizedList(new ArrayList<>());
    numberOfScans = new MutableLiveData<>(0);
    scanning = false;
  }

  private final BroadcastReceiver mBroadcastReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          synchronized (WiFiScanner.this) {
            if (!scanning) return;
          }
          boolean successful = intent.getBooleanExtra(EXTRA_RESULTS_UPDATED, false);
          if (!successful) {
            Timber.w("Wi-Fi scanning was not successful.");
            return;
          }
          scanResults.addAll(
              WiFiHelper.get().getManager().getScanResults().stream()
                  .map(WiFiScanResult::new)
                  .collect(Collectors.toSet()));
          // Triggers a UI update of the number of scans performed and evidence collected.
          numberOfScans.setValue(numberOfScans.getValue() + 1);
          Timber.d("Wi-Fi scan results: %s", scanResults);
        }
      };

  synchronized void startScan() {
    if (scanning) return;
    CROSSCityApp.get()
        .registerReceiver(mBroadcastReceiver, new IntentFilter(SCAN_RESULTS_AVAILABLE_ACTION));
    scanning = true;
    startPeriodicScanning();
  }

  private synchronized void startPeriodicScanning() {
    if (!scanning) return;
    WiFiHelper.get().getManager().startScan();
    periodicScanningHandler.postDelayed(this::startPeriodicScanning, millisBetweenScans);
  }

  synchronized void stopScan() {
    if (!scanning) return;
    CROSSCityApp.get().unregisterReceiver(mBroadcastReceiver);
    periodicScanningHandler.removeCallbacksAndMessages(null);
    scanning = false;
    scanResults.clear();
    numberOfScans.setValue(0);
  }

  public LiveData<Integer> getNumberOfScans() {
    return numberOfScans;
  }

  public long getNumberOfScanResults() {
    return scanResults.stream().distinct().count();
  }

  public Set<WiFiScanResult> collectScanResults() {
    return new HashSet<>(scanResults);
  }

  private static class WiFiScannerHolder {
    private static final WiFiScanner INSTANCE = new WiFiScanner();
  }
}
