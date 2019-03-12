package net.ddns.smarttracker;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

class PhoneNumberEditTextPreference extends CustomEditTextPreference {

  private PhoneNumberUtil mPhoneNumberUtil;
  private String mCountryCode;

  public PhoneNumberEditTextPreference(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    setValidator();
  }

  public PhoneNumberEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setValidator();
  }

  public PhoneNumberEditTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setValidator();
  }

  public PhoneNumberEditTextPreference(Context context) {
    super(context);
    setValidator();
  }

  @Override
  protected void onBindDialogView(View view) {
    mPhoneNumberUtil = PhoneNumberUtil.getInstance();
    mCountryCode = CountryDetector.getInstance(getApplicationContext()).getCurrentCountryIso();

    super.onBindDialogView(view);

    PhoneNumberFormattingTextWatcher mPhoneNumberFormattingTextWatcher;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mPhoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher(mCountryCode);
    } else {
      mPhoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
    }

    final EditText editText = view.findViewById(android.R.id.edit);
    editText.setInputType(InputType.TYPE_CLASS_PHONE);
    editText.removeTextChangedListener(mPhoneNumberFormattingTextWatcher);
    editText.addTextChangedListener(mPhoneNumberFormattingTextWatcher);
  }

  private void setValidator() {
    setValidator(
        new Validator() {
          @Override
          public boolean isTextValid(String text) {
            final EditText editText = getEditText();
            if (text.trim().isEmpty()) {
              editText.setError(getString(R.string.err_phone_empty));
              return false;
            }
            try {
              Phonenumber.PhoneNumber phoneNumber = mPhoneNumberUtil.parse(text, mCountryCode);
              if (mPhoneNumberUtil.isValidNumber(phoneNumber)) {
                return true;
              } else {
                onInvalidPhone(editText);
                return false;
              }
            } catch (NumberParseException e) {
              onInvalidPhone(editText);
              return false;
            }
          }
        });
  }

  private void onInvalidPhone(EditText editText) {
    editText.setError(getString(R.string.err_phone_invalid));
  }
}
