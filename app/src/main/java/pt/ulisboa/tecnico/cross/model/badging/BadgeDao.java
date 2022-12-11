package pt.ulisboa.tecnico.cross.model.badging;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface BadgeDao extends EntityDao<Badge> {

  @Query("SELECT * FROM badge")
  List<Badge> getAll();
}
