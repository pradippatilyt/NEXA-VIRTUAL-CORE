package com.nexa.awesome.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HookApplication {
    static native void a(Context context);
    static native String b();
    static native String d();

    static {
        System.loadLibrary("Blackbox");
    }

    public static void Start(Context context) {
        a(context);
        c(context);
    }

    static void c(Context context) {
        try {
            String data = d();

            DataInputStream is = new DataInputStream(new ByteArrayInputStream(Base64.decode(data, 0)));
            final byte[][] originalSigns = new byte[(is.read() & 255)][];
            for (int i = 0; i < originalSigns.length; i++) {
                originalSigns[i] = new byte[is.readInt()];
                is.readFully(originalSigns[i]);
            }

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            final Object sPackageManager = sPackageManagerField.get(currentActivityThread);

            Class<?> iPackageManagerClass = Class.forName("android.content.pm.IPackageManager");
            final String packageName = context.getPackageName();

            Object proxy = Proxy.newProxyInstance(iPackageManagerClass.getClassLoader(), new Class<?>[]{iPackageManagerClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("getPackageInfo")) {
                        if (args[0] instanceof String && args[0].toString() == packageName) {
                            PackageInfo info = (PackageInfo) method.invoke(sPackageManager, args);
                            if (info != null) {
                                if (info.signatures != null) {
                                    info.signatures = new Signature[originalSigns.length];
                                    for (int i = 0; i < info.signatures.length; i++) {
                                        info.signatures[i] = new Signature(originalSigns[i]);
                                    }
                                }

                                if (info.applicationInfo != null) {
                                    info.applicationInfo.sourceDir = b();
                                    info.applicationInfo.publicSourceDir = b();
                                }
                                return info;
                            }
                        }
                    }
                    if (method.getName().equals("getApplicationInfo")) {
                        if (args[0] instanceof String && args[0].toString() == packageName) {
                            ApplicationInfo info = (ApplicationInfo) method.invoke(sPackageManager, args);
                            if (info != null) {
                                info.sourceDir = b();
                                info.publicSourceDir = b();
                            }
                            return info;
                        }
                    }
                    if (method.getName().equals("getPackageArchiveInfo")) {
                        if (args[0] instanceof String && (args[0].toString().contains(".apk") && args[0].toString() == (packageName))) {
                            args[0] = (Object) b();
                        }
                    }
                    return method.invoke(sPackageManager, args);
                }
            });

            sPackageManagerField.set(currentActivityThread, proxy);

            PackageManager pm = context.getPackageManager();
            Field mPmField = pm.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(pm, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
// This Src Real Owner is -- @RrQ_Owner