package pt.ulisboa.tecnico.cross.api.dataset;

import pt.ulisboa.tecnico.cross.contract.Dataset.GetLatestDatasetResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface DatasetService {

  @GET("dataset")
  Call<GetLatestDatasetResponse> getLatestDataset();
}
