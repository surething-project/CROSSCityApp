package pt.ulisboa.tecnico.cross.ui.initiatedroutes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.model.route.ExtendedRoute;

public class InitiatedRoutesViewModel extends ViewModel {

  private final MutableLiveData<Map<String, TripProgress>> tripsProgress;

  public InitiatedRoutesViewModel() {
    this.tripsProgress = new MutableLiveData<>(new HashMap<>());
  }

  public LiveData<Map<String, TripProgress>> getTripsProgress() {
    return tripsProgress;
  }

  public void asyncUpdateTripsProgress(List<ExtendedRoute> initiatedRoutes) {
    new Thread(() -> this.updateTripsProgress(initiatedRoutes)).start();
  }

  private void updateTripsProgress(List<ExtendedRoute> initiatedRoutes) {
    this.tripsProgress.postValue(
        initiatedRoutes.stream()
            .collect(
                Collectors.toMap(
                    route -> route.route.id,
                    route ->
                        new TripProgress(
                            CROSSCityApp.get()
                                .db()
                                .visitDao()
                                .getNumberOfVerifiedVisits(route.trip.id),
                            CROSSCityApp.get()
                                .db()
                                .waypointDao()
                                .getNumberOfWaypoints(route.route.id)))));
  }
}
