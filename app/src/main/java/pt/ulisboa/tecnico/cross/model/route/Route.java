package pt.ulisboa.tecnico.cross.model.route;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Map;

import pt.ulisboa.tecnico.cross.model.LocalizedEntity;

@Entity(tableName = "route")
public class Route extends LocalizedEntity {
  public int position;

  @ColumnInfo(name = "image_url")
  @NonNull
  public String imageUrl;

  @NonNull public Map<String, String> names;
  @NonNull public Map<String, String> descriptions;

  public Route(
      @NonNull String id,
      int position,
      @NonNull String imageUrl,
      @NonNull String mainLocale,
      @NonNull Map<String, String> names,
      @NonNull Map<String, String> descriptions) {
    super(id, mainLocale);
    this.position = position;
    this.imageUrl = imageUrl;
    this.names = names;
    this.descriptions = descriptions;
  }

  public String getName(Context context) {
    return get(context, names);
  }

  public String getDescription(Context context) {
    return get(context, descriptions);
  }
}
