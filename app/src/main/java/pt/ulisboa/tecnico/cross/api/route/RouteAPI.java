package pt.ulisboa.tecnico.cross.api.route;

import java.io.IOException;
import java.util.Set;

import pt.ulisboa.tecnico.cross.api.CallWrapper;
import pt.ulisboa.tecnico.cross.contract.RouteOuterClass;
import pt.ulisboa.tecnico.cross.model.route.Route;
import pt.ulisboa.tecnico.cross.model.waypoint.Waypoint;
import retrofit2.Retrofit;

public class RouteAPI {

  private final RouteService routeService;

  public RouteAPI(Retrofit retrofit) {
    routeService = retrofit.create(RouteService.class);
  }

  public void getRoutes(Set<Route> outputRoutes, Set<Waypoint> outputWaypoints) throws IOException {
    RouteOuterClass.GetRoutesResponse response =
        new CallWrapper<>(routeService.getRoutes()).execute();
    for (RouteOuterClass.Route route : response.getRoutesList()) {
      outputRoutes.add(fromProtobuf(route));
      for (RouteOuterClass.Waypoint waypoint : route.getWaypointsList()) {
        outputWaypoints.add(fromProtobuf(route, waypoint));
      }
    }
  }

  private Route fromProtobuf(RouteOuterClass.Route route) {
    return new Route(
        route.getId(),
        route.getPosition(),
        route.getImageURL(),
        route.getMainLocale(),
        route.getNamesMap(),
        route.getDescriptionsMap());
  }

  private Waypoint fromProtobuf(RouteOuterClass.Route route, RouteOuterClass.Waypoint waypoint) {
    return new Waypoint(
        waypoint.getId(),
        waypoint.getPosition(),
        route.getId(),
        waypoint.getPoiId(),
        waypoint.getStayForSeconds());
  }
}
