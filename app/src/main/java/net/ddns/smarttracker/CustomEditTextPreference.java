package net.ddns.smarttracker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;

public class CustomEditTextPreference extends EditTextPreference {

  private final EditTextWatcher mTextWatcher = new EditTextWatcher();
  private DialogInterface.OnShowListener mOnShowListener;
  private OnDialogCloseListener mOnDialogCloseListener;
  private Validator mValidator;
  private CustomEditTextPreferenceDialogFragment mFragment;

  public CustomEditTextPreference(
      Context context, AttributeSet attrs, final int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public CustomEditTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomEditTextPreference(Context context) {
    super(context);
  }

  public EditText getEditText() {
    if (mFragment != null) {
      final Dialog dialog = mFragment.getDialog();
      if (dialog != null) {
        return (EditText) dialog.findViewById(android.R.id.edit);
      }
    }
    return null;
  }

  public boolean isDialogOpen() {
    return getDialog() != null && getDialog().isShowing();
  }

  public Dialog getDialog() {
    return mFragment == null ? null : mFragment.getDialog();
  }

  @CallSuper
  protected void onBindDialogView(View view) {
    final EditText editText = view.findViewById(android.R.id.edit);
    editText.setInputType(InputType.TYPE_CLASS_TEXT);
    editText.setSelection(editText.getText().length());
    if (mValidator != null) {
      editText.removeTextChangedListener(mTextWatcher);
      editText.addTextChangedListener(mTextWatcher);
    }
  }

  @CallSuper
  protected void onDialogClosed(boolean positiveResult) {
    if (mOnDialogCloseListener != null) {
      mOnDialogCloseListener.onDialogClose(positiveResult);
    }
  }

  @CallSuper
  protected void onDialogShow(Dialog dialog) {
    if (mOnShowListener != null) {
      dialog.setOnShowListener(mOnShowListener);
    }
  }

  private void setFragment(CustomEditTextPreferenceDialogFragment fragment) {
    mFragment = fragment;
  }

  public void setOnDialogShowListener(DialogInterface.OnShowListener listener) {
    mOnShowListener = listener;
  }

  public void setOnDialogCloseListener(OnDialogCloseListener listener) {
    mOnDialogCloseListener = listener;
  }

  public void setValidator(Validator validator) {
    mValidator = validator;
  }

  protected final Context getApplicationContext() {
    return getContext().getApplicationContext();
  }

  protected final String getString(@StringRes int resId) {
    return getApplicationContext().getString(resId);
  }

  public interface OnDialogCloseListener {
    void onDialogClose(boolean positiveResult);
  }

  public interface Validator {
    boolean isTextValid(String text);
  }

  public static class CustomEditTextPreferenceDialogFragment
      extends EditTextPreferenceDialogFragmentCompat {

    public static CustomEditTextPreferenceDialogFragment newInstance(String key) {
      final CustomEditTextPreferenceDialogFragment fragment =
          new CustomEditTextPreferenceDialogFragment();
      final Bundle b = new Bundle(1);
      b.putString(ARG_KEY, key);
      fragment.setArguments(b);
      return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
      super.onPrepareDialogBuilder(builder);
      getCustomEditTextPreference().setFragment(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Dialog dialog = super.onCreateDialog(savedInstanceState);
      getCustomEditTextPreference().onDialogShow(dialog);
      return dialog;
    }

    @Override
    protected void onBindDialogView(View view) {
      super.onBindDialogView(view);
      getCustomEditTextPreference().onBindDialogView(view);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
      if (positiveResult) {
        final String value = getCustomEditTextPreference().getEditText().getText().toString();
        final CustomEditTextPreference preference = getCustomEditTextPreference();
        if (preference.callChangeListener(value.trim())) {
          preference.setText(value.trim());
        }
      }
      getCustomEditTextPreference().onDialogClosed(positiveResult);
    }

    public CustomEditTextPreference getCustomEditTextPreference() {
      return (CustomEditTextPreference) getPreference();
    }
  }

  private class EditTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
      if (mValidator != null) {
        final boolean valid = mValidator.isTextValid(s.toString());
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(valid);
      }
    }
  }
}
