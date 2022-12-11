package pt.ulisboa.tecnico.cross.model.poi;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.Map;

import pt.ulisboa.tecnico.cross.model.LocalizedEntity;

@Entity(tableName = "poi")
public class POI extends LocalizedEntity {
  @NonNull
  @ColumnInfo(name = "world_coord")
  public double[] worldCoord;

  @ColumnInfo(name = "web_url")
  @Nullable
  public String webUrl;

  @ColumnInfo(name = "image_url")
  @NonNull
  public String imageUrl;

  @NonNull public Map<String, String> names;
  @NonNull public Map<String, String> descriptions;

  public POI(
      @NonNull String id,
      @NonNull double[] worldCoord,
      @Nullable String webUrl,
      @NonNull String imageUrl,
      @NonNull String mainLocale,
      @NonNull Map<String, String> names,
      @NonNull Map<String, String> descriptions) {
    super(id, mainLocale);
    this.worldCoord = worldCoord;
    this.webUrl = webUrl;
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
