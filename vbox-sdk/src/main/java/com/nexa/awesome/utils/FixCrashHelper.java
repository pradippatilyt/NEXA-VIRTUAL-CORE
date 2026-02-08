package com.nexa.awesome.utils;

import android.util.Log;

import java.io.File;

import com.nexa.awesome.app.BActivityThread;
import com.nexa.awesome.core.env.BEnvironment;

public class FixCrashHelper {
    private static final String TAG = "FixCrashHelper";

    /**
     * Entry point – call this for any app, it will auto-detect PUBG/BGMI variants.
     */
    public static void tryApply(String packageName) {
        if (isTargetGame(packageName)) {
            try {
                int userId = BActivityThread.getUserId();
                File internalBase = BEnvironment.getDataFilesDir(packageName, userId).getParentFile();
                if (internalBase == null) {
                    Log.w(TAG, "Sandbox path not found for " + packageName);
                    return;
                }
                applyFix(internalBase);
                Log.i(TAG, "Crash fix applied for " + packageName);
            } catch (Throwable t) {
                Log.e(TAG, "Crash fix failed for " + packageName, t);
            }
        } else {
            Log.d(TAG, "Not a PUBG/BGMI package: " + packageName);
        }
    }
    
    private static boolean isTargetGame(String pkg) {
        ////////// BGMI (India) /////////////////////  Global.  /////////////////////// Korea + Japan. ////////////////////// Vietnam //////////////////////////////// Taiwan.  //////////////
        return "com.pubg.imobile".equals(pkg) || "com.tencent.ig".equals(pkg) || "com.pubg.krmobile".equals(pkg) || "com.vng.pubgmobile".equals(pkg) || "com.rekoo.pubgm".equals(pkg);
    }

    /**
     * Neutralize crash / log folders – no delete, just block.
     */
    private static void applyFix(File base) {
        neutralize(new File(base, "files/obblib"));
        neutralize(new File(base, "files/xlog"));
        neutralize(new File(base, "app_bugly"));
        neutralize(new File(base, "app_crashrecord"));
        neutralize(new File(base, "app_crashSight"));
        neutralize(new File(base, "files/ano_tmp"));
        // Extra folders seen in new builds
        neutralize(new File(base, "files/tss_tmp"));
        neutralize(new File(base, "files/tss_reports"));
        neutralize(new File(base, "files/tss_log"));
    }
    
    private static void neutralize(File file) {
        try {
            if (file.exists()) {
                if (file.isDirectory()) {
                    File backup = new File(file.getAbsolutePath() + "_bak");
                    boolean renamed = file.renameTo(backup);
                    Log.d(TAG, "Renamed dir to backup: " + renamed);
                } else {
                    file.delete();
                }
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtils.chmod(file.getAbsolutePath(), 0);
            Log.d(TAG, "Neutralized: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to neutralize " + file.getAbsolutePath(), e);
        }
    }
}