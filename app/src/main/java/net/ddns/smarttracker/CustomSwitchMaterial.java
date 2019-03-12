package net.ddns.smarttracker;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.Nullable;

public class CustomSwitchMaterial extends SwitchMaterial {

  private OnCheckedChangeListener mOnCheckedChangeListener;

  public CustomSwitchMaterial(Context context) {
    super(context);
  }

  public CustomSwitchMaterial(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomSwitchMaterial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
    mOnCheckedChangeListener = listener;
    super.setOnCheckedChangeListener(listener);
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    super.setOnCheckedChangeListener(null);
    super.onRestoreInstanceState(state);
    super.setOnCheckedChangeListener(mOnCheckedChangeListener);
  }
}
