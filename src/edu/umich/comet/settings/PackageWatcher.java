package edu.umich.comet.settings;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.InputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PackageWatcher extends BroadcastReceiver {
  private static final int NOTIFICATION_OFFLOAD = 1;

  /* This must match what's in tcpmux.c so we can detect when tcpmux has failed
   * to connect to the remote server. */
  private static final int RTT_INFINITE = 10*1000*1000;

  private static final int UPDATE_INTERVAL = 30*1000;

  private static int lastcwb = -1;
  private static int lastcrb = -1;

  private static int readNL(InputStream is) throws IOException {
    int res = 0;
    for(int i = 0; i < 4; i++) {
      res = res << 8;
      int v = is.read();
      if(v == -1) return -1;
      res = res | v;
    }
    return res;
  }

  @Override
  public void onReceive(final Context context, Intent intent) {
    if(intent.getAction() == null) {
      new Thread() {
        public void run() {
          boolean tcpmuxAlive = false;
          int rtt = 0;
          int rttvar = 0;
          int cwb = 0;
          int crb = 0;
          try {
            Socket s = new Socket(InetAddress.getLocalHost(), 5554);
            rtt = readNL(s.getInputStream());
            rttvar = readNL(s.getInputStream());
            cwb = readNL(s.getInputStream());
            crb = readNL(s.getInputStream());
            s.close();
            tcpmuxAlive = true;
          } catch(IOException e) {
          }
          
    /*
          try {
            Runtime.getRuntime().exec(
                "/system/bin/tcpmux --daemon --control 5554 --retry " +
                "5555 spidermonkey.eecs.umich.edu:5556");
          } catch(IOException e) {
            e.printStackTrace();
          }
    */
          int icon;
          String message;
          String status;
          if(!tcpmuxAlive) {
            icon = R.drawable.ic_menu_sad;
            message = "Local offloading daemon offline";
            status = "Daemon offline";
          } else if(rtt == RTT_INFINITE) {
            icon = R.drawable.ic_menu_phone;
            message = "Unable to connect to offload server";
            status = "No server available";
          } else if(lastcwb == -1 || (lastcwb == cwb && lastcrb == crb)) {
            icon = R.drawable.ic_menu_cloud;
            message = "Offloading idle";
            status = "Offloading idle.  RTT = " + (rtt / 1000) +
                            "ms +/- " + (rttvar / 1000);
          } else {
            icon = R.drawable.ic_menu_star;
            message = "Actively offloading";
            status = "Actively offloading.  RTT = " + (rtt / 1000) +
                            "ms +/- " + (rttvar / 1000);
          }
          lastcwb = cwb;
          lastcrb = crb;

          Notification notification = new Notification(icon, message,
                                                       System.currentTimeMillis());
          notification.setLatestEventInfo(context, "Toe offloading status", status,
              PendingIntent.getActivity(context, 0,
                                        new Intent(context, Settings.class), 0));

          notification.flags |= Notification.FLAG_NO_CLEAR |
                                Notification.FLAG_ONGOING_EVENT;

          NotificationManager notifManager = (NotificationManager)
              context.getSystemService(Context.NOTIFICATION_SERVICE);
          notifManager.notify(NOTIFICATION_OFFLOAD, notification);
        }
      }.start();
    } else if(Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
      ProfileManager.purgeProfileData(intent.getIntExtra(Intent.EXTRA_UID, -1));
    } else if(Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
      ProfileManager.purgeProfileData(intent.getIntExtra(Intent.EXTRA_UID, -1));
    } else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      AlarmManager alarmManager = (AlarmManager)
          context.getSystemService(Context.ALARM_SERVICE);
      alarmManager.setInexactRepeating(AlarmManager.RTC,
          System.currentTimeMillis(), UPDATE_INTERVAL,
          PendingIntent.getBroadcast(context, 0,
              new Intent(context, PackageWatcher.class), 0));
    }
  }
}
