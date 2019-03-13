package net.ddns.smarttracker;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends CustomPreferenceFragmentCompat {

  public static final String KEY_PREF_ALLOW_ONLY_AUTH_PHONE = "pref_allow_only_auth_phone";
  public static final String KEY_PREF_AUTH_PHONE = "pref_auth_phone";
  public static final String KEY_PREF_REQUEST_LOCATION_UPDATES_MESSAGE =
      "pref_request_location_updates_message";
  public static final String KEY_PREF_REMOVE_LOCATION_UPDATES_MESSAGE =
      "pref_remove_location_updates_message";

  private static final String KEY_DELETE_LOCATIONS = "delete_locations";
  private static final int EDITTEXT_MAX_LENGTH = 20;

  private SwitchPreferenceCompat allowOnlyAuthPhone;
  private PhoneNumberEditTextPreference authPhone;
  private CustomEditTextPreference requestLocationUpdatesMessage;
  private CustomEditTextPreference removeLocationUpdatesMessage;
  private Preference deleteLocations;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    allowOnlyAuthPhone = findPreference(KEY_PREF_ALLOW_ONLY_AUTH_PHONE);
    authPhone = findPreference(KEY_PREF_AUTH_PHONE);
    requestLocationUpdatesMessage = findPreference(KEY_PREF_REQUEST_LOCATION_UPDATES_MESSAGE);
    removeLocationUpdatesMessage = findPreference(KEY_PREF_REMOVE_LOCATION_UPDATES_MESSAGE);
    deleteLocations = findPreference(KEY_DELETE_LOCATIONS);

    initAllowOnlySelectedPhone();
    initAuthPhone();
    initRequestLocationUpdatesMessage();
    initRemoveLocationUpdatesMessage();
    initDeleteLocations();

    maybeShowAuthPhone();
  }

  private void initAllowOnlySelectedPhone() {
    allowOnlyAuthPhone.setOnPreferenceChangeListener(
        new Preference.OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            final boolean isChecked = (Boolean) newValue;
            if (isChecked) {
              getPreferenceManager().showDialog(authPhone);
              return true;
            } else {
              authPhone.setVisible(false);
              authPhone.setText(null);
              return true;
            }
          }
        });
  }

  private void initAuthPhone() {
    authPhone.setOnDialogShowListener(
        new DialogInterface.OnShowListener() {
          @Override
          public void onShow(DialogInterface dialog) {
            if (authPhone.getText() == null) {
              ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
          }
        });

    authPhone.setOnDialogCloseListener(
        new CustomEditTextPreference.OnDialogCloseListener() {
          @Override
          public void onDialogClose(boolean positiveResult) {
            if (positiveResult) {
              allowOnlyAuthPhone.setChecked(true);
              authPhone.setVisible(true);
            } else {
              if (authPhone.getText() == null) {
                allowOnlyAuthPhone.setChecked(false);
              }
            }
          }
        });
  }

  private void initRequestLocationUpdatesMessage() {
    requestLocationUpdatesMessage.setOnBindEditTextListener(
        new EditTextPreference.OnBindEditTextListener() {
          @Override
          public void onBindEditText(@NonNull EditText editText) {
            customizeEditText(editText);
          }
        });

    requestLocationUpdatesMessage.setValidator(
        new CustomEditTextPreference.Validator() {
          @Override
          public boolean isTextValid(String text) {
            final EditText editText = requestLocationUpdatesMessage.getEditText();
            if (text.trim().equals(removeLocationUpdatesMessage.getText())) {
              editText.setError(getString(R.string.err_request_message_same));
              return false;
            } else if (text.trim().isEmpty()) {
              editText.setError(getString(R.string.err_request_message_empty));
              return false;
            }
            return true;
          }
        });
  }

  private void initRemoveLocationUpdatesMessage() {
    removeLocationUpdatesMessage.setOnBindEditTextListener(
        new EditTextPreference.OnBindEditTextListener() {
          @Override
          public void onBindEditText(@NonNull EditText editText) {
            customizeEditText(editText);
          }
        });

    removeLocationUpdatesMessage.setValidator(
        new CustomEditTextPreference.Validator() {
          @Override
          public boolean isTextValid(String text) {
            final EditText editText = removeLocationUpdatesMessage.getEditText();
            if (text.trim().equals(requestLocationUpdatesMessage.getText())) {
              editText.setError(getString(R.string.err_remove_message_same));
              return false;
            } else if (text.trim().isEmpty()) {
              editText.setError(getString(R.string.err_remove_message_empty));
              return false;
            }
            return true;
          }
        });
  }

  private void initDeleteLocations() {
    deleteLocations.setOnPreferenceClickListener(
        new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            final MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(getActivity())
                    .setTitle(R.string.delete_locations)
                    .setMessage(R.string.delete_locations_message)
                    .setPositiveButton(
                        R.string.delete,
                        new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                            RetrofitClient.getInstance()
                                .getApiService()
                                .deleteLocations()
                                .enqueue(
                                    new Callback<DeleteLocationsResponse>() {
                                      @Override
                                      public void onResponse(
                                          Call<DeleteLocationsResponse> call,
                                          final Response<DeleteLocationsResponse> response) {
                                        final MainActivity activity = MainActivity.get();
                                        final CharSequence text =
                                            response.isSuccessful()
                                                ? response.body().getMessage()
                                                : activity.getString(
                                                    R.string.err_network_unexpected);
                                        notifyDeleteLocations(text);
                                      }

                                      @Override
                                      public void onFailure(
                                          Call<DeleteLocationsResponse> call, Throwable t) {
                                        final MainActivity activity = MainActivity.get();
                                        final CharSequence text;
                                        if (t instanceof SocketTimeoutException) {
                                          text =
                                              activity.getString(
                                                  R.string.err_network_unreachable_server);
                                        } else if (t instanceof UnknownHostException) {
                                          text =
                                              activity.getString(
                                                  R.string.err_network_no_connection);
                                        } else {
                                          text =
                                              activity.getString(R.string.err_network_unexpected);
                                        }
                                        notifyDeleteLocations(text);
                                      }
                                    });
                          }
                        })
                    .setNegativeButton(android.R.string.cancel, null);
            builder.show();
            return true;
          }
        });
  }

  private void maybeShowAuthPhone() {
    authPhone.setVisible(allowOnlyAuthPhone.isChecked());
  }

  private void customizeEditText(EditText editText) {
    editText.setInputType(
        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
    editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(EDITTEXT_MAX_LENGTH)});
  }

  private void notifyDeleteLocations(CharSequence text) {
    final View content = MainActivity.getContent();
    if (content == null) {
      final Context appContext = MainActivity.get().getApplicationContext();
      Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show();
    } else {
      Snackbar.make(content, text, Snackbar.LENGTH_LONG).show();
    }
  }
}
