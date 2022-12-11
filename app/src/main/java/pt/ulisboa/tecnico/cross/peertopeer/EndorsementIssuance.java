package pt.ulisboa.tecnico.cross.peertopeer;

import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.CHALLENGE;
import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.ENDORSED;
import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.PREPARE;
import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.READY;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.cross.account.CryptoManager;
import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.account.model.LoggedInUser;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.Endorsement;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.Prepare;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.SignedEndorsement;
import pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE;
import timber.log.Timber;

// Witness
// https://www.overleaf.com/read/hsgrgwmcmhnr
public class EndorsementIssuance {

  private final String prover;
  private final List<Long> responseTimes;
  private byte[] claimSignature;
  private BitSet vH, vC, vR;
  private int i; // Challenge iterator
  private long requestTime;
  private ACQUISITION_STAGE stage;

  public EndorsementIssuance(String prover) {
    this.prover = prover;
    responseTimes = new ArrayList<>();
    stage = PREPARE;
  }

  public byte[] getPrepare(byte[] claimSignature) {
    this.claimSignature = claimSignature;
    vH = PeerManager.get().randomValue();
    vC = PeerManager.get().randomValue();
    vR = new BitSet(PeerManager.get().N_CHALLENGE_ITERATIONS);
    i = 0;

    Prepare prepare = Prepare.newBuilder().setVH(ByteString.copyFrom(vH.toByteArray())).build();
    Timber.d("%s: Prepare: %s", prover, prepare.getVH());

    stage = READY;
    return prepare.toByteArray();
  }

  public boolean getChallenge() {
    if (stage == READY) stage = CHALLENGE;
    requestTime = System.nanoTime();
    return vC.get(i++);
  }

  public boolean isResponseTimeValid(long responseTime) {
    long elapsedTime = responseTime - requestTime;
    // Timber.d("Elapsed time: %s", elapsedTime);
    responseTimes.add(elapsedTime);

    boolean valid = sumResponseTimes() <= PeerManager.get().MAX_RESPONSE_TIME_SUM;
    if (!valid) {
      Timber.w(
          "Sum of response times exceeded. Current average of %s millis.", averageResponseMillis());
    }
    return valid;
  }

  public boolean setChallengeResponse(boolean challengeResponse) {
    vR.set(i - 1, challengeResponse);
    return i == PeerManager.get().N_CHALLENGE_ITERATIONS;
  }

  public byte[] getEndorsement() {
    Timber.d("Average response time of %s millis.", averageResponseMillis());

    LoggedInUser loggedInUser = LoginManager.get().getLoggedInUser().getValue();
    Instant instant = Instant.now();
    Endorsement endorsement =
        Endorsement.newBuilder()
            .setWitnessId(loggedInUser.getUsername())
            .setWitnessSessionId(loggedInUser.getSessionId())
            .setPoiId(PeerManager.get().getWaypoint().poi.id)
            .setTimestamp(
                Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build())
            .setVH(ByteString.copyFrom(vH.toByteArray()))
            .setVC(ByteString.copyFrom(vC.toByteArray()))
            .setVR(ByteString.copyFrom(vR.toByteArray()))
            .build();
    Timber.d("%s: Endorsement: %s", prover, endorsement);

    byte[] endorsementSignature =
        CryptoManager.get()
            .sign(
                ByteString.copyFrom(claimSignature)
                    .concat(endorsement.toByteString())
                    .toByteArray());
    SignedEndorsement signedEndorsement =
        SignedEndorsement.newBuilder()
            .setEndorsement(endorsement)
            .setWitnessSignature(ByteString.copyFrom(endorsementSignature))
            .build();
    byte[] encryptedSignedEndorsement =
        CryptoManager.get().encrypt(signedEndorsement.toByteArray());

    PeerManager.get().addEndorsementIssued(prover);
    stage = ENDORSED;
    return encryptedSignedEndorsement;
  }

  public ACQUISITION_STAGE getStage() {
    return stage;
  }

  public long averageResponseMillis() {
    long l = responseTimes.size() - PeerManager.get().N_WORST_TO_BE_DISCARDED;
    return l > 0L ? TimeUnit.NANOSECONDS.toMillis(sumResponseTimes()) / l : 0L;
  }

  private long sumResponseTimes() {
    long l = responseTimes.size() - PeerManager.get().N_WORST_TO_BE_DISCARDED;
    return l > 0L ? responseTimes.stream().sorted().limit(l).reduce(0L, Long::sum) : 0L;
  }
}
