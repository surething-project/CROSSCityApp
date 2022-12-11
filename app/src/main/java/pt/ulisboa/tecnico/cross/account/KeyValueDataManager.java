package pt.ulisboa.tecnico.cross.account;

import android.content.Context;
import android.content.SharedPreferences;

import pt.ulisboa.tecnico.cross.CROSSCityApp;

public class KeyValueDataManager {

  public static SharedPreferences get() {
    return SharedPreferencesHolder.INSTANCE;
  }

  private static class SharedPreferencesHolder {
    private static final String SHARED_PREF_FILENAME = "CROSSPlainSharedPreferences";

    private static final SharedPreferences INSTANCE =
        CROSSCityApp.get().getSharedPreferences(SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
  }
}
