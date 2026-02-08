package com.nexa.awesome.fake.hook;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.nexa.awesome.NexaCore;
import com.nexa.awesome.fake.delegate.AppInstrumentation;

import com.nexa.awesome.fake.service.IBluetoothManagerProxy;
import com.nexa.awesome.fake.service.ISmsProxy;
import com.nexa.awesome.fake.service.ISubProxy;
import com.nexa.awesome.fake.service.HCallbackStub;
import com.nexa.awesome.fake.service.IAccessibilityManagerProxy;
import com.nexa.awesome.fake.service.IAccountManagerProxy;
import com.nexa.awesome.fake.service.IActivityClientProxy;
import com.nexa.awesome.fake.service.IActivityManagerProxy;
import com.nexa.awesome.fake.service.IActivityTaskManagerProxy;
import com.nexa.awesome.fake.service.IAlarmManagerProxy;
import com.nexa.awesome.fake.service.IAppIntegrityManagerProxy;
import com.nexa.awesome.fake.service.IAppOpsManagerProxy;
import com.nexa.awesome.fake.service.IAppWidgetManagerProxy;
import com.nexa.awesome.fake.service.IAudioManagerProxy;
import com.nexa.awesome.fake.service.IAutofillManagerProxy;
import com.nexa.awesome.fake.service.IBackupManagerProxy;
import com.nexa.awesome.fake.service.IClipboardManagerProxy;
import com.nexa.awesome.fake.service.IConnectivityManagerProxy;
import com.nexa.awesome.fake.service.IContextHubServiceProxy;
import com.nexa.awesome.fake.service.IDeviceIdentifiersPolicyProxy;
import com.nexa.awesome.fake.service.IDevicePolicyManagerProxy;
import com.nexa.awesome.fake.service.IDisplayManagerProxy;
import com.nexa.awesome.fake.service.IFingerprintManagerProxy;
import com.nexa.awesome.fake.service.IGraphicsStatsProxy;
import com.nexa.awesome.fake.service.IJobServiceProxy;
import com.nexa.awesome.fake.service.ILauncherAppsProxy;
import com.nexa.awesome.fake.service.ILocaleManagerProxy;
import com.nexa.awesome.fake.service.ILocationManagerProxy;
import com.nexa.awesome.fake.service.IMediaRouterServiceProxy;
import com.nexa.awesome.fake.service.IMediaSessionManagerProxy;
import com.nexa.awesome.fake.service.INetworkManagementServiceProxy;
import com.nexa.awesome.fake.service.INotificationManagerProxy;
import com.nexa.awesome.fake.service.IPackageManagerProxy;
import com.nexa.awesome.fake.service.IPermissionManagerProxy;
import com.nexa.awesome.fake.service.IPersistentDataBlockServiceProxy;
import com.nexa.awesome.fake.service.IPhoneSubInfoProxy;
import com.nexa.awesome.fake.service.IPowerManagerProxy;
import com.nexa.awesome.fake.service.IShortcutManagerProxy;
import com.nexa.awesome.fake.service.IStorageManagerProxy;
import com.nexa.awesome.fake.service.IStorageStatsManagerProxy;
import com.nexa.awesome.fake.service.ISystemUpdateProxy;
import com.nexa.awesome.fake.service.ITelephonyManagerProxy;
import com.nexa.awesome.fake.service.ITelephonyRegistryProxy;
import com.nexa.awesome.fake.service.IUserManagerProxy;
import com.nexa.awesome.fake.service.IVibratorServiceProxy;
import com.nexa.awesome.fake.service.IVpnManagerProxy;
import com.nexa.awesome.fake.service.IWifiManagerProxy;
import com.nexa.awesome.fake.service.IWifiScannerProxy;
import com.nexa.awesome.fake.service.IWindowManagerProxy;
import com.nexa.awesome.fake.service.context.ContentServiceStub;
import com.nexa.awesome.fake.service.context.RestrictionsManagerStub;
import com.nexa.awesome.fake.service.libcore.OsStub;
import com.nexa.awesome.fake.service.vivo.IVivoPermissionServiceProxy;
import com.nexa.awesome.utils.Slog;
import com.nexa.awesome.utils.compat.BuildCompat;

