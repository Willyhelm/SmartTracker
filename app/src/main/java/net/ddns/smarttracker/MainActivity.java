package net.ddns.smarttracker;

import android.os.Bundle;
import android.view.View;

import net.ddns.smarttracker.databinding.ActivityMainBinding;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

  private static WeakReference<View> mContent;

  public static View getContent() {
    return mContent == null ? null : mContent.get();
  }

  private static void setContent(View content) {
    mContent = new WeakReference<>(content);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);
    setContent(findViewById(android.R.id.content));
    ActivityMainBinding activityMainBinding =
        DataBindingUtil.setContentView(this, R.layout.activity_main);
    setSupportActionBar(activityMainBinding.toolbar);
    NavigationUI.setupWithNavController(
        activityMainBinding.toolbar, Navigation.findNavController(this, R.id.nav_host_fragment));
  }
}
