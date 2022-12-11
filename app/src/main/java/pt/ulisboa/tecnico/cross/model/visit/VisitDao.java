package pt.ulisboa.tecnico.cross.model.visit;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface VisitDao extends EntityDao<Visit> {

  @Query("SELECT * FROM visit")
  List<Visit> getAll();

  @Query("SELECT COUNT(*) FROM visit WHERE trip_id = :tripId AND verified = 1")
  int getNumberOfVerifiedVisits(String tripId);

  @Query("SELECT * FROM visit WHERE leave_millis > 0 AND verified = 0 AND rejected = 0")
  List<Visit> getUnsubmittedVisits();
}
