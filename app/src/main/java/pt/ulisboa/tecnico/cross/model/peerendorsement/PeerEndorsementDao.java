package pt.ulisboa.tecnico.cross.model.peerendorsement;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface PeerEndorsementDao extends EntityDao<PeerEndorsement> {

  @Query("SELECT * FROM peer_endorsement WHERE visit_id IN (:visitIds)")
  List<PeerEndorsement> getAllByVisitId(String... visitIds);
}
