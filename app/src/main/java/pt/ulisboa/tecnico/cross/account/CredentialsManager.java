package pt.ulisboa.tecnico.cross.account;

import static androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV;
import static androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM;
import static androidx.security.crypto.MasterKeys.AES256_GCM_SPEC;

import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import timber.log.Timber;

public class CredentialsManager {

  private static final String SHARED_PREF_FILENAME = "CROSSSecureSharedPreferences";

  private SharedPreferences sharedPref;

  public static CredentialsManager get() {
    return CredentialsManagerHolder.INSTANCE;
  }

  private CredentialsManager() {
    try {
      final String masterKeyAlias = MasterKeys.getOrCreate(AES256_GCM_SPEC);
      sharedPref =
          EncryptedSharedPreferences.create(
              SHARED_PREF_FILENAME, masterKeyAlias, CROSSCityApp.get(), AES256_SIV, AES256_GCM);
    } catch (GeneralSecurityException | IOException e) {
      Timber.e(e, "Failed to create encrypted shared preferences.");
      System.exit(1);
    }
  }

  public synchronized void save(String username, String password) {
    sharedPref.edit().putString("username", username).putString("password", password).apply();
  }

  public synchronized Map<String, ?> request() {
    return sharedPref.getAll();
  }

  public synchronized void generateNewSessionId() {
    sharedPref.edit().putString("session_id", UUID.randomUUID().toString()).apply();
  }

  public synchronized String getSessionId() {
    return sharedPref.getString("session_id", null);
  }

  private static class CredentialsManagerHolder {
    private static final CredentialsManager INSTANCE = new CredentialsManager();
  }
}
