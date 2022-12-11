package pt.ulisboa.tecnico.cross.peertopeer;

import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.CHALLENGE;
import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.ENDORSED;
import static pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE.PREPARE;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.util.BitSet;

import pt.ulisboa.tecnico.cross.account.CryptoManager;
import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.account.model.LoggedInUser;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.Claim;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.PeerEndorsement;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.Prepare;
import pt.ulisboa.tecnico.cross.peertopeer.PeerEndorsementAcquisition.SignedClaim;
import pt.ulisboa.tecnico.cross.peertopeer.PeerManager.ACQUISITION_STAGE;
import timber.log.Timber;

// Prover
// https://www.overleaf.com/read/hsgrgwmcmhnr
public class EvidenceCollection {

  private final String witness;
  private SignedClaim signedClaim;
  private BitSet vZ;
  private int i; // Challenge iterator
  private ACQUISITION_STAGE stage;

  public EvidenceCollection(String witness) {
    this.witness = witness;
  }

  public byte[] getClaimSignature() {
    LoggedInUser loggedInUser = LoginManager.get().getLoggedInUser().getValue();
    Instant instant = Instant.now();
    Claim claim =
        Claim.newBuilder()
            .setProverId(loggedInUser.getUsername())
            .setProverSessionId(loggedInUser.getSessionId())
            .setPoiId(PeerManager.get().getWaypoint().poi.id)
            .setTimestamp(
                Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build())
            .setVA(ByteString.copyFrom(PeerManager.get().getVA().toByteArray()))
            .setVB(ByteString.copyFrom(PeerManager.get().getVB().toByteArray()))
            .build();
    Timber.d("%s: Claim: %s", witness, claim);

    byte[] claimSignature = CryptoManager.get().sign(claim.toByteArray());
    signedClaim =
        SignedClaim.newBuilder()
            .setClaim(claim)
            .setProverSignature(ByteString.copyFrom(claimSignature))
            .build();

    stage = PREPARE;
    return claimSignature;
  }

  public byte[] getReady(byte[] prepareBytes) {
    Prepare prepare;
    try {
      prepare = Prepare.parseFrom(prepareBytes);
      Timber.d("%s: Prepare: %s", witness, prepare.getVH());
    } catch (InvalidProtocolBufferException e) {
      Timber.w(e, "%s: Prepare parsing failed.", witness);
      return null;
    }

    vZ = BitSet.valueOf(prepare.getVH().toByteArray());
    vZ.xor(PeerManager.get().getVB());
    i = 0;

    stage = CHALLENGE;
    return new byte[] {0};
  }

  public Boolean getChallengeResponse(boolean challenge) {
    if (i >= PeerManager.get().N_CHALLENGE_ITERATIONS) {
      Timber.d("%s: Challenge completed!", witness);
      return null;
    }
    return (challenge ? vZ : PeerManager.get().getVA()).get(i++);
  }

  public void setEncryptedSignedEndorsement(byte[] encryptedSignedEndorsement) {
    Timber.d("%s: Endorsement received! Size: %s", witness, encryptedSignedEndorsement.length);
    PeerEndorsement endorsement =
        PeerEndorsement.newBuilder()
            .setSignedClaim(signedClaim)
            .setEncryptedSignedEndorsement(ByteString.copyFrom(encryptedSignedEndorsement))
            .build();

    PeerManager.get().addEvidenceCollected(witness, endorsement.toByteArray());
    stage = ENDORSED;
  }

  public ACQUISITION_STAGE getStage() {
    return stage;
  }
}
