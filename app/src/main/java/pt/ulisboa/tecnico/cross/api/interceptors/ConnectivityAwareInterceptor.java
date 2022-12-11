package pt.ulisboa.tecnico.cross.api.interceptors;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;
import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.api.exceptions.NoConnectivityException;

public class ConnectivityAwareInterceptor implements Interceptor {

  @NonNull
  @Override
  public Response intercept(@NonNull Chain chain) throws IOException {
    if (!isConnected()) throw new NoConnectivityException();
    return chain.proceed(chain.request());
  }

  private boolean isConnected() {
    ConnectivityManager connectivityManager =
        CROSSCityApp.get().getSystemService(ConnectivityManager.class);
    if (connectivityManager == null) return false;
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }
}
