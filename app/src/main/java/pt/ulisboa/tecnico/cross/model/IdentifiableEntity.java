package pt.ulisboa.tecnico.cross.model;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

import java.util.Objects;
import java.util.UUID;

public abstract class IdentifiableEntity {

  @PrimaryKey @NonNull public String id;

  public IdentifiableEntity() {
    this.id = UUID.randomUUID().toString();
  }

  public IdentifiableEntity(@NonNull String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IdentifiableEntity that = (IdentifiableEntity) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
