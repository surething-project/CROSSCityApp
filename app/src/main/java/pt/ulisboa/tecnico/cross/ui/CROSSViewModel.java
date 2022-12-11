package pt.ulisboa.tecnico.cross.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.account.KeyValueDataManager;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.model.badging.Badge;
import pt.ulisboa.tecnico.cross.model.route.ExtendedRoute;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.model.waypoint.ExtendedWaypoint;
import timber.log.Timber;

public class CROSSViewModel extends ViewModel {

  private final MutableLiveData<Integer> selectedStrategy;
  private final MutableLiveData<Boolean> visitAllowed;
  private final MutableLiveData<List<ExtendedRoute>> routeCollection;
  private final MutableLiveData<ExtendedRoute> foregroundRoute;
  private final MutableLiveData<List<ExtendedWaypoint>> waypointCollection;
  private final MutableLiveData<ExtendedWaypoint> foregroundWaypoint;
  private final MutableLiveData<List<ScoreboardProfile>> scoreboard;
  private final MutableLiveData<List<Badge>> badges;
  private final MutableLiveData<Badge> foregroundBadge;
  private final MutableLiveData<Integer> gems;
  private final MutableLiveData<Set<String>> ownedBadges;

  public CROSSViewModel() {
    selectedStrategy = new MutableLiveData<>(0);
    visitAllowed = new MutableLiveData<>(false);
    routeCollection = new MutableLiveData<>(new ArrayList<>());
    foregroundRoute = new MutableLiveData<>(null);
    foregroundRoute.observeForever(this::asyncUpdateWaypointCollection);
    waypointCollection = new MutableLiveData<>(new ArrayList<>());
    foregroundWaypoint = new MutableLiveData<>(null);
    badges = new MutableLiveData<>(new ArrayList<>());
    foregroundBadge = new MutableLiveData<>(null);
    scoreboard = new MutableLiveData<>(new ArrayList<>());
    gems = new MutableLiveData<>(KeyValueDataManager.get().getInt("gems", 0));
    ownedBadges =
        new MutableLiveData<>(
            KeyValueDataManager.get().getStringSet("ownedBadges", new HashSet<>()));
  }

  public void selectStrategy(int flags) {
    selectedStrategy.setValue(flags);
  }

  public LiveData<Integer> selectedStrategy() {
    return selectedStrategy;
  }

  public void proceedToVisit() {
    visitAllowed.setValue(true);
  }

  public void endVisit() {
    visitAllowed.setValue(false);
  }

  public LiveData<Boolean> visitAllowed() {
    return visitAllowed;
  }

  public LiveData<List<ExtendedRoute>> getRouteCollection() {
    return routeCollection;
  }

  public void setForegroundRoute(ExtendedRoute route) {
    waypointCollection.setValue(new ArrayList<>());
    foregroundRoute.setValue(route);
  }

  public LiveData<ExtendedRoute> getForegroundRoute() {
    return foregroundRoute;
  }

  public LiveData<List<ExtendedWaypoint>> getWaypointCollection() {
    return waypointCollection;
  }

  public void setForegroundWaypoint(ExtendedWaypoint waypoint) {
    foregroundWaypoint.setValue(waypoint);
  }

  public LiveData<ExtendedWaypoint> getForegroundWaypoint() {
    return foregroundWaypoint;
  }

  public LiveData<List<Badge>> getBadges() {
    return badges;
  }

  public LiveData<Badge> getForegroundBadge() {
    return foregroundBadge;
  }

  public void setForegroundBadge(Badge badge) {
    foregroundBadge.setValue(badge);
  }

  public LiveData<List<ScoreboardProfile>> getScoreboard() {
    return scoreboard;
  }

  public LiveData<Integer> getGems() {
    return gems;
  }

  public LiveData<Set<String>> getOwnedBadges() {
    return ownedBadges;
  }

  public void asyncUpdate() {
    new Thread(this::update).start();
  }

  private synchronized void update() {
    // Update Routes
    List<ExtendedRoute> routeCollection = CROSSCityApp.get().db().routeDao().getExtendedRoutes();
    routeCollection.sort(Comparator.comparingInt(route -> route.route.position));
    this.routeCollection.postValue(routeCollection);

    ExtendedRoute foregroundRoute = this.foregroundRoute.getValue();
    if (foregroundRoute != null) {
      this.foregroundRoute.postValue(
          routeCollection.stream()
              .filter(route -> route.route.id.equals(foregroundRoute.route.id))
              .findAny()
              .orElse(null));
    }

    // Update Waypoints
    updateWaypointCollection(foregroundRoute);

    // Update Scoreboard
    List<ScoreboardProfile> updatedScoreboard =
        CROSSCityApp.get().db().scoreboardProfileDao().getAll();
    updatedScoreboard.sort(Comparator.comparingInt(profile -> profile.position));
    scoreboard.postValue(updatedScoreboard);

    // Update Badges
    List<Badge> updatedBadges = CROSSCityApp.get().db().badgeDao().getAll();
    updatedBadges.sort(Comparator.comparingInt(badge -> badge.position));
    badges.postValue(updatedBadges);

    Badge foregroundBadge = this.foregroundBadge.getValue();
    if (foregroundBadge != null) {
      this.foregroundBadge.postValue(
          updatedBadges.stream()
              .filter(badge -> badge.id.equals(foregroundBadge.id))
              .findAny()
              .orElse(null));
    }

    Timber.i("Updated in-memory data.");
  }

  private void asyncUpdateWaypointCollection(ExtendedRoute route) {
    new Thread(() -> this.updateWaypointCollection(route)).start();
  }

  private synchronized void updateWaypointCollection(ExtendedRoute route) {
    if (route == null) return;
    List<ExtendedWaypoint> waypointCollection =
        CROSSCityApp.get().db().waypointDao().getExtendedWaypoints(route.route.id);
    waypointCollection.sort(Comparator.comparingInt(waypoint -> waypoint.waypoint.position));
    this.waypointCollection.postValue(waypointCollection);

    ExtendedWaypoint foregroundWaypoint = this.foregroundWaypoint.getValue();
    if (foregroundWaypoint != null) {
      this.foregroundWaypoint.postValue(
          waypointCollection.stream()
              .filter(waypoint -> waypoint.waypoint.id.equals(foregroundWaypoint.waypoint.id))
              .findAny()
              .orElse(null));
    }
  }

  public synchronized void updateScoreboard() {
    try {
      List<ScoreboardProfile> updatedScoreboard =
          APIManager.get().getScoreboardAPI().getScoreboard();
      updatedScoreboard.sort(Comparator.comparingInt(profile -> profile.position));
      scoreboard.postValue(updatedScoreboard);
      new Thread(
              () ->
                  CROSSCityApp.get()
                      .db()
                      .runInTransaction(
                          () -> {
                            CROSSCityApp.get().db().scoreboardProfileDao().delete();
                            CROSSCityApp.get()
                                .db()
                                .scoreboardProfileDao()
                                .insertAll(updatedScoreboard.toArray(new ScoreboardProfile[0]));
                          }))
          .start();
    } catch (IOException e) {
      Timber.e(e, "Scoreboard fetch failed.");
    }
  }

  public void updateGems(int gems) {
    this.gems.postValue(gems);
  }

  public void updateOwnedBadges(Set<String> ownedBadges) {
    this.ownedBadges.postValue(ownedBadges);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    foregroundRoute.removeObserver(this::asyncUpdateWaypointCollection);
  }
}
