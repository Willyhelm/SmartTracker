package net.ddns.smarttracker;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.Locale;

public class CountryDetector {

  private static final String DEFAULT_COUNTRY_ISO = "IT";
  private static final Object sLock = new Object();

  private static CountryDetector sInstance;

  private final TelephonyManager mTelephonyManager;

  private CountryDetector(Context context) {
    mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
  }

  public static CountryDetector getInstance(Context appContext) {
    synchronized (sLock) {
      if (sInstance == null) {
        sInstance = new CountryDetector(appContext.getApplicationContext());
      }
      return sInstance;
    }
  }

  public String getCurrentCountryIso() {
    String result = null;
    if (isNetworkCountryCodeAvailable()) {
      result = getNetworkBasedCountryIso();
    }
    if (TextUtils.isEmpty(result)) {
      result = getSimBasedCountryIso();
    }
    if (TextUtils.isEmpty(result)) {
      result = getLocaleBasedCountryIso();
    }
    if (TextUtils.isEmpty(result)) {
      result = DEFAULT_COUNTRY_ISO;
    }
    return result.toUpperCase();
  }

  private String getNetworkBasedCountryIso() {
    return mTelephonyManager.getNetworkCountryIso();
  }

  private String getSimBasedCountryIso() {
    return mTelephonyManager.getSimCountryIso();
  }

  private String getLocaleBasedCountryIso() {
    return Locale.getDefault().getCountry();
  }

  private boolean isNetworkCountryCodeAvailable() {
    return mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM;
  }
}
