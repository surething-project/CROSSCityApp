package pt.ulisboa.tecnico.cross.api.user;

import pt.ulisboa.tecnico.cross.contract.User.Credentials;
import pt.ulisboa.tecnico.cross.contract.User.Token;
import pt.ulisboa.tecnico.cross.contract.User.Welcome;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface UserService {

  @POST("user/signup")
  Call<Welcome> signup(@Body Credentials credentials);

  @POST("user/signin")
  Call<Welcome> signin(@Body Credentials credentials);

  @POST("user/register_token")
  Call<Void> registerToken(@Header("Authorization") String jwt, @Body Token token);
}
