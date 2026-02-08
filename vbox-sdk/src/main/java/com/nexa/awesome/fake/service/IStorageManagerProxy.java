package com.nexa.awesome.fake.service;

import android.os.Build;
import android.os.IInterface;
import android.os.storage.StorageVolume;

import java.io.File;
import java.lang.reflect.Method;

import black.android.os.BRServiceManager;
import black.android.os.mount.BRIMountServiceStub;
import black.android.os.storage.BRIStorageManagerStub;
import com.nexa.awesome.NexaCore;
import com.nexa.awesome.app.BActivityThread;
import com.nexa.awesome.fake.hook.BinderInvocationStub;
import com.nexa.awesome.fake.hook.MethodHook;
import com.nexa.awesome.fake.hook.ProxyMethod;
import com.nexa.awesome.utils.MethodParameterUtils;
import com.nexa.awesome.utils.Slog;
import com.nexa.awesome.utils.compat.BuildCompat;

/**
 * Created by Milk on 4/10/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class IStorageManagerProxy extends BinderInvocationStub {

	public IStorageManagerProxy() {
		super(BRServiceManager.get().getService("mount"));
	}

	@Override
	protected Object getWho() {
		IInterface mount;
		if (BuildCompat.isOreo()) {
			mount = BRIStorageManagerStub.get().asInterface(BRServiceManager.get().getService("mount"));
		} else {
			mount = BRIMountServiceStub.get().asInterface(BRServiceManager.get().getService("mount"));
		}
		return mount;
	}

	@Override
	protected void inject(Object baseInvocation, Object proxyInvocation) {
		replaceSystemService("mount");
	}

	@Override
	public boolean isBadEnv() {
		return false;
	}

	@ProxyMethod("getVolumeList")
    public static class GetVolumeList extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                int flags = Integer.parseInt(args[0] + "");
                StorageVolume[] volumeList = NexaCore.getBStorageManager().getVolumeList(BActivityThread.getBUid(), null, flags, BActivityThread.getUserId());
                if (volumeList == null) {
                    return method.invoke(who, args);
                }
                return volumeList;
            } catch (Throwable t) {
                return method.invoke(who, args);
            }
        }
    }

	@ProxyMethod("mkdirs")
    public static class mkdirs extends MethodHook {
        @Override
        protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return super.beforeHook(who, method, args);
        }

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return method.invoke(who, args);
            }
            String path;
            if (args.length == 1) {
                path = (String) args[0];
            } else {
                path = (String) args[1];
            }
            File file = new File(path);
            if (!file.exists() && !file.mkdirs()) {
                return -1;
            }
            return 0;
        }
    }
    
    @ProxyMethod("getTotalBytes")
    public static class getTotalBytes extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getCacheBytes")
    public static class getCacheBytes extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastAppPkg(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("getCacheQuotaBytes")
    public static class getCacheQuotaBytes extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args[args.length - 1] instanceof Integer) {
                args[args.length - 1] = Integer.valueOf(NexaCore.getHostUid());
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("queryStatsForUser")
    public static class queryStatsForUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastAppPkg(args);
            MethodParameterUtils.replaceLastUserId(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("queryExternalStatsForUser")
    public static class queryExternalStatsForUser extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastAppPkg(args);
            MethodParameterUtils.replaceLastUserId(args);
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("queryStatsForUid")
    public static class queryStatsForUid extends MethodHook {

        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            MethodParameterUtils.replaceLastAppPkg(args);
            MethodParameterUtils.replaceLastUserId(args);
            return method.invoke(who, args);
        }
    }
	
}
