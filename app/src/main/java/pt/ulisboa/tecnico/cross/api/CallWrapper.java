package pt.ulisboa.tecnico.cross.api;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import java.io.IOException;
import java.util.Map;

import pt.ulisboa.tecnico.cross.account.CredentialsManager;
import pt.ulisboa.tecnico.cross.api.exceptions.APIException;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class CallWrapper<T> {

  private final Call<T> call;
  private boolean renewedJwt;

  public CallWrapper(Call<T> call) {
    this.call = call;
    this.renewedJwt = false;
  }

  public T execute() throws IOException {
    try {
      Response<T> response = call.execute();
      if (response.isSuccessful()) {
        Timber.i("%s: %s", response, response.body());
        return response.body();
      } else {
        String errorMessage = response.errorBody().string();
        Timber.e("%s: %s", response, errorMessage);
        if (response.code() == HTTP_UNAUTHORIZED && !renewedJwt) {
          renewedJwt = true;
          Map<String, ?> credentials = CredentialsManager.get().request();
          if (credentials.containsKey("username") && credentials.containsKey("password")) {
            String username = (String) credentials.get("username");
            String password = (String) credentials.get("password");
            APIManager.get().getUserAPI().signin(username, password, null, null);
            // Retry after renewing JWT
            return execute();
          }
        }
        throw new APIException(errorMessage);
      }
    } catch (IOException e) {
      Timber.e(e, "%s call failed to execute.", call.request().method());
      throw e;
    }
  }
}
