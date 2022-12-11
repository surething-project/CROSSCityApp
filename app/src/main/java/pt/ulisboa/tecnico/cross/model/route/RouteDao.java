package pt.ulisboa.tecnico.cross.model.route;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface RouteDao extends EntityDao<Route> {

  @Query("SELECT * FROM route")
  List<Route> getAll();

  @Transaction
  @Query("SELECT * FROM route")
  List<ExtendedRoute> getExtendedRoutes();
}
