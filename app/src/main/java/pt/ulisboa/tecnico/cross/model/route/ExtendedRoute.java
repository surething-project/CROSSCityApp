package pt.ulisboa.tecnico.cross.model.route;

import androidx.room.Embedded;
import androidx.room.Relation;

import pt.ulisboa.tecnico.cross.model.trip.Trip;

public class ExtendedRoute {
  @Embedded public Route route;

  @Relation(parentColumn = "id", entityColumn = "route_id")
  public Trip trip;

  public boolean wasInitiated() {
    return trip != null;
  }

  public boolean wasCompleted() {
    return trip != null && trip.completed;
  }
}
