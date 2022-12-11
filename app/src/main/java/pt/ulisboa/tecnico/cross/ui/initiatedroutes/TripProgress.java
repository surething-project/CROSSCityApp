package pt.ulisboa.tecnico.cross.ui.initiatedroutes;

public class TripProgress {

  private final int numberOfVisits;
  private final int numberOfWaypoints;

  public TripProgress(int numberOfVisits, int numberOfWaypoints) {
    this.numberOfVisits = numberOfVisits;
    this.numberOfWaypoints = numberOfWaypoints;
  }

  public int getNumberOfVisits() {
    return numberOfVisits;
  }

  public int getNumberOfWaypoints() {
    return numberOfWaypoints;
  }

  public int getProgress() {
    return 100 * numberOfVisits / numberOfWaypoints;
  }
}
