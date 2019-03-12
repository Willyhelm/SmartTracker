package net.ddns.smarttracker;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

interface ApiService {

  String BASE_URL = "http://smarttracker.ddns.net/";

  @FormUrlEncoded
  @POST("AddLocation.php")
  Call<Void> addLocation(@Field("latlng") String latlng);

  @GET("DeleteLocations.php")
  Call<DeleteLocationsResponse> deleteLocations();
}
