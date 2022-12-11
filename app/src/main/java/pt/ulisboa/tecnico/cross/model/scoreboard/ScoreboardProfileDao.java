package pt.ulisboa.tecnico.cross.model.scoreboard;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface ScoreboardProfileDao extends EntityDao<ScoreboardProfile> {
  @Query("SELECT * FROM scoreboard")
  List<ScoreboardProfile> getAll();

  @Query("SELECT * FROM scoreboard WHERE username = :username")
  List<ScoreboardProfile> getAll(String username);

  @Query("DELETE FROM scoreboard")
  void delete();
}
