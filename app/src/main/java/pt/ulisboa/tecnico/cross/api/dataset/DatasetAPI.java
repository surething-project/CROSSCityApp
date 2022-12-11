package pt.ulisboa.tecnico.cross.api.dataset;

import java.io.IOException;

import pt.ulisboa.tecnico.cross.api.CallWrapper;
import retrofit2.Retrofit;

public class DatasetAPI {

  private final DatasetService datasetService;

  public DatasetAPI(Retrofit retrofit) {
    this.datasetService = retrofit.create(DatasetService.class);
  }

  public String getLatestDatasetVersion() throws IOException {
    return new CallWrapper<>(datasetService.getLatestDataset()).execute().getVersion();
  }
}
