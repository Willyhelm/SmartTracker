package net.ddns.smarttracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class Repository {

  private static final Object sLock = new Object();
  private static Repository sInstance;
  private final MutableLiveData<LocationStatus> mLocationStatus =
      new MutableLiveData<>(LocationStatus.stopped());

  private Repository() {}

  public static Repository getInstance() {
    synchronized (sLock) {
      if (sInstance == null) {
        sInstance = new Repository();
      }
      return sInstance;
    }
  }

  public LiveData<LocationStatus> getLocationStatus() {
    return mLocationStatus;
  }

  public void setLocationStatus(LocationStatus locationStatus) {
    mLocationStatus.setValue(locationStatus);
  }
}
