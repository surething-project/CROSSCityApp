package pt.ulisboa.tecnico.cross.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

import java.util.Map;

public abstract class LocalizedEntity extends IdentifiableEntity {
  @ColumnInfo(name = "main_locale")
  @NonNull
  public String mainLocale;

  public LocalizedEntity(@NonNull String id, @NonNull String mainLocale) {
    super(id);
    this.mainLocale = mainLocale;
  }

  protected String get(Context context, Map<String, String> localizedContent) {
    return localizedContent.getOrDefault(
        getLanguage(context), localizedContent.getOrDefault(mainLocale, "Undefined"));
  }

  private String getLanguage(Context context) {
    return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
  }
}
