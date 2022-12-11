package pt.ulisboa.tecnico.cross.api.badging;

import pt.ulisboa.tecnico.cross.contract.gamification.Badging.GetBadgesResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface BadgingService {

  @GET("badging")
  Call<GetBadgesResponse> getBadges();
}
