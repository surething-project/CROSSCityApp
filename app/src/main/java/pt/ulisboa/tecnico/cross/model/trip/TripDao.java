package pt.ulisboa.tecnico.cross.model.trip;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface TripDao extends EntityDao<Trip> {

  @Query("SELECT * FROM trip")
  List<Trip> getAll();

  @Query("SELECT * FROM trip WHERE id IN (:ids)")
  List<Trip> getAllById(String... ids);
}
