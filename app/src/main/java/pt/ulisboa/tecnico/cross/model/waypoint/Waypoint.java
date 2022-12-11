package pt.ulisboa.tecnico.cross.model.waypoint;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import pt.ulisboa.tecnico.cross.model.IdentifiableEntity;
import pt.ulisboa.tecnico.cross.model.poi.POI;
import pt.ulisboa.tecnico.cross.model.route.Route;

@Entity(
    tableName = "waypoint",
    indices = {@Index("route_id"), @Index("poi_id")},
    foreignKeys = {
      @ForeignKey(
          entity = Route.class,
          parentColumns = "id",
          childColumns = "route_id",
          onDelete = CASCADE),
      @ForeignKey(
          entity = POI.class,
          parentColumns = "id",
          childColumns = "poi_id",
          onDelete = CASCADE)
    })
public class Waypoint extends IdentifiableEntity {
  public int position;

  @NonNull
  @ColumnInfo(name = "route_id")
  public String routeId;

  @NonNull
  @ColumnInfo(name = "poi_id")
  public String poiId;

  @ColumnInfo(name = "stay_for_seconds")
  public long stayForSeconds;

  public Waypoint(
      @NonNull String id,
      int position,
      @NonNull String routeId,
      @NonNull String poiId,
      long stayForSeconds) {
    super(id);
    this.position = position;
    this.routeId = routeId;
    this.poiId = poiId;
    this.stayForSeconds = stayForSeconds;
  }
}
