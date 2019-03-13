package net.ddns.smarttracker;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class PermissionFragment extends Fragment
    implements EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {

  private static final String[] PERMISSION_LOCATION_AND_SMS = {
    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECEIVE_SMS
  };
  private static final int REQUEST_CODE_LOCATION_AND_SMS = 123;
  private PermissionState mPermissionState;

  public PermissionFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final Drawable exit = ContextCompat.getDrawable(getContext(), R.drawable.launch_screen);
    final ColorDrawable enter = new ColorDrawable(-1);
    final TransitionDrawable transitionDrawable =
        new TransitionDrawable(new Drawable[] {exit, enter});

    getActivity().getWindow().setBackgroundDrawable(transitionDrawable);
    getAppBarLayout().setAlpha(0.0f);

    mPermissionState = PermissionState.getInstance(getActivity());

    if (savedInstanceState == null) {
      handlePermissionState();
    }
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    mPermissionState.setHasPermissionsResult();
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override
  public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    if (mPermissionState.isLocationPermissionGranted()
        && mPermissionState.isSmsPermissionGranted()) {
      onPermissionGranted();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
      if (mPermissionState.isLocationPermissionGranted()
          && mPermissionState.isSmsPermissionGranted()) {
        onPermissionGranted();
      } else {
        Toast.makeText(getContext().getApplicationContext(), getToastText(), Toast.LENGTH_LONG)
            .show();
        finish();
      }
    }
  }

  @Override
  public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    handlePermissionState();
  }

  @Override
  public void onRationaleAccepted(int requestCode) {}

  @Override
  public void onRationaleDenied(int requestCode) {
    finish();
  }

  private void handlePermissionState() {
    switch (mPermissionState.get()) {
      case PermissionState.GRANTED:
        onPermissionGranted();
        break;
      case PermissionState.DENIED:
        EasyPermissions.requestPermissions(
            new PermissionRequest.Builder(
                    this, REQUEST_CODE_LOCATION_AND_SMS, PERMISSION_LOCATION_AND_SMS)
                .setRationale(getDeniedRationale())
                .setNegativeButtonText(R.string.exit)
                .build());
        break;
      case PermissionState.PERMANENTLY_DENIED:
        new AppSettingsDialog.Builder(this)
            .setTitle(R.string.perm_required)
            .setRationale(getPermanentlyDeniedRationale())
            .setPositiveButton(R.string.action_settings)
            .setNegativeButton(R.string.exit)
            .build()
            .show();
        break;
      default:
        break;
    }
  }

  private void onPermissionGranted() {
    final int duration = getResources().getInteger(android.R.integer.config_shortAnimTime);

    NavHostFragment.findNavController(this)
        .navigate(R.id.action_permissionFragment_to_mainFragment);

    getBackground().startTransition(duration);
    getAppBarLayout().animate().alpha(1.0f).setDuration(duration);
  }

  private String getDeniedRationale() {
    if (mPermissionState.isLocationPermissionGranted()) {
      return getString(R.string.perm_sms_rationale);
    } else if (mPermissionState.isSmsPermissionGranted()) {
      return getString(R.string.perm_location_rationale);
    } else {
      return getString(R.string.perm_location_sms_rationale);
    }
  }

  private String getPermanentlyDeniedRationale() {
    if (mPermissionState.isLocationPermissionPermanentlyDenied()) {
      if (mPermissionState.isSmsPermissionPermanentlyDenied()) {
        return getString(R.string.perm_location_sms_go_to_settings);
      } else {
        return getString(R.string.perm_location_go_to_settings);
      }
    } else {
      return getString(R.string.perm_sms_go_to_settings);
    }
  }

  private CharSequence getToastText() {
    final Resources resources = getResources();
    return mPermissionState.isLocationPermissionPermanentlyDenied()
            && mPermissionState.isSmsPermissionPermanentlyDenied()
        ? resources.getQuantityString(R.plurals.required_permission_missing, 2)
        : resources.getQuantityString(R.plurals.required_permission_missing, 1);
  }

  private AppBarLayout getAppBarLayout() {
    return getActivity().findViewById(R.id.app_bar_layout);
  }

  private TransitionDrawable getBackground() {
    return (TransitionDrawable) getActivity().getWindow().getDecorView().getBackground();
  }

  private void finish() {
    getActivity().finish();
  }
}
