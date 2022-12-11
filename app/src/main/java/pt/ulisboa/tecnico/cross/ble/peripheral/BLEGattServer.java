package pt.ulisboa.tecnico.cross.ble.peripheral;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;

import java.util.Arrays;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.ble.BLEHelper;
import timber.log.Timber;

// https://github.com/androidthings/sample-bluetooth-le-gattserver
@SuppressLint("MissingPermission")
// Required permissions are requested before initializing this class.
public class BLEGattServer {

  private final BLEGattServerCallback gattServerCallback;

  private BluetoothGattServer gattServer;

  public static BLEGattServer get() {
    return BLEGattServerHolder.INSTANCE;
  }

  private BLEGattServer() {
    gattServerCallback = new BLEGattServerCallback();
  }

  public synchronized void open() {
    if (gattServer != null) return;
    gattServer =
        BLEHelper.get().getManager().openGattServer(CROSSCityApp.get(), gattServerCallback);
    gattServer.addService(buildService());
  }

  public synchronized void close() {
    if (gattServer != null) {
      gattServer.close();
      gattServerCallback.clear();
      gattServer = null;
    }
  }

  /*********************
   * Auxiliary methods *
   *********************/

  void sendResponse(BluetoothDevice device, byte[] value) {
    BluetoothGattCharacteristic responseCharacteristic =
        gattServer
            .getService(BLEHelper.get().SERVICE_UUID)
            .getCharacteristic(BLEHelper.get().RESPONSE_CHARACTERISTIC_UUID);
    if (!responseCharacteristic.setValue(value)
        || !gattServer.notifyCharacteristicChanged(device, responseCharacteristic, true)) {
      Timber.e("%s: Failed to send response: %s", device, Arrays.toString(value));
    }
  }

  private static BluetoothGattService buildService() {
    BluetoothGattService service =
        new BluetoothGattService(
            BLEHelper.get().SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

    BluetoothGattCharacteristic requestCharacteristic =
        new BluetoothGattCharacteristic(
            BLEHelper.get().REQUEST_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE);
    service.addCharacteristic(requestCharacteristic);

    BluetoothGattCharacteristic responseCharacteristic =
        new BluetoothGattCharacteristic(
            BLEHelper.get().RESPONSE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            0);
    service.addCharacteristic(responseCharacteristic);

    return service;
  }

  private static class BLEGattServerHolder {
    private static final BLEGattServer INSTANCE = new BLEGattServer();
  }
}
