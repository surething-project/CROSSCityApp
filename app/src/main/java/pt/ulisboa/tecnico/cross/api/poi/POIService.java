package pt.ulisboa.tecnico.cross.api.poi;

import pt.ulisboa.tecnico.cross.contract.POIOuterClass.GetPOIsResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface POIService {

  @GET("poi")
  Call<GetPOIsResponse> getPOIs();
}
