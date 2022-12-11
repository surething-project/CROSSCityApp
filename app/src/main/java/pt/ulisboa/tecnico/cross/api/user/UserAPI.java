package pt.ulisboa.tecnico.cross.api.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.security.PublicKey;
import java.util.HashSet;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.account.CryptoManager;
import pt.ulisboa.tecnico.cross.account.KeyValueDataManager;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.api.CallWrapper;
import pt.ulisboa.tecnico.cross.contract.User;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import retrofit2.Retrofit;
import timber.log.Timber;

public class UserAPI {

  private final UserService userService;

  public UserAPI(Retrofit retrofit) {
    userService = retrofit.create(UserService.class);
  }

  public void signup(
      String username, String password, @NonNull String sessionId, @NonNull PublicKey publicKey)
      throws IOException {
    User.CryptoIdentity cryptoIdentity =
        User.CryptoIdentity.newBuilder()
            .setSessionId(sessionId)
            .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
            .build();
    User.Credentials credentials =
        User.Credentials.newBuilder()
            .setUsername(username)
            .setPassword(password)
            .setCryptoIdentity(cryptoIdentity)
            .build();

    welcome(new CallWrapper<>(userService.signup(credentials)).execute());
  }

  public void signin(
      String username, String password, @Nullable String sessionId, @Nullable PublicKey publicKey)
      throws IOException {
    User.Credentials.Builder credentialsBuilder =
        User.Credentials.newBuilder().setUsername(username).setPassword(password);
    if (sessionId != null && publicKey != null) {
      User.CryptoIdentity cryptoIdentity =
          User.CryptoIdentity.newBuilder()
              .setSessionId(sessionId)
              .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
              .build();
      credentialsBuilder.setCryptoIdentity(cryptoIdentity);
    }
    User.Credentials credentials = credentialsBuilder.build();

    welcome(new CallWrapper<>(userService.signin(credentials)).execute());
  }

  private void welcome(User.Welcome welcome) {
    KeyValueDataManager.get().edit().putInt("gems", welcome.getGems()).apply();
    KeyValueDataManager.get()
        .edit()
        .putStringSet("ownedBadges", new HashSet<>(welcome.getOwnedBadgesList()))
        .apply();
    CROSSViewModel viewModel = CROSSCityApp.get().getViewModel();
    if (viewModel != null) {
      viewModel.updateGems(welcome.getGems());
      viewModel.updateOwnedBadges(new HashSet<>(welcome.getOwnedBadgesList()));
    }
    APIManager.get().setJwt(welcome.getJwt());
    CryptoManager.get().storeServerCertificate(welcome.getServerCertificate().toByteArray());
  }

  public void registerToken(String registrationToken) {
    try {
      new CallWrapper<>(
              userService.registerToken(
                  APIManager.get().getJwt(),
                  User.Token.newBuilder().setRegistrationToken(registrationToken).build()))
          .execute();
    } catch (IOException e) {
      Timber.w(e, "The registration token has not been registered.");
    }
  }
}
