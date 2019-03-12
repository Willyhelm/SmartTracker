package net.ddns.smarttracker;

import android.location.Location;

public class LocationStatus {

  public static final int STOPPED = 0;
  public static final int WAITING = 1;
  public static final int UPDATED = 2;
  public static final int UNKNOWN = 3;

  private final int mState;
  private final Location mLocation;

  private LocationStatus(int state, Location location) {
    mState = state;
    mLocation = location;
  }

  public static LocationStatus stopped() {
    return new LocationStatus(STOPPED, null);
  }

  public static LocationStatus waiting() {
    return new LocationStatus(WAITING, null);
  }

  public static LocationStatus updated(Location location) {
    return new LocationStatus(UPDATED, location);
  }

  public static LocationStatus unknown() {
    return new LocationStatus(UNKNOWN, null);
  }

  public int getState() {
    return mState;
  }

  public Location getLocation() {
    return mLocation;
  }
}
