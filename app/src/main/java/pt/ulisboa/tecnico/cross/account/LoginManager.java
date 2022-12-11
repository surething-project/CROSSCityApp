package pt.ulisboa.tecnico.cross.account;

import static pt.ulisboa.tecnico.cross.account.LoginManager.LOGIN_METHOD.AUTO_SIGNIN;
import static pt.ulisboa.tecnico.cross.account.LoginManager.LOGIN_METHOD.SIGNUP;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.security.PublicKey;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.account.model.LoggedInUser;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.api.exceptions.NoConnectivityException;
import pt.ulisboa.tecnico.cross.firebase.MessagingService;

public class LoginManager {

  public enum LOGIN_METHOD {
    SIGNUP,
    SIGNIN,
    AUTO_SIGNIN
  }

  private final MutableLiveData<LoggedInUser> loggedInUser;

  public static LoginManager get() {
    return LoginManagerHolder.INSTANCE;
  }

  private LoginManager() {
    this.loggedInUser = new MutableLiveData<>(null);
  }

  public LiveData<LoggedInUser> getLoggedInUser() {
    return loggedInUser;
  }

  public boolean isLoggedIn() {
    return loggedInUser.getValue() != null;
  }

  public boolean login(String username, String password, LOGIN_METHOD loginMethod) {
    String sessionId = null;
    PublicKey publicKey = null;
    if (loginMethod != AUTO_SIGNIN) {
      CredentialsManager.get().generateNewSessionId();
      sessionId = CredentialsManager.get().getSessionId();
      CryptoManager.get().generateNewUserKeyPair();
      publicKey = CryptoManager.get().getUserPublicKey();
      if (sessionId == null || publicKey == null) return false;
    }
    try {
      if (loginMethod == SIGNUP) {
        APIManager.get().getUserAPI().signup(username, password, sessionId, publicKey);
      } else {
        APIManager.get().getUserAPI().signin(username, password, sessionId, publicKey);
      }
      new Thread(() -> APIManager.get().getUserAPI().registerToken(MessagingService.getToken()))
          .start();
    } catch (IOException e) {
      if (!(e instanceof NoConnectivityException) || loginMethod != AUTO_SIGNIN) {
        CROSSCityApp.get().showToast("Authentication failed: " + e.getMessage());
        return false;
      }
    }
    if (sessionId == null) sessionId = CredentialsManager.get().getSessionId();
    loggedInUser.postValue(new LoggedInUser(username, sessionId));
    CROSSCityApp.get().showToast("Welcome " + username + "!");
    return true;
  }

  public void logout() {
    loggedInUser.postValue(null);
    // TODO: Delete all trips and visits
  }

  private static class LoginManagerHolder {
    private static final LoginManager INSTANCE = new LoginManager();
  }
}
