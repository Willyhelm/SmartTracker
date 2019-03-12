package net.ddns.smarttracker;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

  private final LiveData<LocationStatus> mLocationStatus;

  public MainViewModel() {
    mLocationStatus = Repository.getInstance().getLocationStatus();
  }

  public LiveData<LocationStatus> getLocationStatus() {
    return mLocationStatus;
  }

  public void onCheckedChanged(Context context, boolean isChecked) {
    Context appContext = context.getApplicationContext();
    if (isChecked) {
      ServiceManager.getInstance(appContext).requestLocationUpdates();
    } else {
      ServiceManager.getInstance(appContext).removeLocationUpdates();
    }
  }
}
