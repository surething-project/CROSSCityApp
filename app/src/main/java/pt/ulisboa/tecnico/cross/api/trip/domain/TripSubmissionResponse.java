package pt.ulisboa.tecnico.cross.api.trip.domain;

import java.util.List;
import java.util.Map;

public class TripSubmissionResponse {

  public boolean completedTrip;
  public Map<String, Integer> visitVerificationStatus;
  public int awardedScore;
  public int awardedGems;
  public List<String> awardedBadges;

  public TripSubmissionResponse(
      boolean completedTrip,
      Map<String, Integer> visitVerificationStatus,
      int awardedScore,
      int awardedGems,
      List<String> awardedBadges) {
    this.completedTrip = completedTrip;
    this.visitVerificationStatus = visitVerificationStatus;
    this.awardedScore = awardedScore;
    this.awardedGems = awardedGems;
    this.awardedBadges = awardedBadges;
  }
}
