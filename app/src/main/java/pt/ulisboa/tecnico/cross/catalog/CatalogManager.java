package pt.ulisboa.tecnico.cross.catalog;

import static androidx.work.BackoffPolicy.LINEAR;
import static androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS;
import static androidx.work.WorkRequest.MIN_BACKOFF_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.account.KeyValueDataManager;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.model.badging.Badge;
import pt.ulisboa.tecnico.cross.model.poi.POI;
import pt.ulisboa.tecnico.cross.model.route.Route;
import pt.ulisboa.tecnico.cross.model.waypoint.Waypoint;
import timber.log.Timber;

public class CatalogManager {

  private final MutableLiveData<Boolean> update;
  private IOException exception;

  public static CatalogManager get() {
    return CatalogManagerHolder.INSTANCE;
  }

  private CatalogManager() {
    this.update = new MutableLiveData<>(null);
  }

  public LiveData<Boolean> newUpdate() {
    update.setValue(null);
    return update;
  }

  public LiveData<Boolean> getUpdate() {
    return update;
  }

  public void enqueueSyncPeriodicWork() {
    PeriodicWorkRequest catalogSyncWorkRequest =
        new PeriodicWorkRequest.Builder(
                CatalogSyncWorker.class,
                // Fifteen minutes
                MIN_PERIODIC_INTERVAL_MILLIS,
                MILLISECONDS)
            .setConstraints(
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(
                LINEAR,
                // Ten seconds
                MIN_BACKOFF_MILLIS,
                MILLISECONDS)
            .build();
    WorkManager.getInstance(CROSSCityApp.get()).enqueue(catalogSyncWorkRequest);
  }

  synchronized void sync() throws IOException, InterruptedException {
    String latestDatasetVersion = APIManager.get().getDatasetAPI().getLatestDatasetVersion();
    if (latestDatasetVersion.equals(KeyValueDataManager.get().getString("datasetVersion", null))) {
      Timber.i("The latest version of the catalog has already been fetched.");
      update.postValue(false);
      return;
    }

    final Set<Route> latestRoutes = new HashSet<>();
    final Set<Waypoint> latestWaypoints = new HashSet<>();
    final Set<POI> latestPOIs = new HashSet<>();
    final Set<Badge> latestBadges = new HashSet<>();

    final Stream<APICall> apiCallStream =
        Stream.of(
            () -> APIManager.get().getRouteAPI().getRoutes(latestRoutes, latestWaypoints),
            () -> APIManager.get().getPOIAPI().getPOIs(latestPOIs),
            () -> APIManager.get().getBadgingAPI().getBadges(latestBadges));
    final List<Thread> apiCallThreads =
        apiCallStream
            .map(apiCall -> new Thread(() -> executeAPICall(apiCall)))
            .collect(Collectors.toList());

    exception = null;
    // All requests to the server are made in parallel.
    apiCallThreads.forEach(Thread::start);
    for (Thread apiCallThread : apiCallThreads) {
      apiCallThread.join();
      synchronized (this) {
        if (exception != null) {
          apiCallThreads.forEach(Thread::interrupt);
          throw exception;
        }
      }
    }

    CROSSCityApp.get()
        .db()
        .runInTransaction(
            () -> updateCatalog(latestRoutes, latestWaypoints, latestPOIs, latestBadges));
    // Update dataset version
    KeyValueDataManager.get().edit().putString("datasetVersion", latestDatasetVersion).apply();

    Timber.i("Catalog synchronization was successful.");
    update.postValue(true);
  }

  private void updateCatalog(
      Set<Route> latestRoutes,
      Set<Waypoint> latestWaypoints,
      Set<POI> latestPOIs,
      Set<Badge> latestBadges) {
    // Fetch known

    List<Route> knownRoutes = CROSSCityApp.get().db().routeDao().getAll();
    List<POI> knownPOIs = CROSSCityApp.get().db().poiDao().getAll();
    List<Waypoint> knownWaypoints = CROSSCityApp.get().db().waypointDao().getAll();
    List<Badge> knownBadges = CROSSCityApp.get().db().badgeDao().getAll();

    // Delete removed

    Set<Route> removedRoutes = new HashSet<>(knownRoutes);
    removedRoutes.removeAll(latestRoutes);
    CROSSCityApp.get().db().routeDao().deleteAll(removedRoutes.toArray(new Route[0]));

    Set<POI> removedPOIs = new HashSet<>(knownPOIs);
    removedPOIs.removeAll(latestPOIs);
    CROSSCityApp.get().db().poiDao().deleteAll(removedPOIs.toArray(new POI[0]));

    Set<Waypoint> removedWaypoints = new HashSet<>(knownWaypoints);
    removedWaypoints.removeAll(latestWaypoints);
    CROSSCityApp.get().db().waypointDao().deleteAll(removedWaypoints.toArray(new Waypoint[0]));

    Set<Badge> removedBadges = new HashSet<>(knownBadges);
    removedBadges.removeAll(latestBadges);
    CROSSCityApp.get().db().badgeDao().deleteAll(removedBadges.toArray(new Badge[0]));

    // Update known

    Set<Route> updatedRoutes = new HashSet<>(latestRoutes);
    updatedRoutes.retainAll(knownRoutes);
    CROSSCityApp.get().db().routeDao().updateAll(updatedRoutes.toArray(new Route[0]));

    Set<POI> updatedPOIs = new HashSet<>(latestPOIs);
    updatedPOIs.retainAll(knownPOIs);
    CROSSCityApp.get().db().poiDao().updateAll(updatedPOIs.toArray(new POI[0]));

    Set<Waypoint> updatedWaypoints = new HashSet<>(latestWaypoints);
    updatedWaypoints.retainAll(knownWaypoints);
    CROSSCityApp.get().db().waypointDao().updateAll(updatedWaypoints.toArray(new Waypoint[0]));

    Set<Badge> updatedBadges = new HashSet<>(latestBadges);
    updatedBadges.retainAll(knownBadges);
    CROSSCityApp.get().db().badgeDao().updateAll(updatedBadges.toArray(new Badge[0]));

    // Insert new

    Set<Route> newRoutes = new HashSet<>(latestRoutes);
    knownRoutes.forEach(newRoutes::remove);
    CROSSCityApp.get().db().routeDao().insertAll(newRoutes.toArray(new Route[0]));

    Set<POI> newPOIs = new HashSet<>(latestPOIs);
    knownPOIs.forEach(newPOIs::remove);
    CROSSCityApp.get().db().poiDao().insertAll(newPOIs.toArray(new POI[0]));

    Set<Waypoint> newWaypoints = new HashSet<>(latestWaypoints);
    knownWaypoints.forEach(newWaypoints::remove);
    CROSSCityApp.get().db().waypointDao().insertAll(newWaypoints.toArray(new Waypoint[0]));

    Set<Badge> newBadges = new HashSet<>(latestBadges);
    knownBadges.forEach(newBadges::remove);
    CROSSCityApp.get().db().badgeDao().insertAll(newBadges.toArray(new Badge[0]));
  }

  private interface APICall {
    void execute() throws IOException;
  }

  private void executeAPICall(APICall apiCall) {
    try {
      apiCall.execute();
    } catch (IOException e) {
      synchronized (this) {
        exception = e;
      }
    }
  }

  private static class CatalogManagerHolder {
    private static final CatalogManager INSTANCE = new CatalogManager();
  }
}
