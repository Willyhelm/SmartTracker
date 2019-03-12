package net.ddns.smarttracker;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.ddns.smarttracker.databinding.FragmentMainBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

public class MainFragment extends Fragment {

  public MainFragment() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    FragmentMainBinding fragmentMainBinding =
        FragmentMainBinding.inflate(inflater, container, false);
    fragmentMainBinding.setLifecycleOwner(this);
    MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    fragmentMainBinding.setViewModel(mainViewModel);
    setHasOptionsMenu(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && PermissionState.getInstance(getActivity()).get() != PermissionState.GRANTED) {
      NavHostFragment.findNavController(this)
          .navigate(R.id.action_mainFragment_to_permissionFragment);
    }
    return fragmentMainBinding.getRoot();
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_main, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_map:
        openMap();
        return true;
      case R.id.action_settings:
        NavHostFragment.findNavController(this)
            .navigate(R.id.action_mainFragment_to_settingsFragment);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void openMap() {
    Context context = getContext();
    Context appContext = context.getApplicationContext();
    CustomTabsIntent customTabsIntent =
        new CustomTabsIntent.Builder()
            .setCloseButtonIcon(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back))
            .setToolbarColor(ContextCompat.getColor(appContext, R.color.colorPrimary))
            .setShowTitle(true)
            .setStartAnimations(appContext, android.R.anim.fade_in, android.R.anim.fade_out)
            .setExitAnimations(appContext, android.R.anim.fade_in, android.R.anim.fade_out)
            .build();

    customTabsIntent.launchUrl(context, Uri.parse(ApiService.BASE_URL));
  }
}
