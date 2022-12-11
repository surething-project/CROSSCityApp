package pt.ulisboa.tecnico.cross.model.trip;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import pt.ulisboa.tecnico.cross.model.IdentifiableEntity;
import pt.ulisboa.tecnico.cross.model.route.Route;

@Entity(
    tableName = "trip",
    indices = @Index(value = "route_id", unique = true),
    foreignKeys =
        @ForeignKey(
            entity = Route.class,
            parentColumns = "id",
            childColumns = "route_id",
            onDelete = CASCADE))
public class Trip extends IdentifiableEntity {
  @NonNull
  @ColumnInfo(name = "route_id")
  public String routeId;

  public boolean completed;

  public Trip(@NonNull String id, @NonNull String routeId, boolean completed) {
    super(id);
    this.routeId = routeId;
    this.completed = completed;
  }

  @Ignore
  public Trip(@NonNull String routeId, boolean completed) {
    this.routeId = routeId;
    this.completed = completed;
  }
}
