package pt.ulisboa.tecnico.cross.ui.login;

import androidx.annotation.Nullable;

public class LoginFormState {

  @Nullable private final Integer usernameError;
  @Nullable private final Integer passwordError;
  private final boolean isDataValid;

  public LoginFormState(@Nullable Integer usernameError, @Nullable Integer passwordError) {
    this.usernameError = usernameError;
    this.passwordError = passwordError;
    this.isDataValid = usernameError == null && passwordError == null;
  }

  @Nullable
  public Integer getUsernameError() {
    return usernameError;
  }

  @Nullable
  public Integer getPasswordError() {
    return passwordError;
  }

  public boolean isDataValid() {
    return isDataValid;
  }
}
