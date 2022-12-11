package pt.ulisboa.tecnico.cross.model.wifiapevidence;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pt.ulisboa.tecnico.cross.model.EntityDao;

@Dao
public interface WiFiAPEvidenceDao extends EntityDao<WiFiAPEvidence> {

  @Query("SELECT * FROM wifiap_evidence WHERE visit_id IN (:visitIds)")
  List<WiFiAPEvidence> getAllByVisitId(String... visitIds);
}
