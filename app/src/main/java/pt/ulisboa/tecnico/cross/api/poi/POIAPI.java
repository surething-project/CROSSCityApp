package pt.ulisboa.tecnico.cross.api.poi;

import java.io.IOException;
import java.util.Set;

import pt.ulisboa.tecnico.cross.api.CallWrapper;
import pt.ulisboa.tecnico.cross.contract.POIOuterClass;
import pt.ulisboa.tecnico.cross.model.poi.POI;
import retrofit2.Retrofit;

public class POIAPI {

  private final POIService poiService;

  public POIAPI(Retrofit retrofit) {
    poiService = retrofit.create(POIService.class);
  }

  public void getPOIs(Set<POI> outputPOIs) throws IOException {
    new CallWrapper<>(poiService.getPOIs())
        .execute().getPOIsList().stream().map(this::fromProtobuf).forEach(outputPOIs::add);
  }

  private POI fromProtobuf(POIOuterClass.POI poi) {
    return new POI(
        poi.getId(),
        poi.getWorldCoordList().stream().mapToDouble(Double::doubleValue).toArray(),
        !poi.getWebURL().isEmpty() ? poi.getWebURL() : null,
        poi.getImageURL(),
        poi.getMainLocale(),
        poi.getNamesMap(),
        poi.getDescriptionsMap());
  }
}
