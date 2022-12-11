package pt.ulisboa.tecnico.cross.model.waypoint;

import androidx.room.Embedded;
import androidx.room.Relation;

import pt.ulisboa.tecnico.cross.model.poi.POI;
import pt.ulisboa.tecnico.cross.model.visit.Visit;

public class ExtendedWaypoint {
  @Embedded public Waypoint waypoint;

  @Relation(parentColumn = "poi_id", entityColumn = "id")
  public POI poi;

  @Relation(parentColumn = "poi_id", entityColumn = "poi_id")
  public Visit visit;

  public boolean wasVisitInitiated() {
    return visit != null;
  }

  public boolean wasVisitVerified() {
    return visit != null && visit.verified;
  }

  public boolean wasVisitRejected() {
    return visit != null && visit.rejected;
  }

  public boolean isVisitIncomplete() {
    return visit != null && visit.leaveMillis == 0;
  }
}
