package pt.ulisboa.tecnico.cross.peertopeer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.security.SecureRandom;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.model.waypoint.ExtendedWaypoint;

// https://www.overleaf.com/read/hsgrgwmcmhnr
public class PeerManager {

  public final int N_CHALLENGE_ITERATIONS;
  public final int N_WORST_TO_BE_DISCARDED;
  public final long MAX_RESPONSE_TIME_SUM;

  private final SecureRandom random;

  private ExtendedWaypoint waypoint;
  private BitSet vA;
  private BitSet vB;
  private final Map<String, byte[]> evidenceCollected;
  private final Set<String> endorsementsIssued;
  private final MutableLiveData<Integer> numberOfEvidenceCollected;
  private final MutableLiveData<Integer> numberOfEndorsementsIssued;

  public static PeerManager get() {
    return PeerHelperHolder.INSTANCE;
  }

  private PeerManager() {
    N_CHALLENGE_ITERATIONS =
        Integer.parseInt(CROSSCityApp.get().getProperty("N_CHALLENGE_ITERATIONS"));
    N_WORST_TO_BE_DISCARDED =
        Integer.parseInt(CROSSCityApp.get().getProperty("N_WORST_TO_BE_DISCARDED"));
    MAX_RESPONSE_TIME_SUM =
        Long.parseLong(CROSSCityApp.get().getProperty("MAX_AVERAGE_RESPONSE_NANOS"))
            * (N_CHALLENGE_ITERATIONS - N_WORST_TO_BE_DISCARDED);
    random = new SecureRandom();
    evidenceCollected = new ConcurrentHashMap<>();
    endorsementsIssued = ConcurrentHashMap.newKeySet();
    numberOfEvidenceCollected = new MutableLiveData<>(0);
    numberOfEndorsementsIssued = new MutableLiveData<>(0);
  }

  public void initialize(ExtendedWaypoint waypoint) {
    this.waypoint = waypoint;
    vA = PeerManager.get().randomValue();
    vB = PeerManager.get().randomValue();
  }

  public void finish() {
    waypoint = null;
    vA = null;
    vB = null;
    evidenceCollected.clear();
    endorsementsIssued.clear();
    numberOfEvidenceCollected.setValue(0);
    numberOfEndorsementsIssued.setValue(0);
  }

  public void addEvidenceCollected(String witness, byte[] endorsement) {
    evidenceCollected.put(witness, endorsement);
    numberOfEvidenceCollected.postValue(evidenceCollected.size());
  }

  public void addEndorsementIssued(String prover) {
    endorsementsIssued.add(prover);
    numberOfEndorsementsIssued.postValue(endorsementsIssued.size());
  }

  public Collection<byte[]> collectEndorsements() {
    return evidenceCollected.values();
  }

  public LiveData<Integer> getNumberOfEvidenceCollected() {
    return numberOfEvidenceCollected;
  }

  public LiveData<Integer> getNumberOfEndorsementsIssued() {
    return numberOfEndorsementsIssued;
  }

  /*********************
   * Auxiliary methods *
   *********************/

  ExtendedWaypoint getWaypoint() {
    return waypoint;
  }

  BitSet getVA() {
    return vA;
  }

  BitSet getVB() {
    return vB;
  }

  BitSet randomValue() {
    BitSet value = new BitSet(N_CHALLENGE_ITERATIONS);
    for (int i = 0; i < N_CHALLENGE_ITERATIONS; i++) value.set(i, random.nextBoolean());
    return value;
  }

  private static class PeerHelperHolder {
    private static final PeerManager INSTANCE = new PeerManager();
  }

  public enum ACQUISITION_STAGE {
    PREPARE,
    READY,
    CHALLENGE,
    ENDORSED
  }
}
