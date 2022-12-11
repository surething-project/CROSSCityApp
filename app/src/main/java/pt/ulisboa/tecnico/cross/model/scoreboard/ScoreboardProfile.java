package pt.ulisboa.tecnico.cross.model.scoreboard;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Entity(
    tableName = "scoreboard",
    primaryKeys = {"category", "position"})
public class ScoreboardProfile {
  public enum CATEGORY {
    ALL_TIME,
    SEASONAL,
    WEEKLY
  }

  @NonNull public String username;
  public int position;
  public int score;
  @NonNull public List<String> ownedBadges;
  @NonNull public CATEGORY category;

  public ScoreboardProfile(
      @NonNull String username,
      int position,
      int score,
      @NonNull List<String> ownedBadges,
      @NonNull CATEGORY category) {
    this.username = username;
    this.position = position;
    this.score = score;
    this.ownedBadges = ownedBadges;
    this.category = category;
  }

  @NonNull
  @Override
  public String toString() {
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("Username", username);
      jsonObject.put("Position", position);
      jsonObject.put("Score", score);
      jsonObject.put("OwnedBadges", ownedBadges);
      jsonObject.put("Category", category);
      return jsonObject.toString(2);
    } catch (JSONException e) {
      return e.getMessage();
    }
  }
}
