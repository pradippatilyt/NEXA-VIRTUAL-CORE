package com.nexa.awesome.fake.service;

import android.content.pm.ProviderInfo;
import android.util.Log;
import black.android.content.BRIClipboardStub;
import black.android.os.BRServiceManager;
import java.lang.reflect.Method;
import com.nexa.awesome.NexaCore;
import com.nexa.awesome.app.BActivityThread;
import com.nexa.awesome.fake.hook.BinderInvocationStub;
import com.nexa.awesome.fake.hook.MethodHook;
import com.nexa.awesome.fake.hook.ProxyMethod;
import com.nexa.awesome.utils.Slog;

public class IClipboardManagerProxy extends BinderInvocationStub {
    private static final String TAG = "IClipboardManagerProxy";

    public IClipboardManagerProxy() {
        super(BRServiceManager.get().getService("clipboard"));
    }

    public Object getWho() {
        return BRIClipboardStub.get().asInterface(BRServiceManager.get().getService("clipboard"));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int argIndex = getPackNameIndex(args);
        if (argIndex != -1) {
            args[argIndex] = NexaCore.getHostPkg();
        }
        return super.invoke(proxy, method, args);
    }

    private int getPackNameIndex(Object[] args) {
        if (args == null) {
            return -1;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                Log.d(TAG, "args[" + i + "] " + args[i]);
                return i;
            }
        }
        return -1;
    }

    public void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("clipboard");
    }

    public boolean isBadEnv() {
        return false;
    }
    
    @ProxyMethod("setActivityLocusContext")
	public static class SetActivityLocusContext extends MethodHook {
		@Override
		protected Object hook(Object who, Method method, Object[] args) throws Throwable {
			try {
				if (args != null && args.length >= 2) {
					String targetPackage = (String) args[1];
					String currentPackage = BActivityThread.getAppPackageName();
					if (targetPackage != null && !targetPackage.equals(currentPackage)) {
						Slog.w(TAG, "SetActivityLocusContext: Ignoring locus context for " + targetPackage + " (current: " + currentPackage + ")");
					}
				}
				return null;
			} catch (Throwable e) {
				Slog.w(TAG, "SetActivityLocusContext: swallowed exception", e);
				return null;
			}
		}
	}

    @ProxyMethod("getProviderInfo")
    public static class GetProviderInfoFix extends MethodHook {
        private static final String STARTUP_PROVIDER = "androidx.startup.InitializationProvider";

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                // Normal call first
                return method.invoke(who, args);
            } catch (Throwable e) {
                // Only handle InitializationProvider crash
                if (args != null && args.length > 0) {
                    String name = String.valueOf(args[0]);
                    if (name.contains(STARTUP_PROVIDER)) {
                        ProviderInfo info = new android.content.pm.ProviderInfo();
                        String pkg = BActivityThread.getAppPackageName();
                        info.name = STARTUP_PROVIDER;
                        info.packageName = pkg;
                        info.authority = pkg + ".androidx-startup";
                        info.enabled = true;
                        info.exported = false;
                        info.processName = pkg;
                        try {
                            info.applicationInfo = BActivityThread.getApplication().getApplicationInfo();
                        } catch (Throwable ignored) {}
                        return info; // ✅ prevent crash
                    }
                }
                // fallback (don’t crash)
                return null;
            }
        }
    }


}
