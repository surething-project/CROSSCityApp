package pt.ulisboa.tecnico.cross.api.trip;

import pt.ulisboa.tecnico.cross.contract.TripOuterClass.CreateOrUpdateTripResponse;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass.GetTripsResponse;
import pt.ulisboa.tecnico.cross.contract.TripOuterClass.Trip;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface TripService {

  @POST("trip")
  Call<CreateOrUpdateTripResponse> createOrUpdateTrip(
      @Header("Authorization") String jwt, @Body Trip trip);

  @GET("trip")
  Call<GetTripsResponse> getTrips(@Header("Authorization") String jwt);
}
