package pt.ulisboa.tecnico.cross.model.waypoint;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface WaypointDao extends EntityDao<Waypoint> {

  @Query("SELECT * FROM waypoint")
  List<Waypoint> getAll();

  @Query("SELECT COUNT(*) FROM waypoint WHERE route_id = :routeId")
  int getNumberOfWaypoints(String routeId);

  @Transaction
  @Query("SELECT * FROM waypoint WHERE route_id = :routeId")
  List<ExtendedWaypoint> getExtendedWaypoints(String routeId);
}
