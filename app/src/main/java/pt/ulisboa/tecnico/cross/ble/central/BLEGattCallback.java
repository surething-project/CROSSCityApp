package pt.ulisboa.tecnico.cross.ble.central;

import static pt.ulisboa.tecnico.cross.ble.central.BLEGattCallback.DISCONNECTION_CAUSE.ACCEPTED;
import static pt.ulisboa.tecnico.cross.ble.central.BLEGattCallback.DISCONNECTION_CAUSE.CLOSE;
import static pt.ulisboa.tecnico.cross.ble.central.BLEGattCallback.DISCONNECTION_CAUSE.ERROR;
import static pt.ulisboa.tecnico.cross.ble.central.BLEGattCallback.DISCONNECTION_CAUSE.REJECTED;
import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.CHALLENGE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulisboa.tecnico.cross.ble.BLEHelper;
import pt.ulisboa.tecnico.cross.peertopeer.EvidenceCollection;
import pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE;
import timber.log.Timber;

@SuppressLint("MissingPermission")
// Required permissions are requested before initializing this class.
public class BLEGattCallback extends BluetoothGattCallback {

  private static final int MTU = 515;

  private final Map<String, EvidenceCollection> evidenceCollectionInstances;
  private final Set<BluetoothGatt> connectedGatts;

  BLEGattCallback() {
    evidenceCollectionInstances = new ConcurrentHashMap<>();
    connectedGatts = ConcurrentHashMap.newKeySet();
  }

