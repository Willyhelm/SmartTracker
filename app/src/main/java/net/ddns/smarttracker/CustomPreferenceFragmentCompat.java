package net.ddns.smarttracker;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class CustomPreferenceFragmentCompat extends PreferenceFragmentCompat {

  private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";

  @Override
  public void onDisplayPreferenceDialog(Preference preference) {
    final DialogFragment f;
    if (preference instanceof CustomEditTextPreference) {
      f =
          CustomEditTextPreference.CustomEditTextPreferenceDialogFragment.newInstance(
              preference.getKey());
    } else {
      super.onDisplayPreferenceDialog(preference);
      return;
    }
    f.setTargetFragment(this, 0);
    f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
  }
}
