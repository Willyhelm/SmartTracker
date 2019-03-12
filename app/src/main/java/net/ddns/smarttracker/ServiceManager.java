package net.ddns.smarttracker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class ServiceManager {

  private static final Object sLock = new Object();
  private static ServiceManager sInstance;
  private final Context mAppContext;
  private final Repository mRepository;
  private boolean mRequestingLocationUpdates;

  private ServiceManager(Context context) {
    mAppContext = context;
    mRepository = Repository.getInstance();
  }

  public static ServiceManager getInstance(Context appContext) {
    synchronized (sLock) {
      if (sInstance == null) {
        sInstance = new ServiceManager(appContext.getApplicationContext());
      }
      return sInstance;
    }
  }

  public void requestLocationUpdates() {
    if (!mRequestingLocationUpdates) {
      mAppContext.startService(
          new Intent(mAppContext, LocationUpdatesService.class)
              .setAction(LocationUpdatesService.ACTION_START));
      mRequestingLocationUpdates = true;
      mRepository.setLocationStatus(LocationStatus.waiting());
    }
  }

  public void removeLocationUpdates() {
    if (mRequestingLocationUpdates) {
      mAppContext.stopService(new Intent(mAppContext, LocationUpdatesService.class));
      mRequestingLocationUpdates = false;
      mRepository.setLocationStatus(LocationStatus.stopped());
    }
  }

  public void notifyStopSelf() {
    mRequestingLocationUpdates = false;
    mRepository.setLocationStatus(LocationStatus.stopped());
  }

  public void updateLocation(Location location) {
    if (location == null) {
      mRepository.setLocationStatus(LocationStatus.unknown());
    } else {
      mRepository.setLocationStatus(LocationStatus.updated(location));
      uploadLocation(location);
    }
  }

  private void uploadLocation(Location location) {
    String latlng = location.getLatitude() + "," + location.getLongitude();
    Data inputData = new Data.Builder().putString(UploadWorker.KEY_LATLNG, latlng).build();
    Constraints constraints =
        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    OneTimeWorkRequest workRequest =
        new OneTimeWorkRequest.Builder(UploadWorker.class)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build();
    WorkManager.getInstance().enqueue(workRequest);
  }
}
