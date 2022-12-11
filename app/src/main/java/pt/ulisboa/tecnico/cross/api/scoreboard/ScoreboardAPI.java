package pt.ulisboa.tecnico.cross.api.scoreboard;

import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.ALL_TIME;
import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.SEASONAL;
import static pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY.WEEKLY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.api.CallWrapper;
import pt.ulisboa.tecnico.cross.contract.gamification.Scoreboard;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile;
import pt.ulisboa.tecnico.cross.model.scoreboard.ScoreboardProfile.CATEGORY;
import retrofit2.Retrofit;

public class ScoreboardAPI {

  private final ScoreboardService scoreboardService;

  public ScoreboardAPI(Retrofit retrofit) {
    scoreboardService = retrofit.create(ScoreboardService.class);
  }

  public List<ScoreboardProfile> getScoreboard() throws IOException {
    Scoreboard.GetScoreboardResponse response =
        new CallWrapper<>(scoreboardService.getScoreboard(APIManager.get().getJwt())).execute();
    List<ScoreboardProfile> scoreboard = new ArrayList<>();
    response.getAllTimeList().forEach(profile -> scoreboard.add(fromProtobuf(profile, ALL_TIME)));
    response.getSeasonalList().forEach(profile -> scoreboard.add(fromProtobuf(profile, SEASONAL)));
    response.getWeeklyList().forEach(profile -> scoreboard.add(fromProtobuf(profile, WEEKLY)));
    return scoreboard;
  }

  private ScoreboardProfile fromProtobuf(Scoreboard.ScoreboardProfile profile, CATEGORY category) {
    return new ScoreboardProfile(
        profile.getUsername(),
        profile.getPosition(),
        profile.getScore(),
        profile.getOwnedBadgesList(),
        category);
  }
}