  @Override
  public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    if (status == BluetoothGatt.GATT_SUCCESS) {
      if (newState == BluetoothProfile.STATE_CONNECTED) connect(gatt);
      else if (newState == BluetoothProfile.STATE_DISCONNECTED) disconnect(gatt, CLOSE);
    } else {
      Timber.e("%s: Connection not successful: %s", gatt.getDevice(), status);
      disconnect(gatt, ERROR);
    }
  }

  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    if (status != BluetoothGatt.GATT_SUCCESS
        || gatt.getServices().stream()
            .map(BluetoothGattService::getUuid)
            .noneMatch(uuid -> uuid.equals(BLEHelper.get().SERVICE_UUID))) {
      Timber.e("%s: Service discovery failed: %s", gatt.getDevice(), status);
      disconnect(gatt, ERROR);
      return;
    }
    requestHigherMtu(gatt);
  }

  @Override
  public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
    Timber.d("%s: MTU granted: %s, status: %s", gatt.getDevice(), mtu, status);
    if (status != BluetoothGatt.GATT_SUCCESS || mtu < MTU) {
      disconnect(gatt, ERROR);
      return;
    }
    // Protocol start.
    if (enableResponseListening(gatt)) {
      EvidenceCollection evidenceCollection =
          evidenceCollectionInstances.get(gatt.getDevice().getAddress());
      sendRequest(gatt, evidenceCollection.getClaimSignature());
    } else {
      Timber.e("%s: Notifications were not successfully enabled.", gatt.getDevice());
      disconnect(gatt, ERROR);
    }
  }

  @Override
  public void onCharacteristicChanged(
      BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    if (!BLEHelper.get().RESPONSE_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
      Timber.e("%s: Unexpected characteristic: %s", gatt.getDevice(), characteristic.getUuid());
      return;
    }
    byte[] value = characteristic.getValue();
    if (value.length == 0) {
      Timber.e("%s: The claim got rejected.", gatt.getDevice());
      disconnect(gatt, REJECTED);
      return;
    }
    EvidenceCollection evidenceCollection =
        evidenceCollectionInstances.get(gatt.getDevice().getAddress());
    if (evidenceCollection == null) {
      Timber.w("%s: Unknown witness!", gatt.getDevice());
      disconnect(gatt, ERROR);
      return;
    }
    ACQUISITION_STAGE stage = evidenceCollection.getStage();
    byte[] request = null;
    switch (stage) {
      case PREPARE:
        request = evidenceCollection.getReady(value);
        break;
      case CHALLENGE:
        Boolean challengeResponse = evidenceCollection.getChallengeResponse(value[0] == 1);
        if (challengeResponse != null) request = new byte[] {(byte) (challengeResponse ? 1 : 0)};
        else evidenceCollection.setEncryptedSignedEndorsement(value);
        break;
      default:
        Timber.w("%s: Unexpected instance acquisition stage: %s", gatt.getDevice(), stage);
        return;
    }
    if (request != null) sendRequest(gatt, request);
    else disconnect(gatt, ACCEPTED); // Protocol completed.
  }

  /*********************
   * Auxiliary methods *
   *********************/

  private void connect(BluetoothGatt gatt) {
    connectedGatts.add(gatt);
    evidenceCollectionInstances.put(
        gatt.getDevice().getAddress(), new EvidenceCollection(gatt.getDevice().getAddress()));
    requestHighConnectionPriority(gatt);
    discoverServices(gatt);
    Timber.d("%s: Connected.", gatt.getDevice());
  }

  private void disconnect(BluetoothGatt gatt, DISCONNECTION_CAUSE cause) {
    Timber.w("%s: Disconnected.", gatt.getDevice());
    gatt.close();
    BLEScanner.get().connectionLock.unlockAsync(gatt.getDevice().getAddress());
    connectedGatts.remove(gatt);
    EvidenceCollection evidenceCollection =
        evidenceCollectionInstances.remove(gatt.getDevice().getAddress());
    if (cause == ERROR) BLEScanner.get().retry(gatt.getDevice(), false);
    if (evidenceCollection == null) return;
    if (cause == REJECTED && evidenceCollection.getStage() == CHALLENGE) {
      // Rejection for slow response to challenges
      BLEScanner.get().retry(gatt.getDevice(), true);
    }
  }

  private void requestHighConnectionPriority(BluetoothGatt gatt) {
    if (!gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)) {
      Timber.e("%s: High connection priority request failed.", gatt.getDevice());
      disconnect(gatt, ERROR);
    }
  }

  private void discoverServices(BluetoothGatt gatt) {
    if (!gatt.discoverServices()) {
      Timber.e("%s: Service discovery failed.", gatt.getDevice());
      disconnect(gatt, ERROR);
    }
  }

  private void requestHigherMtu(BluetoothGatt gatt) {
    if (!gatt.requestMtu(MTU)) {
      Timber.e("%s: Failed to request an MTU of %s.", gatt.getDevice(), MTU);
      disconnect(gatt, ERROR);
    }
  }

  private boolean enableResponseListening(BluetoothGatt gatt) {
    BluetoothGattCharacteristic responseCharacteristic =
        gatt.getService(BLEHelper.get().SERVICE_UUID)
            .getCharacteristic(BLEHelper.get().RESPONSE_CHARACTERISTIC_UUID);
    return gatt.setCharacteristicNotification(responseCharacteristic, true);
  }

  private void sendRequest(BluetoothGatt gatt, byte[] value) {
    BluetoothGattCharacteristic requestCharacteristic =
        gatt.getService(BLEHelper.get().SERVICE_UUID)
            .getCharacteristic(BLEHelper.get().REQUEST_CHARACTERISTIC_UUID);
    if (!requestCharacteristic.setValue(value)
        || !gatt.writeCharacteristic(requestCharacteristic)) {
      Timber.e("%s: Failed to send request: %s", gatt.getDevice(), Arrays.toString(value));
      disconnect(gatt, ERROR);
    }
  }

  void clear() {
    connectedGatts.forEach(gatt -> disconnect(gatt, CLOSE));
    connectedGatts.clear();
    evidenceCollectionInstances.clear();
  }

  enum DISCONNECTION_CAUSE {
    ACCEPTED,
    REJECTED,
    ERROR,
    CLOSE
  }
}
