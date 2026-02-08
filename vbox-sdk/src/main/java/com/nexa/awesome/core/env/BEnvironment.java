package com.nexa.awesome.core.env;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.Locale;

import com.nexa.awesome.NexaCore;
import com.nexa.awesome.utils.FileUtils;
import com.nexa.awesome.app.BActivityThread;
//import com.nexa.awesome.core.system.api.MetaActivationManager;
import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class BEnvironment {
    
    private static final File InternalDirectory = NexaCore.getContext().getFilesDir();
    private static final File ExternalDirectory = Environment.getExternalStorageDirectory();
    
	public static void load() {
		FileUtils.mkdirs(InternalDirectory);
        FileUtils.mkdirs(ExternalDirectory);
		FileUtils.mkdirs(getSystemDir());
		FileUtils.mkdirs(getCacheDir());
		FileUtils.mkdirs(getProcDir());
	}

    public static File getSystemDir() {
        return new File(InternalDirectory, "system");
    }

    public static File getProcDir() {
        return new File(InternalDirectory, "proc");
    }

    public static File getCacheDir() {
        return new File(InternalDirectory, "cache");
    }

    public static File getUserInfoConf() {
        return new File(getSystemDir(), "user.conf");
    }

    public static File getAccountsConf() {
        return new File(getSystemDir(), "accounts.conf");
    }

    public static File getUidConf() {
        return new File(getSystemDir(), "uid.conf");
    }

    public static File getSharedUserConf() {
        return new File(getSystemDir(), "shared-user.conf");
    }

    public static File getXPModuleConf() {
        return new File(getSystemDir(), "xposed-module.conf");
    }

    public static File getFakeLocationConf() {
        return new File(getSystemDir(), "fake-location.conf");
    }
    
    public static File getFakeDeviceConf() {
        return new File(getSystemDir(), "fake-device.conf");
    }

    public static File getPackageConf(String packageName) {
        return new File(getAppDir(packageName), "package.conf");
    }
    
    public static File getExternalStorageDirectory() {
		if (Build.VERSION.SDK_INT == 29)
			return new File(ExternalDirectory, "SdCard");
		return new File(ExternalDirectory, "SdCard");
	}

    public static File getUserDir(int userId) {
        return new File(InternalDirectory, String.format(Locale.CHINA, "data/user/%d", userId));
    }

    public static File getDeDataDir(String packageName, int userId) {
        return new File(InternalDirectory, String.format(Locale.CHINA, "data/user_de/%d/%s", userId, packageName));
    }

    public static File getExternalDataDir(String packageName) {
        return new File(getExternalStorageDirectory(), String.format(Locale.CHINA, "Android/data/%s", packageName));
    }

    public static File getExternalObbDir(String packageName) {
        return new File(getExternalStorageDirectory(), String.format(Locale.CHINA, "Android/obb/%s/", packageName));
    }

    public static File getDataDir(String packageName, int userId) {
        return new File(InternalDirectory, String.format(Locale.CHINA, "data/user/%d/%s", userId, packageName));
    }

    public static File getProcDir(int pid) {
        File file = new File(getProcDir(), String.format(Locale.CHINA, "%d", pid));
        FileUtils.mkdirs(file);
        return file;
    }

    public static File getExternalDataFilesDir(String packageName) {
        return new File(getExternalDataDir(packageName), "files");
    }

    public static File getDataFilesDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "files");
    }

    public static File getExternalDataCacheDir(String packageName) {
        return new File(getExternalDataDir(packageName), "cache");
    }

    public static File getDataCacheDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "cache");
    }

    public static File getDataLibDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "lib");
    }

    public static File getDataDatabasesDir(String packageName, int userId) {
        return new File(getDataDir(packageName, userId), "databases");
    }

    public static File getAppRootDir() {
        return getAppDir("");
    }

    public static File getAppDir(String packageName) {
        return new File(InternalDirectory, "data/app/" + packageName);
    }

    public static File getBaseApkDir(String packageName) {
        return new File(InternalDirectory, "data/app/" + packageName + "/base.apk");
    }

    public static File getAppLibDir(String packageName) {
        return new File(getAppDir(packageName), "lib");
    }
    
    
}
