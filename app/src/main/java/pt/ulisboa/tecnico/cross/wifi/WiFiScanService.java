package pt.ulisboa.tecnico.cross.wifi;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.MainActivity;
import pt.ulisboa.tecnico.cross.R;

public class WiFiScanService extends Service {

  private final String CHANNEL_ID = "WiFiScanServiceChannel";
  private final int ONGOING_NOTIFICATION_ID = 1;
  private static final AtomicBoolean active = new AtomicBoolean(false);

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    NotificationChannel notificationChannel =
        new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
    NotificationManager notificationManager = getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(notificationChannel);

    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE);

    Notification notification =
        new Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_wifi_24)
            .setContentTitle("Wi-Fi evidence acquisition")
            .setContentText("Wi-Fi scanning has started to certify your location.")
            .setContentIntent(pendingIntent)
            .build();
    startForeground(ONGOING_NOTIFICATION_ID, notification);

    return START_STICKY;
  }

  @Override
  public void onCreate() {
    active.set(true);
    super.onCreate();
    WiFiHelper.get().switchOn();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    WiFiHelper.get().switchOff();
    active.set(false);
  }

  public static void start() {
    if (active.get()) return;
    Intent intent = new Intent(CROSSCityApp.get(), WiFiScanService.class);
    CROSSCityApp.get().startForegroundService(intent);
  }

  public static void stop() {
    if (!active.get()) return;
    Intent intent = new Intent(CROSSCityApp.get(), WiFiScanService.class);
    CROSSCityApp.get().stopService(intent);
  }
}
