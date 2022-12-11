package pt.ulisboa.tecnico.cross.ble.peripheral;

import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;

import java.nio.ByteBuffer;
import java.util.UUID;

import pt.ulisboa.tecnico.cross.ble.BLEHelper;
import timber.log.Timber;

// https://source.android.com/devices/bluetooth/ble_advertising
@SuppressLint("MissingPermission")
// Required permissions are requested before initializing this class.
public class BLEAdvertiser {

  private final BluetoothLeAdvertiser advertiser;
  private final AdvertisingSetParameters advertisingSetParameters;
  private final AdvertiseData advertiseData;

  public static BLEAdvertiser get() {
    return BLEAdvertiserHolder.INSTANCE;
  }

  private BLEAdvertiser() {
    advertiser = BLEHelper.get().getManager().getAdapter().getBluetoothLeAdvertiser();
    // https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetParameters
    advertisingSetParameters =
        new AdvertisingSetParameters.Builder()
            .setLegacyMode(true)
            .setScannable(true)
            .setConnectable(true)
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .build();
    advertiseData =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(new ParcelUuid(BLEHelper.get().SERVICE_UUID))
            .build();
  }

  private final AdvertisingSetCallback advertisingSetCallback =
      new AdvertisingSetCallback() {
        @Override
        public void onAdvertisingSetStarted(
            AdvertisingSet advertisingSet, int txPower, int status) {
          Timber.d("BLE advertisement started: %s", status);
        }

        @Override
        public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
          Timber.d("BLE advertisement stopped.");
        }
      };

  public void startAdvertisingSet() {
    long advertisingId = UUID.randomUUID().getLeastSignificantBits();
    AdvertiseData scanResponse =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceData(
                new ParcelUuid(BLEHelper.get().SERVICE_UUID),
                ByteBuffer.allocate(Long.BYTES).putLong(advertisingId).array())
            .build();
    advertiser.startAdvertisingSet(
        advertisingSetParameters, advertiseData, scanResponse, null, null, advertisingSetCallback);
  }

  public void stopAdvertisingSet() {
    advertiser.stopAdvertisingSet(advertisingSetCallback);
  }

  private static class BLEAdvertiserHolder {
    private static final BLEAdvertiser INSTANCE = new BLEAdvertiser();
  }
}
