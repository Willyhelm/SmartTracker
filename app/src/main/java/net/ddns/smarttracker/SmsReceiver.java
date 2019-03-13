package net.ddns.smarttracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import androidx.preference.PreferenceManager;

public class SmsReceiver extends BroadcastReceiver {

  private Context mAppContext;

  @Override
  public void onReceive(Context context, Intent intent) {
    mAppContext = context.getApplicationContext();
    final Bundle bundle = intent.getExtras();
    final String format = bundle.getString("format");
    final Object[] pdus = (Object[]) bundle.get("pdus");
    final SmsMessage[] smsMessage;
    final StringBuilder stringBuilder = new StringBuilder();
    final String message;
    final String phone;
    if (pdus != null) {
      smsMessage = new SmsMessage[pdus.length];
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        for (int i = 0; i < smsMessage.length; i++) {
          smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
          stringBuilder.append(smsMessage[i].getMessageBody());
        }
      } else {
        for (int i = 0; i < smsMessage.length; i++) {
          smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
          stringBuilder.append(smsMessage[i].getMessageBody());
        }
      }

      message = stringBuilder.toString();
      phone = smsMessage[0].getOriginatingAddress();

      handleMessage(message, phone);
    }
  }

  private void handleMessage(String message, String phone) {
    if (isAuthPhone(phone)) {
      if (message.equals(getRequestLocationUpdatesMessage())) {
        ServiceManager.getInstance(mAppContext).requestLocationUpdates();
      } else if (message.equals(getRemoveLocationUpdatesMessage())) {
        ServiceManager.getInstance(mAppContext).removeLocationUpdates();
      }
    }
  }

  private boolean isAuthPhone(String phone) {
    return !isAllowOnlyAuthPhone() || isSameNumber(phone, getAuthPhone());
  }

  private boolean isSameNumber(CharSequence firstNumber, CharSequence secondNumber) {
    final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    final String defaultRegion = CountryDetector.getInstance(mAppContext).getCurrentCountryIso();
    try {
      final Phonenumber.PhoneNumber firstNumberIn =
          phoneNumberUtil.parse(firstNumber, defaultRegion);
      final Phonenumber.PhoneNumber secondNumberIn =
          phoneNumberUtil.parse(secondNumber, defaultRegion);
      return phoneNumberUtil.isNumberMatch(firstNumberIn, secondNumberIn)
          == PhoneNumberUtil.MatchType.EXACT_MATCH;
    } catch (NumberParseException e) {
      return false;
    }
  }

  private boolean isAllowOnlyAuthPhone() {
    return PreferenceManager.getDefaultSharedPreferences(mAppContext.getApplicationContext())
        .getBoolean(SettingsFragment.KEY_PREF_ALLOW_ONLY_AUTH_PHONE, false);
  }

  private String getAuthPhone() {
    return PreferenceManager.getDefaultSharedPreferences(mAppContext.getApplicationContext())
        .getString(SettingsFragment.KEY_PREF_AUTH_PHONE, null);
  }

  private String getRequestLocationUpdatesMessage() {
    return PreferenceManager.getDefaultSharedPreferences(mAppContext.getApplicationContext())
        .getString(
            SettingsFragment.KEY_PREF_REQUEST_LOCATION_UPDATES_MESSAGE,
            mAppContext.getString(R.string.request_location_updates_default));
  }

  private String getRemoveLocationUpdatesMessage() {
    return PreferenceManager.getDefaultSharedPreferences(mAppContext.getApplicationContext())
        .getString(
            SettingsFragment.KEY_PREF_REMOVE_LOCATION_UPDATES_MESSAGE,
            mAppContext.getString(R.string.remove_location_updates_default));
  }
}
