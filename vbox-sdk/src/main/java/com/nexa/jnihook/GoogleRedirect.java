package com.nexa.jnihook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.nexa.awesome.NexaCore;
import com.nexa.awesome.app.BActivityThread;
import com.nexa.awesome.core.NativeCore;
import com.nexa.awesome.core.env.BEnvironment;
import com.nexa.awesome.utils.FileUtils;
import com.nexa.awesome.utils.TrieTree;

public class GoogleRedirect {
    
    public static final String TAG = "GoogleRedirect";
    private static final GoogleRedirect sGoogleRedirect = new GoogleRedirect();
    private static final TrieTree mTrieTree = new TrieTree();
    private static final TrieTree sBlackTree = new TrieTree();
    private final Map<String, String> mRedirectMap = new LinkedHashMap<>();

    private static final Map<String, Map<String, String>> sCachePackageRedirect = new HashMap<>();
    
    public static GoogleRedirect get() {
        return sGoogleRedirect;
    }
    
    public void addRedirect(String origPath, String redirectPath) {
        if (TextUtils.isEmpty(origPath) || TextUtils.isEmpty(redirectPath) || mRedirectMap.get(origPath) != null)
            return;
        //Add the key to TrieTree
        mTrieTree.add(origPath);
        mRedirectMap.put(origPath, redirectPath);
        File redirectFile = new File(redirectPath);
        if (!redirectFile.exists()) {
            FileUtils.mkdirs(redirectPath);
        }
        NativeCore.addIORule(origPath, redirectPath);
    }

    public void addBlackRedirect(String path) {
        if (TextUtils.isEmpty(path))
            return;
        sBlackTree.add(path);
    }
    
    public static boolean isBrowserPackage(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        return packageName.equals("mark.via.gp") || packageName.contains("browser") || packageName.contains("chrome") || packageName.contains("webview") || packageName.contains("via");
    }
    
    public static boolean isBrowserPackage2(String packageName) {
        if (TextUtils.isEmpty(packageName)) return false;
        return packageName.equals("com.android.browser") || packageName.contains("chrome") || packageName.contains("firefox") || packageName.contains("opera") || packageName.contains("edge");
    }
    
    // Redirect for browsers (safe mode)
    public static void enableBrowserSafeRedirect(Context context, String browserPackageName) {
        Map<String, String> rule = new LinkedHashMap<>();
        Set<String> blackRule = new HashSet<>();
        try {
            int systemUserId = NexaCore.getHostUserId();
            List<ApplicationInfo> installedApplications = NexaCore.getBPackageManager().getInstalledApplications(PackageManager.GET_META_DATA, BActivityThread.getUserId());
            // ONLY enable essential redirections for browsers
            for (ApplicationInfo packageInfo : installedApplications) {
                // Always allow library redirection (safe for browsers)
                rule.put(String.format("/data/data/%s/lib", packageInfo.packageName), packageInfo.nativeLibraryDir);
                rule.put(String.format("/data/user/%d/%s/lib", systemUserId, packageInfo.packageName), packageInfo.nativeLibraryDir);
                // SKIP data directory redirection for ALL apps when we're in a browser context
                // This prevents the ERR_CACHE_MISS error
                if (!isBrowserPackage(packageInfo.packageName)) {
                    // Only redirect data directories for non-browser apps
                    rule.put(String.format("/data/data/%s", packageInfo.packageName), packageInfo.dataDir);
                    rule.put(String.format("/data/user/%d/%s", systemUserId, packageInfo.packageName), packageInfo.dataDir);
                }
            }

            if (NexaCore.getContext().getExternalCacheDir() != null && context.getExternalCacheDir() != null) {
                File externalStorageDirectory = BEnvironment.getExternalStorageDirectory();
                // sdcard redirection - but exclude browser cache directories
                rule.put("/sdcard", externalStorageDirectory.getAbsolutePath());
                rule.put(String.format("/storage/emulated/%d", systemUserId), externalStorageDirectory.getAbsolutePath());
                // Add blacklist rules to prevent browser cache interference
                blackRule.add("/sdcard/Android");
                blackRule.add("/sdcard/Android/data");
                blackRule.add("/sdcard/Android/data/" + browserPackageName);
                blackRule.add("/sdcard/Android/media");
                blackRule.add("/sdcard/Android/obb");
                blackRule.add("/sdcard/Pictures");
                blackRule.add(String.format("/storage/emulated/%d/Pictures", systemUserId));
                // Blacklist browser-specific cache directories
                blackRule.add("/data/data/" + browserPackageName + "/cache");
                blackRule.add("/data/data/" + browserPackageName + "/app_webview");
                blackRule.add("/data/data/" + browserPackageName + "/files");
            }
            
            if (NexaCore.get().setHideRoot()) {
                hideRoot(rule);
            }
            proc(rule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (String key : rule.keySet()) {
            get().addRedirect(key, rule.get(key));
        }
        for (String s : blackRule) {
            get().addBlackRedirect(s);
        }
        NativeCore.enableIO();
    }

    // Hide root paths
    public static void hideRoot(Map<String, String> rule) {
        rule.put("/system/app/Superuser.apk", "/system/app/Superuser.apk-fake");
        rule.put("/sbin/su", "/sbin/su-fake");
        rule.put("/system/bin/su", "/system/bin/su-fake");
        rule.put("/system/xbin/su", "/system/xbin/su-fake");
        rule.put("/data/local/xbin/su", "/data/local/xbin/su-fake");
        rule.put("/data/local/bin/su", "/data/local/bin/su-fake");
        rule.put("/system/sd/xbin/su", "/system/sd/xbin/su-fake");
        rule.put("/system/bin/failsafe/su", "/system/bin/failsafe/su-fake");
        rule.put("/data/local/su", "/data/local/su-fake");
        rule.put("/su/bin/su", "/su/bin/su-fake");
    }

    // Fix proc path redirection
    public static void proc(Map<String, String> rule) {
        int appPid = BActivityThread.getAppPid();
        int pid = Process.myPid();

        String selfProc = "/proc/self/";
        String proc = "/proc/" + pid + "/";

        String cmdline = new File(BEnvironment.getProcDir(appPid), "cmdline").getAbsolutePath();
        rule.put(proc + "cmdline", cmdline);
        rule.put(selfProc + "cmdline", cmdline);
    }
    

}
