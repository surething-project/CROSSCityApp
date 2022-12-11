package pt.ulisboa.tecnico.cross.ble.peripheral;

import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.ENDORSED;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulisboa.tecnico.cross.ble.BLEHelper;
import pt.ulisboa.tecnico.cross.peertopeer.EndorsementIssuance;
import pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE;
import timber.log.Timber;

public class BLEGattServerCallback extends BluetoothGattServerCallback {

  private final Map<String, EndorsementIssuance> endorsementIssuanceInstances;
  private final Map<String, Integer> rejectionsOfDevices;
  private final Set<String> endorsedDevices;

  BLEGattServerCallback() {
    endorsementIssuanceInstances = new ConcurrentHashMap<>();
    rejectionsOfDevices = new ConcurrentHashMap<>();
    endorsedDevices = ConcurrentHashMap.newKeySet();
  }

  @Override
  public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
    if (status == BluetoothGatt.GATT_SUCCESS) {
      if (newState == BluetoothProfile.STATE_CONNECTED) connect(device);
      else if (newState == BluetoothProfile.STATE_DISCONNECTED) disconnect(device);
    } else {
      Timber.e("%s: Connection not successful: %s", device, status);
      disconnect(device);
    }
  }

  @Override
  public void onCharacteristicWriteRequest(
      BluetoothDevice device,
      int requestId,
      BluetoothGattCharacteristic characteristic,
      boolean preparedWrite,
      boolean responseNeeded,
      int offset,
      byte[] value) {
    long responseTime = System.nanoTime();
    if (!BLEHelper.get().REQUEST_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
      Timber.d("%s: Unknown characteristic: %s", device, characteristic.getUuid());
      return;
    }
    EndorsementIssuance endorsementIssuance =
        endorsementIssuanceInstances.getOrDefault(device.getAddress(), null);
    if (endorsementIssuance == null) {
      Timber.w("%s: Unknown prover!", device);
      return;
    }
    ACQUISITION_STAGE stage = endorsementIssuance.getStage();
    byte[] response;
    switch (stage) {
      case PREPARE:
        response =
            rejectionsOfDevices.getOrDefault(device.getAddress(), 0)
                        <= BLEHelper.get().MAX_ENDORSEMENT_ATTEMPTS
                    && !endorsedDevices.contains(device.getAddress())
                ? endorsementIssuance.getPrepare(value)
                : new byte[] {}; // Auto rejecting
        break;
      case READY:
        response = new byte[] {(byte) (endorsementIssuance.getChallenge() ? 1 : 0)};
        break;
      case CHALLENGE:
        response =
            !endorsementIssuance.isResponseTimeValid(responseTime)
                ? new byte[] {} // Rejected
                : endorsementIssuance.setChallengeResponse(value[0] == 1)
                    ? endorsementIssuance.getEndorsement()
                    : new byte[] {(byte) (endorsementIssuance.getChallenge() ? 1 : 0)};
        break;
      default:
        Timber.w("%s: Unexpected instance acquisition stage: %s", device, stage);
        return;
    }
    BLEGattServer.get().sendResponse(device, response);
    if (response.length == 0) {
      Timber.e("%s: The claim got rejected.", device);
      int numberOfRejections = rejectionsOfDevices.getOrDefault(device.getAddress(), 0) + 1;
      rejectionsOfDevices.put(device.getAddress(), numberOfRejections);
    }
  }

  /*********************
   * Auxiliary methods *
   *********************/

  private void connect(BluetoothDevice device) {
    endorsementIssuanceInstances.put(
        device.getAddress(), new EndorsementIssuance(device.getAddress()));
    Timber.d("%s: Connected.", device.getAddress());
  }

  private void disconnect(BluetoothDevice device) {
    Timber.w("%s: Disconnected.", device.getAddress());
    EndorsementIssuance endorsementIssuance =
        endorsementIssuanceInstances.remove(device.getAddress());
    if (endorsementIssuance == null) return;
    if (endorsementIssuance.getStage() == ENDORSED) endorsedDevices.add(device.getAddress());
  }

  void clear() {
    endorsedDevices.clear();
    rejectionsOfDevices.clear();
    endorsementIssuanceInstances.clear();
  }
}
