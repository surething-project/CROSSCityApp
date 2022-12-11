package pt.ulisboa.tecnico.cross.api;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.api.interceptors.ConnectivityAwareInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.protobuf.ProtoConverterFactory;
import timber.log.Timber;

public class APIClient {

  private static final String CERTIFICATE_ALGORITHM = "X.509";
  private static final String CERTIFICATE_ALIAS = "CROSS_CA";
  private static final String SSL_PROTOCOL = "TLS";

  public static Retrofit newRetrofit() {
    String baseUrl = CROSSCityApp.get().getProperty("CROSS_SERVER_BASE_URL");
    try {
      return new Retrofit.Builder()
          .baseUrl(baseUrl)
          .client(generateClient(baseUrl.startsWith("https")))
          .addConverterFactory(ProtoConverterFactory.create())
          .build();
    } catch (CertificateException
        | IOException
        | KeyStoreException
        | NoSuchAlgorithmException
        | KeyManagementException e) {
      Timber.e(e, "Client creation failed.");
      System.exit(1);
      return null;
    }
  }

  private static OkHttpClient generateClient(boolean secure)
      throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
          KeyManagementException {

    ConnectivityAwareInterceptor connectivityAwareInterceptor = new ConnectivityAwareInterceptor();
    HttpLoggingInterceptor httpLogging = new HttpLoggingInterceptor();
    httpLogging.setLevel(Level.BODY);

    OkHttpClient.Builder client =
        new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(connectivityAwareInterceptor)
            .addInterceptor(httpLogging);

    if (secure) {
      CertificateFactory certificateFactory = CertificateFactory.getInstance(CERTIFICATE_ALGORITHM);
      Certificate certificate =
          certificateFactory.generateCertificate(CROSSCityApp.get().getAssets().open("CA.crt"));

      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null);
      keyStore.setCertificateEntry(CERTIFICATE_ALIAS, certificate);

      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);

      SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
      sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

      client.sslSocketFactory(
          sslContext.getSocketFactory(),
          (X509TrustManager) trustManagerFactory.getTrustManagers()[0]);
    }

    return client.build();
  }
}
