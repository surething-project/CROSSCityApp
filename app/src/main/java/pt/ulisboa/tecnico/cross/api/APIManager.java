package pt.ulisboa.tecnico.cross.api;

import pt.ulisboa.tecnico.cross.api.badging.BadgingAPI;
import pt.ulisboa.tecnico.cross.api.dataset.DatasetAPI;
import pt.ulisboa.tecnico.cross.api.poi.POIAPI;
import pt.ulisboa.tecnico.cross.api.route.RouteAPI;
import pt.ulisboa.tecnico.cross.api.scoreboard.ScoreboardAPI;
import pt.ulisboa.tecnico.cross.api.trip.TripAPI;
import pt.ulisboa.tecnico.cross.api.user.UserAPI;
import retrofit2.Retrofit;

public class APIManager {

  private final DatasetAPI datasetAPI;
  private final POIAPI poiAPI;
  private final RouteAPI routeAPI;
  private final TripAPI tripAPI;
  private final UserAPI userAPI;
  private final ScoreboardAPI scoreboardAPI;
  private final BadgingAPI badgingAPI;

  private String jwt;

  public static APIManager get() {
    return APIManagerHolder.INSTANCE;
  }

  private APIManager() {
    Retrofit retrofit = APIClient.newRetrofit();
    datasetAPI = new DatasetAPI(retrofit);
    poiAPI = new POIAPI(retrofit);
    routeAPI = new RouteAPI(retrofit);
    tripAPI = new TripAPI(retrofit);
    userAPI = new UserAPI(retrofit);
    scoreboardAPI = new ScoreboardAPI(retrofit);
    badgingAPI = new BadgingAPI(retrofit);
  }

  public DatasetAPI getDatasetAPI() {
    return datasetAPI;
  }

  public POIAPI getPOIAPI() {
    return poiAPI;
  }

  public RouteAPI getRouteAPI() {
    return routeAPI;
  }

  public TripAPI getTripAPI() {
    return tripAPI;
  }

  public UserAPI getUserAPI() {
    return userAPI;
  }

  public ScoreboardAPI getScoreboardAPI() {
    return scoreboardAPI;
  }

  public BadgingAPI getBadgingAPI() {
    return badgingAPI;
  }

  public synchronized String getJwt() {
    return jwt;
  }

  public synchronized void setJwt(String jwt) {
    this.jwt = jwt;
  }

  private static class APIManagerHolder {
    private static final APIManager INSTANCE = new APIManager();
  }
}
