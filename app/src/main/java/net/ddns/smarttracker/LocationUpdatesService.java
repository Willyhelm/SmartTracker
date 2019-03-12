package net.ddns.smarttracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class LocationUpdatesService extends Service implements LifecycleObserver {

  public static final String ACTION_START = "net.ddns.smarttracker.action_START";
  public static final String ACTION_STOP = "net.ddns.smarttracker.action_STOP";

  private static final int NOTIFICATION_ID = 12345678;
  private static final String CHANNEL_ID = "channel_01";
  private static final long UPDATE_INTERVAL_IN_MS = 5000L;

  private FusedLocationProviderClient mFusedLocationProviderClient;
  private LocationRequest mLocationRequest;
  private LocationCallback mLocationCallback;
  private boolean mForeground;
  private boolean mStopSelf;
  private LiveData<LocationStatus> mLocationStatus;
  private ServiceManager mServiceManager;
  private NotificationManager mNotificationManager;

  @Override
  public void onCreate() {
    super.onCreate();
    ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel =
          new NotificationChannel(
              CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
      channel.setDescription(getString(R.string.channel_desc));
      mNotificationManager.createNotificationChannel(channel);
    }
    mFusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(getApplicationContext());
    mLocationStatus = Repository.getInstance().getLocationStatus();
    createLocationRequest();
    createLocationCallback();
    mServiceManager = ServiceManager.getInstance(getApplicationContext());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    switch (intent.getAction()) {
      case ACTION_START:
        if (!mForeground) startForeground(NOTIFICATION_ID, getNotification());
        mFusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback, null);
        break;
      case ACTION_STOP:
        mStopSelf = true;
        stopSelf();
        break;
      default:
        break;
    }
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    if (mStopSelf) mServiceManager.notifyStopSelf();
    mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  private void onActivityForeground() {
    mForeground = true;
    stopForeground(true);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  private void onActivityBackground() {
    mForeground = false;
    startForeground(NOTIFICATION_ID, getNotification());
  }

  private void createLocationCallback() {
    mLocationCallback =
        new LocationCallback() {
          @Override
          public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            mServiceManager.updateLocation(location);
            if (!mForeground) mNotificationManager.notify(NOTIFICATION_ID, getNotification());
          }
        };
  }

  private void createLocationRequest() {
    mLocationRequest =
        LocationRequest.create()
            .setInterval(UPDATE_INTERVAL_IN_MS)
            .setFastestInterval(UPDATE_INTERVAL_IN_MS)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  private Notification getNotification() {
    Intent activityIntent =
        new Intent(this, MainActivity.class)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

    Intent stopIntent = new Intent(this, LocationUpdatesService.class).setAction(ACTION_STOP);
    PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

    CharSequence contentText = "";
    switch (mLocationStatus.getValue().getState()) {
      case LocationStatus.WAITING:
        contentText = getString(R.string.state_location_waiting);
        break;
      case LocationStatus.UPDATED:
        contentText = getLocationFormattedString(mLocationStatus.getValue().getLocation());
        break;
      case LocationStatus.UNKNOWN:
        contentText = getString(R.string.state_location_unknown);
        break;
      default:
        break;
    }

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
            .addAction(
                R.drawable.ic_action_close,
                getString(R.string.remove_location_updates),
                stopPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(activityPendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle(getString(R.string.location_updates_on))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_stat_product_logo_smarttracker)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

    return builder.build();
  }

  private String getLocationFormattedString(Location location) {
    return getString(
        R.string.state_location_updated_notification,
        location.getLatitude(),
        location.getLongitude());
  }
}
