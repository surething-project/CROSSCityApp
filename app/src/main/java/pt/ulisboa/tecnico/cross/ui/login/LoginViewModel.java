package pt.ulisboa.tecnico.cross.ui.login;

import static pt.ulisboa.tecnico.cross.account.LoginManager.LOGIN_METHOD.SIGNIN;
import static pt.ulisboa.tecnico.cross.account.LoginManager.LOGIN_METHOD.SIGNUP;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.account.CredentialsManager;
import pt.ulisboa.tecnico.cross.account.LoginManager;

public class LoginViewModel extends ViewModel {

  private final MutableLiveData<LoginFormState> loginFormState;

  public LoginViewModel() {
    this.loginFormState = new MutableLiveData<>();
  }

  public LiveData<LoginFormState> getLoginFormState() {
    return loginFormState;
  }

  public void usernameChanged(String username) {
    LoginFormState loginFormStateValue = loginFormState.getValue();
    loginFormState.setValue(
        new LoginFormState(
            isUserNameValid(username) ? null : R.string.invalid_username,
            loginFormStateValue != null
                ? loginFormStateValue.getPasswordError()
                : Integer.valueOf(R.string.invalid_password)));
  }

  public void passwordChanged(String password) {
    LoginFormState loginFormStateValue = loginFormState.getValue();
    loginFormState.setValue(
        new LoginFormState(
            loginFormStateValue != null
                ? loginFormStateValue.getUsernameError()
                : Integer.valueOf(R.string.invalid_username),
            isPasswordValid(password) ? null : R.string.invalid_password));
  }

  private boolean isUserNameValid(String username) {
    if (username == null) return false;
    if (username.contains("@")) {
      return Patterns.EMAIL_ADDRESS.matcher(username).matches();
    } else {
      return !username.trim().isEmpty();
    }
  }

  private boolean isPasswordValid(String password) {
    return password != null && password.trim().length() > 5;
  }

  public boolean login(String username, String password, boolean isNewAccount) {
    boolean loginSucceeded =
        LoginManager.get().login(username, password, isNewAccount ? SIGNUP : SIGNIN);
    if (loginSucceeded) CredentialsManager.get().save(username, password);
    return loginSucceeded;
  }
}
