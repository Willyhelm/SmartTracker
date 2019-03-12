package net.ddns.smarttracker;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
  private static final Object sLock = new Object();
  private static RetrofitClient sInstance;
  private final Retrofit mRetrofit;

  private RetrofitClient() {
    mRetrofit =
        new Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
  }

  public static RetrofitClient getInstance() {
    synchronized (sLock) {
      if (sInstance == null) {
        sInstance = new RetrofitClient();
      }
      return sInstance;
    }
  }

  public ApiService getApiService() {
    return mRetrofit.create(ApiService.class);
  }
}