/**
 * Created by Milk on 3/30/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class HookManager {
    public static final String TAG = "HookManager";

    private static final HookManager sHookManager = new HookManager();

    private final Map<Class<?>, IInjectHook> mInjectors = new HashMap<>();

    public static HookManager get() {
        return sHookManager;
    }

    public void init() {
        if (NexaCore.get().isBlackProcess() || NexaCore.get().isServerProcess()) {
            addInjector(new OsStub());
            addInjector(new IDisplayManagerProxy());
            //addInjector(new IDropBoxManagerProxy());
            //addInjector(new IInputMethodManagerProxy());
            addInjector(new IJobServiceProxy());
            addInjector(new IActivityManagerProxy());
            addInjector(new IPackageManagerProxy());
            addInjector(new ITelephonyManagerProxy());
            addInjector(new HCallbackStub());
            addInjector(new IWifiManagerProxy());
            addInjector(new IWifiScannerProxy());
            addInjector(new ISubProxy());
            addInjector(new IAppOpsManagerProxy());
            addInjector(new INotificationManagerProxy());
            addInjector(new IAlarmManagerProxy());
            addInjector(new IAppWidgetManagerProxy());
            addInjector(new IAudioManagerProxy());
            addInjector(new IBackupManagerProxy());
            addInjector(new IBluetoothManagerProxy());
            addInjector(new ContentServiceStub());
            addInjector(new IWindowManagerProxy());
            addInjector(new IUserManagerProxy());
          //  addInjector(new RestrictionsManagerStub());
            addInjector(new IMediaSessionManagerProxy());
            addInjector(new ILocationManagerProxy());
            addInjector(new ISmsProxy());
            addInjector(new IStorageManagerProxy());
            addInjector(new ILauncherAppsProxy());
            addInjector(new IAccessibilityManagerProxy());
            addInjector(new ITelephonyRegistryProxy());
            addInjector(new IDevicePolicyManagerProxy());
            addInjector(new IAccountManagerProxy());
            addInjector(new IConnectivityManagerProxy());
            addInjector(new IClipboardManagerProxy());
            addInjector(new IPhoneSubInfoProxy());
            addInjector(new IMediaRouterServiceProxy());
            addInjector(new INetworkManagementServiceProxy());
            addInjector(new IPowerManagerProxy());
           // addInjector(new ICrossProfileAppsProxy());
            addInjector(new IVibratorServiceProxy());
            addInjector(AppInstrumentation.get());
            
            if (BuildCompat.isVivo()) {
                addInjector(new IVivoPermissionServiceProxy());
            }
            if (BuildCompat.isBaklava()) {
                addInjector(new IPersistentDataBlockServiceProxy());
            }
            if (BuildCompat.isUpsideDownCake()) {
                addInjector(new IAppIntegrityManagerProxy());
                addInjector(new ILocaleManagerProxy());
            }
            
            if (BuildCompat.isS()) {
                addInjector(new IActivityClientProxy((Object) null));
                addInjector(new IVpnManagerProxy());
            }
            if (BuildCompat.isR()) {
                addInjector(new IActivityTaskManagerProxy());
                addInjector(new IPermissionManagerProxy());
            }
            if (BuildCompat.isQ()) {
                addInjector(new IDeviceIdentifiersPolicyProxy());
            }
            if (BuildCompat.isPie()) {
                addInjector(new ISystemUpdateProxy());
            }
            
            if (BuildCompat.isOreo_MR1()) {
                addInjector(new IAutofillManagerProxy());
                addInjector(new IContextHubServiceProxy());
                addInjector(new IStorageStatsManagerProxy());
                addInjector(new ISystemUpdateProxy());
            }
            
            if (BuildCompat.isOreo()) {
                addInjector(new IShortcutManagerProxy());
            }
            
            if (BuildCompat.isN()) {
                addInjector(new IFingerprintManagerProxy());
                addInjector(new IGraphicsStatsProxy());
            }
        }
        injectAll();
    }

    public void checkEnv(Class<?> clazz) {
        IInjectHook iInjectHook = mInjectors.get(clazz);
        if (iInjectHook != null && iInjectHook.isBadEnv()) {
            Log.d(TAG, "checkEnv: " + clazz.getSimpleName() + " is bad env");
            iInjectHook.injectHook();
        }
    }

    public void checkAll() {
        for (Class<?> aClass : mInjectors.keySet()) {
            IInjectHook iInjectHook = mInjectors.get(aClass);
            if (iInjectHook != null && iInjectHook.isBadEnv()) {
                Log.d(TAG, "checkEnv: " + aClass.getSimpleName() + " is bad env");
                iInjectHook.injectHook();
            }
        }
    }

    void addInjector(IInjectHook injectHook) {
        mInjectors.put(injectHook.getClass(), injectHook);

    }

    void injectAll() {
        for (IInjectHook value : mInjectors.values()) {
            try {
                Slog.d(TAG, "hook: " + value);
                value.injectHook();
            } catch (Exception e) {
                Slog.d(TAG, "hook error: " + value);
            }
        }
    }
}
