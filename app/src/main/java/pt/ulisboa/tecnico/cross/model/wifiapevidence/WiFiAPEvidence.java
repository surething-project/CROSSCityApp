package pt.ulisboa.tecnico.cross.model.wifiapevidence;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import pt.ulisboa.tecnico.cross.model.IdentifiableEntity;
import pt.ulisboa.tecnico.cross.model.visit.Visit;

@Entity(
    tableName = "wifiap_evidence",
    indices = @Index("visit_id"),
    foreignKeys =
        @ForeignKey(
            entity = Visit.class,
            parentColumns = "id",
            childColumns = "visit_id",
            onDelete = CASCADE))
public class WiFiAPEvidence extends IdentifiableEntity {
  @NonNull
  @ColumnInfo(name = "visit_id")
  public String visitId;

  @NonNull
  @ColumnInfo(name = "bssid")
  public String bssid;

  @NonNull
  @ColumnInfo(name = "ssid")
  public String ssid;

  @ColumnInfo(name = "sighting_millis")
  public long sightingMillis;

  public WiFiAPEvidence(
      @NonNull String visitId, @NonNull String bssid, @NonNull String ssid, long sightingMillis) {
    this.visitId = visitId;
    this.bssid = bssid;
    this.ssid = ssid;
    this.sightingMillis = sightingMillis;
  }
}
