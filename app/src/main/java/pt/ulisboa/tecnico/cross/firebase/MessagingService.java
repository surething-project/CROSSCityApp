package pt.ulisboa.tecnico.cross.firebase;

import static pt.ulisboa.tecnico.cross.travel.TravelManager.updateGems;
import static pt.ulisboa.tecnico.cross.travel.TravelManager.updateOwnedBadges;
import static pt.ulisboa.tecnico.cross.travel.TravelManager.updateScoreboard;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import timber.log.Timber;

public class MessagingService extends FirebaseMessagingService {

  @Override
  public void onNewToken(@NonNull String token) {
    Timber.d("Messaging registration token refreshed: %s", token);
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage message) {
    Timber.d("Remote message: %s", message);
    CROSSCityApp.get().showToast(message.getNotification().getBody());

    switch (message.getNotification().getTitle()) {
      case "Endorsement Reward":
        updateScoreboard();
        updateGems(Integer.parseInt(message.getData().get("awardedGems")));
        break;
      case "Badge Earning":
        updateOwnedBadges(Collections.singletonList(message.getData().get("awardedBadge")));
        break;
      case "Payment":
        updateGems(-Integer.parseInt(message.getData().get("deductedGems")));
        break;
    }
  }

  public static String getToken() {
    final AtomicReference<String> token = new AtomicReference<>();
    synchronized (token) {
      FirebaseMessaging.getInstance()
          .getToken()
          .addOnCompleteListener(
              task -> {
                if (task.isSuccessful()) token.set(task.getResult());
                else Timber.w(task.getException(), "Messaging registration token fetch failed.");
                synchronized (token) {
                  token.notify();
                }
              });
      try {
        token.wait();
      } catch (InterruptedException ignored) {
      }
    }
    return token.get();
  }
}
