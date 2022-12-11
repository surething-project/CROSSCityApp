package pt.ulisboa.tecnico.cross;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static pt.ulisboa.tecnico.cross.MainActivity.REQUEST_CODE.CHECK_LOCATION_SETTINGS;
import static pt.ulisboa.tecnico.cross.MainActivity.REQUEST_CODE.ENABLE_BLUETOOTH;
import static pt.ulisboa.tecnico.cross.MainActivity.REQUEST_CODE.STRATEGY_PERMISSIONS;
import static pt.ulisboa.tecnico.cross.MainActivity.STRATEGY.BLE_SCAN;
import static pt.ulisboa.tecnico.cross.MainActivity.STRATEGY.WIFI_SCAN;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.WorkManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.cross.account.LoginManager;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.ble.BLEHelper;
import pt.ulisboa.tecnico.cross.ble.BLEService;
import pt.ulisboa.tecnico.cross.catalog.CatalogManager;
import pt.ulisboa.tecnico.cross.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cross.databinding.NavHeaderMainBinding;
import pt.ulisboa.tecnico.cross.travel.TravelManager;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import pt.ulisboa.tecnico.cross.wifi.WiFiScanService;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;
  private AppBarConfiguration mAppBarConfiguration;
  private CROSSViewModel viewModel;
  private Menu optionsMenu;

  @Override
  protected synchronized void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setSupportActionBar(binding.appBarMain.toolbar);

    mAppBarConfiguration =
        new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_initiated_routes, R.id.nav_badging, R.id.nav_tourist_card)
            .setOpenableLayout(binding.drawerLayout)
            .build();
    NavController navController =
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(binding.navView, navController);
    viewModel = new ViewModelProvider(this).get(CROSSViewModel.class);
    viewModel.asyncUpdate();

    CatalogManager.get().newUpdate().observe(this, this::onDataUpdate);
    TravelManager.get().newUpdate().observe(this, this::onDataUpdate);

    NavHeaderMainBinding headerBinding =
        NavHeaderMainBinding.bind(binding.navView.getHeaderView(0));
    LoginManager.get()
        .getLoggedInUser()
        .observe(
            this,
            loggedInUser -> {
              boolean loggedIn = loggedInUser != null;
              if (loggedIn) headerBinding.displayableUsername.setText(loggedInUser.getUsername());
              headerBinding.displayableUsername.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
              getMenu().findItem(R.id.nav_login).setVisible(!loggedIn);
              getMenu().findItem(R.id.nav_scoreboard).setVisible(loggedIn);
              getMenu().findItem(R.id.nav_badging).setVisible(loggedIn);
              getMenu().findItem(R.id.nav_profile).setVisible(loggedIn);
              getMenu()
                  .findItem(R.id.nav_tourist_card)
                  .setVisible(loggedIn && APIManager.get().getJwt() != null);
            });
    viewModel
        .getRouteCollection()
        .observe(
            this,
            routeCollection -> {
              boolean visibility =
                  LoginManager.get().getLoggedInUser().getValue() != null
                      && routeCollection.stream()
                          .anyMatch(route -> route.wasInitiated() && !route.wasCompleted());
              getMenu().findItem(R.id.nav_initiated_routes).setVisible(visibility);
            });
    CROSSCityApp.get().setActivity(this);
  }

  private void onDataUpdate(Boolean update) {
    if (update != null && update) viewModel.asyncUpdate();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    menu.findItem(R.id.action_information_gems).setVisible(false);
    menu.findItem(R.id.action_information_score).setVisible(false);
    optionsMenu = menu;
    return true;
  }

  public Menu getMenu() {
    return binding.navView.getMenu();
  }

  public Menu getOptionsMenu() {
    return optionsMenu;
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController =
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }

  @SuppressLint("NonConstantResourceId")
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
            .navigate(R.id.nav_settings);
        return true;
      case R.id.action_information_gems:
      case R.id.action_information_score:
        Drawable drawable =
            DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.baseline_info_24));
        int blue = ContextCompat.getColor(this, R.color.blue);
        DrawableCompat.setTint(drawable, blue);
        AlertDialog alertDialog =
            new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(drawable)
                .setTitle(R.string.action_information)
                .setMessage(
                    getString(
                        item.getItemId() == R.id.action_information_gems
                            ? R.string.information_gems
                            : R.string.information_score))
                .create();
        alertDialog.show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /*********************************************************
   * Setup evidence acquisition with the selected strategy *
   *********************************************************/

  public void setupStrategy(int strategy) {
    CROSSViewModel viewModel = new ViewModelProvider(this).get(CROSSViewModel.class);
    viewModel.selectStrategy(strategy);
    List<String> permissions = new ArrayList<>();
    if (WIFI_SCAN.test(strategy) || BLE_SCAN.test(strategy)) {
      permissions.add(ACCESS_COARSE_LOCATION);
      permissions.add(ACCESS_FINE_LOCATION);
    }
    if (BLE_SCAN.test(strategy) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      permissions.add(BLUETOOTH_SCAN);
      permissions.add(BLUETOOTH_ADVERTISE);
      permissions.add(BLUETOOTH_CONNECT);
    }
    if (permissions.stream()
        .allMatch(permission -> checkSelfPermission(permission) == PERMISSION_GRANTED)) {
      enableLocationServices();
      if (BLE_SCAN.test(strategy)) enableBluetooth();
      viewModel.proceedToVisit();
    } else {
      requestPermissions(permissions.toArray(new String[0]), STRATEGY_PERMISSIONS.code);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == STRATEGY_PERMISSIONS.code) {
      if (Arrays.stream(grantResults).allMatch(grantResult -> grantResult == PERMISSION_GRANTED)) {
        Timber.i("The selected strategy's permissions have been granted!");
        enableLocationServices();
        int strategy = viewModel.selectedStrategy().getValue();
        if (BLE_SCAN.test(strategy)) enableBluetooth();
        viewModel.proceedToVisit();
      } else {
        Timber.e("Not all selected strategy's permissions have been granted.");
        finish();
      }
    }
  }

  private void enableLocationServices() {
    LocationRequest locationRequest = LocationRequest.create();
    locationRequest.setInterval(10000);
    locationRequest.setFastestInterval(5000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    LocationSettingsRequest locationSettingsRequest =
        new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();

    Task<LocationSettingsResponse> locationSettingsResponseTask =
        LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest);
    locationSettingsResponseTask.addOnFailureListener(
        this,
        e -> {
          if (e instanceof ResolvableApiException) {
            try {
              ResolvableApiException resolvableApiException = (ResolvableApiException) e;
              resolvableApiException.startResolutionForResult(this, CHECK_LOCATION_SETTINGS.code);
              return;
            } catch (SendIntentException sendIntentException) {
              e = sendIntentException;
            }
          }
          CROSSCityApp.get().showToast("Please turn on the GPS.");
          Timber.e(e);
        });
  }

  @SuppressLint("MissingPermission")
  // Required permissions are requested before calling this method.
  private void enableBluetooth() {
    BluetoothManager manager = BLEHelper.get().getManager();
    if (manager == null) return;
    BluetoothAdapter adapter = manager.getAdapter();
    if (adapter == null) return;

    if (!adapter.isEnabled()) {
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(intent, ENABLE_BLUETOOTH.code);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == CHECK_LOCATION_SETTINGS.code && resultCode != RESULT_OK) {
      CROSSCityApp.get().showToast("Please turn on GPS.");
    } else if (requestCode == ENABLE_BLUETOOTH.code && resultCode != RESULT_OK) {
      CROSSCityApp.get().showToast("Please turn on Bluetooth.");
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    WorkManager.getInstance(CROSSCityApp.get()).cancelAllWork();
    WiFiScanService.stop();
    BLEService.stop();
    binding = null;
  }

  public enum STRATEGY {
    WIFI_SCAN(1),
    BLE_SCAN(2);

    public final int value;

    STRATEGY(int value) {
      this.value = value;
    }

    public boolean test(int strategy) {
      return (value & strategy) != 0;
    }
  }

  enum REQUEST_CODE {
    STRATEGY_PERMISSIONS(1),
    CHECK_LOCATION_SETTINGS(2),
    ENABLE_BLUETOOTH(3);

    private final int code;

    REQUEST_CODE(int code) {
      this.code = 100 + code;
    }
  }
}
