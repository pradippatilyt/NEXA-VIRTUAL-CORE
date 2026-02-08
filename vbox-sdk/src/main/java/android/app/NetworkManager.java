package android.app;

import android.Encrypt.StringEncrypt;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import org.lsposed.lsparanoid.Obfuscate;
import com.nexa.awesome.NexaCore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Obfuscate
public class NetworkManager {

    public static volatile boolean isSdk = false;
    public static volatile String Msg = "Activation failed.";
    public static final String PREFERENCE_NAME = "license_cache";
    public static volatile String ActivationUrl = "https://blackbox360.business/api/connect.php";
    
    public static boolean getActivatedSdk() {
        if (getActivatedHidden()) return true;

        SharedPreferences sp = NexaCore.getContext().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean cached = sp.getBoolean("activated", false);
        String expiry = sp.getString("expiry", null);

        if (cached && expiry != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date expiryDate = sdf.parse(expiry);
                if (expiryDate != null) {
                    return System.currentTimeMillis() < expiryDate.getTime();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getServerMessage() {
        return Msg;
    }
    
    public static String 获取接口地址() {
        return "https://blackbox360.business/api/connect.php";
    }
    
    public static void ismsg(String msg) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(NexaCore.getContext(), msg, Toast.LENGTH_LONG).show());
    }
    
    public static void setActivatedHidden(boolean value) {
        try {
            Class<?> clazz = Class.forName("android.app.NetworkManager");
            java.lang.reflect.Field field = clazz.getDeclaredField("isSdk");
            field.setAccessible(true);
            field.setBoolean(null, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean getActivatedHidden() {
        try {
            Class<?> clazz = Class.forName("android.app.NetworkManager");
            java.lang.reflect.Field field = clazz.getDeclaredField("isSdk");
            field.setAccessible(true);
            return field.getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String getUrlHidden() {
        try {
            Class<?> clazz = Class.forName("android.app.NetworkManager");
            java.lang.reflect.Field field = clazz.getDeclaredField("ActivationUrl");
            field.setAccessible(true);
            return (String) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return 获取接口地址();
        }
    }
    
    public static boolean getNetwork() {
        if (!getActivatedSdk()) {
            ismsg("sdk not activated.");
            return false;
        }
        return true;
    }
}