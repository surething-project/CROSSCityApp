package pt.ulisboa.tecnico.cross.model.visit;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import pt.ulisboa.tecnico.cross.model.IdentifiableEntity;
import pt.ulisboa.tecnico.cross.model.poi.POI;
import pt.ulisboa.tecnico.cross.model.trip.Trip;

@Entity(
    tableName = "visit",
    indices = {
      @Index("trip_id"),
      @Index("poi_id"),
      @Index(
          value = {"trip_id", "poi_id"},
          unique = true)
    },
    foreignKeys = {
      @ForeignKey(
          entity = Trip.class,
          parentColumns = "id",
          childColumns = "trip_id",
          onDelete = CASCADE),
      @ForeignKey(
          entity = POI.class,
          parentColumns = "id",
          childColumns = "poi_id",
          onDelete = CASCADE)
    })
public class Visit extends IdentifiableEntity {
  @NonNull
  @ColumnInfo(name = "trip_id")
  public String tripId;

  @NonNull
  @ColumnInfo(name = "poi_id")
  public String poiId;

  @ColumnInfo(name = "entry_millis")
  public long entryMillis;

  @ColumnInfo(name = "leave_millis")
  public long leaveMillis;

  public boolean verified;

  public boolean rejected;

  public Visit(
      @NonNull String id,
      @NonNull String tripId,
      @NonNull String poiId,
      long entryMillis,
      long leaveMillis,
      boolean verified,
      boolean rejected) {
    super(id);
    this.tripId = tripId;
    this.poiId = poiId;
    this.entryMillis = entryMillis;
    this.leaveMillis = leaveMillis;
    this.verified = verified;
    this.rejected = rejected;
  }

  @Ignore
  public Visit(
      @NonNull String tripId,
      @NonNull String poiId,
      long entryMillis,
      long leaveMillis,
      boolean verified,
      boolean rejected) {
    this.tripId = tripId;
    this.poiId = poiId;
    this.entryMillis = entryMillis;
    this.leaveMillis = leaveMillis;
    this.verified = verified;
    this.rejected = rejected;
  }
}
