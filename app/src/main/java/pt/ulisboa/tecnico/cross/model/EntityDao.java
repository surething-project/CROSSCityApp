package pt.ulisboa.tecnico.cross.model;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

public interface EntityDao<E> {
  @Insert
  void insertAll(E... elements);

  @Update
  void updateAll(E... elements);

  @Delete
  void deleteAll(E... elements);
}
