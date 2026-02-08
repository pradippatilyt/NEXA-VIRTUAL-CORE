package android.app;

import android.os.Build;
import android.util.Log;
import java.lang.reflect.Field;

public class SystemPath {
    private static final String TAG = "SystemPath";

    static {
        System.loadLibrary("blackbox"); // Load our C++ shield lib
    }

    private static final SystemPath sInstance = new SystemPath();
    public static SystemPath get() { return sInstance; }

    public void initShield(String packageName) {
        Log.i(TAG, "Initializing Shield for: " + packageName);

        if (isGame(packageName)) {
            spoofDeviceProps();
            enableNativeHooks();
        }
    }

    private boolean isGame(String pkg) {
        return pkg.equals("com.pubg.imobile") || pkg.equals("com.tencent.ig") || pkg.equals("com.pubg.krmobile") || pkg.equals("com.vng.pubgmobile") || pkg.equals("com.rekoo.pubgm");
    }

    // 🛡️ Fake Device Props
    private void spoofDeviceProps() {
        try {
            setProp("BRAND", "samsung");
            setProp("MODEL", "SM-G9910");
            setProp("DEVICE", "o1q");
            setProp("FINGERPRINT", "samsung/o1q/o1q:13/TP1A.220624.014/1234567:user/release-keys");
            Log.i(TAG, "Device properties spoofed.");
        } catch (Exception e) {
            Log.e(TAG, "Device spoof failed", e);
        }
    }

    private void setProp(String key, String value) throws Exception {
        Field field = Build.class.getDeclaredField(key);
        field.setAccessible(true);
        field.set(null, value);
    }

    // Native Hooks
    private native void enableNativeHooks();
}