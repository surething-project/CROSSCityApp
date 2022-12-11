package pt.ulisboa.tecnico.cross.model.poi;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface POIDao extends EntityDao<POI> {

  @Query("SELECT * FROM poi")
  List<POI> getAll();
}
