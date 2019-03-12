package net.ddns.smarttracker;

import android.content.Context;
import android.location.Location;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {

  private BindingAdapters() {}

  @BindingAdapter({"android:text"})
  public static void setText(TextView view, LocationStatus locationStatus) {
    Context appContext = view.getContext().getApplicationContext();
    CharSequence text = "";
    switch (locationStatus.getState()) {
      case LocationStatus.WAITING:
        text = appContext.getString(R.string.state_location_waiting);
        break;
      case LocationStatus.UPDATED:
        text = getLocationFormattedString(appContext, locationStatus.getLocation());
        break;
      case LocationStatus.UNKNOWN:
        text = appContext.getString(R.string.state_location_unknown);
        break;
      case LocationStatus.STOPPED:
        text = appContext.getString(R.string.state_location_stopped);
        break;
      default:
        break;
    }
    if (view.getText() != text) {
      view.setText(text);
    }
  }

  private static String getLocationFormattedString(Context appContext, Location location) {
    return appContext.getString(
        R.string.state_location_updated, location.getLatitude(), location.getLongitude());
  }
}
