package edu.umich.comet.settings;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Settings extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();

    LinearLayout appList;
    appList = new LinearLayout(this);
    appList.setOrientation(LinearLayout.VERTICAL);

    HashSet<Integer> processedUids = new HashSet<Integer>();
    PackageManager pm = getPackageManager();
    for(PackageInfo pkg : pm.getInstalledPackages(0)) {
      if(processedUids.add(pkg.applicationInfo.uid)) {
        appList.addView(new AppView(this, pkg, pkg.applicationInfo.uid));
      }
    }

    ScrollView scrollView = new ScrollView(this);
    scrollView.addView(appList);
    setContentView(scrollView);
  }

  private static class AppView extends LinearLayout {
    public int uid;

    public boolean isExempt(boolean toggle) {
      boolean exempt = false;
      try {
        Process p = Runtime.getRuntime().exec(new String[] {
            "/system/bin/cometmanager", toggle ? "toggle" : "check", "" + uid});
        exempt = p.waitFor() != 0;
      } catch(IOException e) {
        e.printStackTrace();
      } catch(InterruptedException e) {
        e.printStackTrace();
      }
      return exempt;
    }

    public AppView(Activity activity, PackageInfo pkg, int uid) {
      super(activity);
      this.uid = uid;

      setMinimumHeight(100);
      setOrientation(LinearLayout.HORIZONTAL);
      ImageView imageView = new ImageView(activity);
      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
      imageView.setAdjustViewBounds(true);
      imageView.setMaxHeight(80);
      imageView.setMaxWidth(80);
      imageView.setMinimumWidth(100);
      imageView.setLayoutParams(new ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.FILL_PARENT));
      TextView textView = new TextView(activity);
      textView.setGravity(Gravity.CENTER_VERTICAL);
      textView.setLayoutParams(new ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.FILL_PARENT,
          ViewGroup.LayoutParams.FILL_PARENT));
      addView(imageView);
      addView(textView);

      setBackgroundColor(isExempt(false) ? 0xFF3F3F3F : 0xFF000000);
      setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          setBackgroundColor(isExempt(true) ? 0xFF3F3F3F : 0xFF000000);
        }
      });

      setFocusable(true);

      PackageManager pm = getContext().getPackageManager();
      try {
        imageView.setImageDrawable(pm.getApplicationIcon(pkg.packageName));
      } catch(PackageManager.NameNotFoundException e) {
      }
      textView.setText(pkg.packageName);
    }
  }

}
