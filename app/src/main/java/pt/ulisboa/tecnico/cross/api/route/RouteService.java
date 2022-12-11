package pt.ulisboa.tecnico.cross.api.route;

import pt.ulisboa.tecnico.cross.contract.RouteOuterClass.GetRoutesResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RouteService {

  @GET("route")
  Call<GetRoutesResponse> getRoutes();
}
