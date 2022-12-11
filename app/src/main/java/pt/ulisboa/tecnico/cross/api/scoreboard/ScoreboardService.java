package pt.ulisboa.tecnico.cross.api.scoreboard;

import pt.ulisboa.tecnico.cross.contract.gamification.Scoreboard.GetScoreboardResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ScoreboardService {

  @GET("scoreboard")
  Call<GetScoreboardResponse> getScoreboard(@Header("Authorization") String jwt);
}
