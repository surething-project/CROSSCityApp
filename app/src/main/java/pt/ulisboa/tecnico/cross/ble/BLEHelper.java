package pt.ulisboa.tecnico.cross.ble;

import android.bluetooth.BluetoothManager;

import java.util.UUID;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.ble.central.BLEScanner;
import pt.ulisboa.tecnico.cross.ble.peripheral.BLEAdvertiser;
import pt.ulisboa.tecnico.cross.ble.peripheral.BLEGattServer;
import pt.ulisboa.tecnico.cross.model.waypoint.ExtendedWaypoint;
import pt.ulisboa.tecnico.cross.peertopeer.PeerManager;
import timber.log.Timber;

public class BLEHelper {

  public final UUID SERVICE_UUID;
  public final UUID REQUEST_CHARACTERISTIC_UUID;
  public final UUID RESPONSE_CHARACTERISTIC_UUID;
  public final int MAX_ENDORSEMENT_ATTEMPTS;

  public static BLEHelper get() {
    return BLEHelperHolder.INSTANCE;
  }

  private BLEHelper() {
    SERVICE_UUID = UUID.fromString(CROSSCityApp.get().getString(R.string.service_uuid));
    REQUEST_CHARACTERISTIC_UUID =
        UUID.fromString(CROSSCityApp.get().getString(R.string.request_characteristic_uuid));
    RESPONSE_CHARACTERISTIC_UUID =
        UUID.fromString(CROSSCityApp.get().getString(R.string.response_characteristic_uuid));
    MAX_ENDORSEMENT_ATTEMPTS =
        Integer.parseInt(CROSSCityApp.get().getProperty("MAX_ENDORSEMENT_ATTEMPTS"));
  }

  void switchOn(ExtendedWaypoint waypoint) {
    if (getManager() == null) {
      Timber.e("Bluetooth manager could not be initialized.");
      return;
    }
    PeerManager.get().initialize(waypoint);

    // Prover
    BLEScanner.get().startScan();
    // Witness
    BLEGattServer.get().open();
    BLEAdvertiser.get().startAdvertisingSet();
  }

  void switchOff() {
    // Prover
    BLEScanner.get().stopScan();
    // Witness
    BLEAdvertiser.get().stopAdvertisingSet();
    BLEGattServer.get().close();

    PeerManager.get().finish();
  }

  public BluetoothManager getManager() {
    return BluetoothManagerHolder.INSTANCE;
  }

  private static class BLEHelperHolder {
    private static final BLEHelper INSTANCE = new BLEHelper();
  }

  private static class BluetoothManagerHolder {
    private static final BluetoothManager INSTANCE =
        CROSSCityApp.get().getSystemService(BluetoothManager.class);
  }
}
