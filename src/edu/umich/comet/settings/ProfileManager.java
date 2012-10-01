package edu.umich.comet.settings;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/* It use to be that we kept around profile information for applications keyed
 * by their user id.  Therefore when applications were uninstalled we needed to
 * delete the profile information to 1) free up space and 2) avoid future
 * applications from starting with its profile information.
 */
public class ProfileManager {
  private static String TAG = "ProfileManager";

  public static void purgeProfileData(int uid) {
/*
    if(uid >= 0) try {
      java.lang.Process p = Runtime.getRuntime().exec("su");
      DataOutputStream os = new DataOutputStream(p.getOutputStream());
      os.writeBytes("rf -f /data/profiles." + uid + ".dat\n");
      os.writeBytes("exit\n");
      os.flush();
      p.waitFor();
      if(p.exitValue() != 0) {
        Log.i(TAG, "Non-zero exit value removing profile data");
      }
    } catch(InterruptedException e) {
      Log.i(TAG, "Error removing profile data");
    } catch(IOException e) {
      Log.i(TAG, "Error removing profile data");
    }
*/
  }
}
