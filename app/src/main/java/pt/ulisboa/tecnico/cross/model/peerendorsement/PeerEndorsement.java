package pt.ulisboa.tecnico.cross.model.peerendorsement;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import pt.ulisboa.tecnico.cross.model.IdentifiableEntity;
import pt.ulisboa.tecnico.cross.model.visit.Visit;

@Entity(
    tableName = "peer_endorsement",
    indices = @Index("visit_id"),
    foreignKeys =
        @ForeignKey(
            entity = Visit.class,
            parentColumns = "id",
            childColumns = "visit_id",
            onDelete = CASCADE))
public class PeerEndorsement extends IdentifiableEntity {
  @NonNull
  @ColumnInfo(name = "visit_id")
  public String visitId;

  @NonNull public byte[] endorsement;

  public PeerEndorsement(@NonNull String visitId, @NonNull byte[] endorsement) {
    this.visitId = visitId;
    this.endorsement = endorsement;
  }
}
