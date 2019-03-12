package net.ddns.smarttracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class PermissionState {

  public static final int GRANTED = 0;
  public static final int DENIED = 1;
  public static final int PERMANENTLY_DENIED = 2;
  private static final String KEY_HAS_PERMISSIONS_RESULT = "has_permissions_result";

  private static final Object sLock = new Object();
  private static PermissionState sInstance;
  private final Context mAppContext;
  private final Activity mActivity;

  private PermissionState(Activity activity) {
    mActivity = activity;
    mAppContext = mActivity.getApplicationContext();
  }

  public static PermissionState getInstance(Activity activity) {
    synchronized (sLock) {
      if (sInstance == null) {
        sInstance = new PermissionState(activity);
      }
      return sInstance;
    }
  }

  public int get() {
    if (isLocationPermissionGranted() && isSmsPermissionGranted()) {
      return GRANTED;
    }
    if (isLocationPermissionPermanentlyDenied() || isSmsPermissionPermanentlyDenied()) {
      return PERMANENTLY_DENIED;
    }
    return DENIED;
  }

  public boolean isLocationPermissionGranted() {
    return ContextCompat.checkSelfPermission(mAppContext, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED;
  }

  public boolean isSmsPermissionGranted() {
    return ContextCompat.checkSelfPermission(mAppContext, Manifest.permission.RECEIVE_SMS)
        == PackageManager.PERMISSION_GRANTED;
  }

  public boolean isLocationPermissionPermanentlyDenied() {
    return hasPermissionsResult()
        && !isLocationPermissionGranted()
        && !ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity, Manifest.permission.ACCESS_FINE_LOCATION);
  }

  public boolean isSmsPermissionPermanentlyDenied() {
    return hasPermissionsResult()
        && !isSmsPermissionGranted()
        && !ActivityCompat.shouldShowRequestPermissionRationale(
            mActivity, Manifest.permission.RECEIVE_SMS);
  }

  private boolean hasPermissionsResult() {
    return PreferenceManager.getDefaultSharedPreferences(mAppContext)
        .getBoolean(KEY_HAS_PERMISSIONS_RESULT, false);
  }

  public void setHasPermissionsResult() {
    PreferenceManager.getDefaultSharedPreferences(mAppContext)
        .edit()
        .putBoolean(KEY_HAS_PERMISSIONS_RESULT, true)
        .apply();
  }
}
