package net.ddns.smarttracker;

import android.content.Context;

import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.ResolvableFuture;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadWorker extends ListenableWorker {

  public static final String KEY_LATLNG = "latlng";
  private final String latlng;
  private ResolvableFuture<Result> mFuture;

  public UploadWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
    super(appContext, workerParams);
    latlng = workerParams.getInputData().getString(KEY_LATLNG);
  }

  @NonNull
  @Override
  public ListenableFuture<Result> startWork() {
    mFuture = ResolvableFuture.create();
    RetrofitClient.getInstance()
        .getApiService()
        .addLocation(latlng)
        .enqueue(
            new Callback<Void>() {
              @Override
              public void onResponse(Call<Void> call, Response<Void> response) {
                mFuture.set(Result.success());
              }

              @Override
              public void onFailure(Call<Void> call, Throwable t) {
                mFuture.set(Result.failure());
              }
            });
    return mFuture;
  }
}
