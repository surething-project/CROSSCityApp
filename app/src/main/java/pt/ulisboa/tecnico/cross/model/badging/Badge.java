package pt.ulisboa.tecnico.cross.model.badging;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Map;

import pt.ulisboa.tecnico.cross.model.LocalizedEntity;

@Entity(tableName = "badge")
public class Badge extends LocalizedEntity {
  public int position;

  @ColumnInfo(name = "image_url")
  @NonNull
  public String imageUrl;

  @NonNull public Map<String, String> names;
  @NonNull public Map<String, String> quests;
  @NonNull public Map<String, String> achievements;

  public Badge(
      @NonNull String id,
      int position,
      @NonNull String imageUrl,
      @NonNull String mainLocale,
      @NonNull Map<String, String> names,
      @NonNull Map<String, String> quests,
      @NonNull Map<String, String> achievements) {
    super(id, mainLocale);
    this.position = position;
    this.imageUrl = imageUrl;
    this.names = names;
    this.quests = quests;
    this.achievements = achievements;
  }

  public String getName(Context context) {
    return get(context, names);
  }

  public String getQuest(Context context) {
    return get(context, quests);
  }

  public String getAchievement(Context context) {
    return get(context, achievements);
  }
}
