package pt.ulisboa.tecnico.cross.ble.central;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.ble.BLEHelper;
import pt.ulisboa.tecnico.cross.utils.QueuingLock;
import timber.log.Timber;

// https://developer.android.com/guide/topics/connectivity/bluetooth/find-ble-devices
@SuppressLint("MissingPermission")
// Required permissions are requested before initializing this class.
public class BLEScanner {

  private final BluetoothLeScanner scanner;
  private final List<ScanFilter> scanFilters;
  private final ScanSettings scanSettings;
  private final BLEGattCallback gattCallback;
  private final long millisBetweenScans;
  private final long scanDurationMillis;
  private final Handler periodicScanningHandler;
  private final Set<Long> advertisingIdFilter;
  private final Map<String, Integer> rejectionsOfDevices;
  private final Map<String, Integer> attemptsOfDevices;
  private final Handler attemptsHandler;

  private boolean scanning;

  final QueuingLock connectionLock = new QueuingLock();

  public static BLEScanner get() {
    return BLEScannerHolder.INSTANCE;
  }

  private BLEScanner() {
    scanner = BLEHelper.get().getManager().getAdapter().getBluetoothLeScanner();
    scanFilters =
        Collections.singletonList(
            new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(BLEHelper.get().SERVICE_UUID))
                .build());
    // https://developer.android.com/reference/android/bluetooth/le/ScanSettings
    scanSettings =
        new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    gattCallback = new BLEGattCallback();
    millisBetweenScans =
        TimeUnit.SECONDS.toMillis(
            Long.parseLong(CROSSCityApp.get().getProperty("BLE_SCAN_PERIOD_SECONDS")));
    scanDurationMillis =
        TimeUnit.SECONDS.toMillis(
            Long.parseLong(CROSSCityApp.get().getProperty("BLE_SCAN_DURATION_SECONDS")));
    periodicScanningHandler = new Handler(Looper.getMainLooper());
    advertisingIdFilter = ConcurrentHashMap.newKeySet();
    rejectionsOfDevices = new ConcurrentHashMap<>();
    attemptsOfDevices = new ConcurrentHashMap<>();
    attemptsHandler = new Handler(Looper.getMainLooper());
    scanning = false;
  }

  private final ScanCallback scanCallback =
      new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
          long advertisingId =
              ByteBuffer.wrap(
                      result
                          .getScanRecord()
                          .getServiceData()
                          .get(new ParcelUuid(BLEHelper.get().SERVICE_UUID)))
                  .getLong();
          if (!advertisingIdFilter.add(advertisingId)) return;

          Timber.d("%s: Found", result.getDevice());
          connect(result.getDevice());
        }

        @Override
        public void onScanFailed(int errorCode) {
          Timber.d("BLE scan failed: %s", errorCode);
        }
      };

  public synchronized void startScan() {
    if (scanning) return;
    scanning = true;
    startPeriodicScanning();
  }

  private synchronized void startPeriodicScanning() {
    if (!scanning) return;
    if (connectionLock.locked()) {
      periodicScanningHandler.postDelayed(this::startPeriodicScanning, millisBetweenScans);
      return;
    }
    scanner.startScan(scanFilters, scanSettings, scanCallback);
    periodicScanningHandler.postDelayed(this::stopAndRescheduleScan, scanDurationMillis);
  }

  private synchronized void stopAndRescheduleScan() {
    if (!scanning) return;
    scanner.stopScan(scanCallback);
    periodicScanningHandler.postDelayed(this::startPeriodicScanning, millisBetweenScans);
  }

  public synchronized void stopScan() {
    if (!scanning) return;
    scanner.stopScan(scanCallback);
    periodicScanningHandler.removeCallbacksAndMessages(null);
    scanning = false;
    attemptsHandler.removeCallbacksAndMessages(null);
    gattCallback.clear();
    advertisingIdFilter.clear();
    rejectionsOfDevices.clear();
    attemptsOfDevices.clear();
  }

  /*********************
   * Auxiliary methods *
   *********************/

  private void connect(BluetoothDevice device) {
    connectionLock.lockAsync(
        device.getAddress(),
        () ->
            device.connectGatt(
                CROSSCityApp.get(), false, gattCallback, BluetoothDevice.TRANSPORT_LE));
  }

  void retry(BluetoothDevice device, boolean rejection) {
    if (!scanning) return;
    long delaySeconds = 1;
    if (rejection) {
      attemptsOfDevices.remove(device.getAddress());
      int numberOfRejections = rejectionsOfDevices.getOrDefault(device.getAddress(), 0) + 1;
      if (numberOfRejections > BLEHelper.get().MAX_ENDORSEMENT_ATTEMPTS) return;
      rejectionsOfDevices.put(device.getAddress(), numberOfRejections);
    } else {
      int numberOfAttempts = attemptsOfDevices.getOrDefault(device.getAddress(), 0) + 1;
      attemptsOfDevices.put(device.getAddress(), numberOfAttempts);
      delaySeconds = numberOfAttempts;
    }
    Timber.d("%s: Retrying in %s second(s)...", device, delaySeconds);
    attemptsHandler.postDelayed(() -> connect(device), TimeUnit.SECONDS.toMillis(delaySeconds));
  }

  private static class BLEScannerHolder {
    private static final BLEScanner INSTANCE = new BLEScanner();
  }
}
