package pt.ulisboa.tecnico.cross;

import static pt.ulisboa.tecnico.cross.account.LoginManager.LOGIN_METHOD.AUTO_SIGNIN;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Properties;

import pt.ulisboa.tecnico.cross.account.CredentialsManager;
import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.account.model.LoggedInUser;
import pt.ulisboa.tecnico.cross.catalog.CatalogManager;
import pt.ulisboa.tecnico.cross.model.CROSSDatabase;
import pt.ulisboa.tecnico.cross.travel.TravelManager;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import timber.log.Timber;

public class CROSSCityApp extends Application {

  private final Properties properties = new Properties();
  private CROSSDatabase db;
  private WeakReference<MainActivity> activity;

  private static CROSSCityApp instance;

  public static CROSSCityApp get() {
    return instance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;

    plantTimber();
    loadAppProperties();
    db = Room.databaseBuilder(this, CROSSDatabase.class, getProperty("DB_NAME")).build();

    CatalogManager.get().enqueueSyncPeriodicWork();
    CatalogManager.get().getUpdate().observeForever(this::onFirstCatalogUpdate);
    autoLogin();
  }

  private void plantTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(
          new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(@NonNull StackTraceElement element) {
              return String.format(
                  "%s[%s:%s:%s]",
                  getString(R.string.app_name).replaceAll("\\s+", ""),
                  super.createStackElementTag(element),
                  element.getMethodName(),
                  element.getLineNumber());
            }
          });
    }
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  public CROSSDatabase db() {
    return db;
  }

  private void loadAppProperties() {
    try {
      properties.load(getAssets().open("CROSSCityApp.properties"));
    } catch (IOException e) {
      Timber.e(e, "Error loading properties.");
      System.exit(1);
    }
  }

  private void onFirstCatalogUpdate(Boolean update) {
    if (update != null) {
      LoginManager.get().getLoggedInUser().observeForever(this::onLoggedInUserUpdate);
      CatalogManager.get().getUpdate().removeObserver(this::onFirstCatalogUpdate);
    }
  }

  private void onLoggedInUserUpdate(LoggedInUser loggedInUser) {
    if (loggedInUser != null) {
      TravelManager.get().enqueueSyncWork();
      TravelManager.get().enqueueSubmitWork();
    }
  }

  private void autoLogin() {
    Map<String, ?> credentials = CredentialsManager.get().request();
    if (credentials.containsKey("username") && credentials.containsKey("password")) {
      String username = (String) credentials.get("username");
      String password = (String) credentials.get("password");
      new Thread(() -> LoginManager.get().login(username, password, AUTO_SIGNIN)).start();
    }
  }

  public void showToast(String message) {
    if (Looper.getMainLooper().isCurrentThread()) createToast(message).show();
    else new Handler(Looper.getMainLooper()).post(() -> createToast(message).show());
    Timber.w("Showing toast message: %s", message);
  }

  private Toast createToast(String message) {
    return Toast.makeText(this, message, Toast.LENGTH_LONG);
  }

  public synchronized MainActivity getActivity() {
    return activity.get();
  }

  public synchronized void setActivity(MainActivity activity) {
    this.activity = new WeakReference<>(activity);
  }

  public synchronized CROSSViewModel getViewModel() {
    if (activity == null || activity.get() == null) return null;
    return new ViewModelProvider(activity.get()).get(CROSSViewModel.class);
  }
}
