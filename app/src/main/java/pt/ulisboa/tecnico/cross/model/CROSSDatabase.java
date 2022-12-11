package pt.ulisboa.tecnico.cross.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pt.ulisboa.tecnico.cross.model.badging.Badge;
import pt.ulisboa.tecnico.cross.model.badging.BadgeDao;
import pt.ulisboa.tecnico.cross.model.peerendorsement.PeerEndorsement;
import pt.ulisboa.tecnico.cross.model.peerendorsement.PeerEndorsementDao;
import pt.ulisboa.tecnico.cross.model.poi.POI;
import pt.ulisboa.tecnico.cross.model.poi.POIDao;
import pt.ulisboa.tecnico.cross.model.route.Route;
import pt.ulisboa.tecnico.cross.model.route.RouteDao;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfileDao;
import pt.ulisboa.tecnico.cross.model.trip.Trip;
import pt.ulisboa.tecnico.cross.model.trip.TripDao;
import pt.ulisboa.tecnico.cross.model.visit.Visit;
import pt.ulisboa.tecnico.cross.model.visit.VisitDao;
import pt.ulisboa.tecnico.cross.model.waypoint.Waypoint;
import pt.ulisboa.tecnico.cross.model.waypoint.WaypointDao;
import pt.ulisboa.tecnico.cross.model.wifiapevidence.WiFiAPEvidence;
import pt.ulisboa.tecnico.cross.model.wifiapevidence.WiFiAPEvidenceDao;

@Database(
    entities = {
      Route.class,
      Waypoint.class,
      POI.class,
      Trip.class,
      Visit.class,
      WiFiAPEvidence.class,
      PeerEndorsement.class,
      ScoreboardProfile.class,
      Badge.class
    },
    version = 1)
@TypeConverters({Converters.class})
public abstract class CROSSDatabase extends RoomDatabase {
  public abstract RouteDao routeDao();
  public abstract WaypointDao waypointDao();
  public abstract POIDao poiDao();
  public abstract TripDao tripDao();
  public abstract VisitDao visitDao();
  public abstract WiFiAPEvidenceDao wiFiAPEvidenceDao();
  public abstract PeerEndorsementDao peerEndorsementDao();
  public abstract ScoreboardProfileDao scoreboardProfileDao();
  public abstract BadgeDao badgeDao();
}
