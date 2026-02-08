package com.nexa.awesome.core.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nexa.awesome.NexaCore;
import com.nexa.awesome.core.env.AppSystemEnv;
import com.nexa.awesome.core.env.BEnvironment;
import com.nexa.awesome.core.system.accounts.BAccountManagerService;
import com.nexa.awesome.core.system.am.BActivityManagerService;
import com.nexa.awesome.core.system.am.BJobManagerService;
import com.nexa.awesome.core.system.location.BLocationManagerService;
import com.nexa.awesome.core.system.notification.BNotificationManagerService;
import com.nexa.awesome.core.system.os.BStorageManagerService;
import com.nexa.awesome.core.system.pm.BPackageInstallerService;
import com.nexa.awesome.core.system.pm.BPackageManagerService;
import com.nexa.awesome.core.system.pm.BXposedManagerService;
import com.nexa.awesome.core.system.user.BUserHandle;
import com.nexa.awesome.core.system.user.BUserManagerService;
import com.nexa.awesome.entity.pm.InstallOption;
import com.nexa.awesome.utils.FileUtils;


public class BlackBoxSystem {
    private static BlackBoxSystem sBlackBoxSystem;
    private final List<ISystemService> mServices = new ArrayList<>();
    private final static AtomicBoolean isStartup = new AtomicBoolean(false);

    private static final class BlackBoxSystemHolder {
        static final BlackBoxSystem blackboxsystem = new BlackBoxSystem();

        private BlackBoxSystemHolder() {
        }
    }

    public static BlackBoxSystem getSystem() {
        return BlackBoxSystemHolder.blackboxsystem;
    }

    public void startup() {
        if (!isStartup.getAndSet(true)) {
			BEnvironment.load();
			mServices.add(BPackageManagerService.get());
			mServices.add(BUserManagerService.get());
			mServices.add(BActivityManagerService.get());
			mServices.add(BJobManagerService.get());
			mServices.add(BStorageManagerService.get());
			mServices.add(BPackageInstallerService.get());
			mServices.add(BXposedManagerService.get());
			mServices.add(BProcessManagerService.get());
			mServices.add(BAccountManagerService.get());
			mServices.add(BLocationManagerService.get());
			mServices.add(BNotificationManagerService.get());

			for (ISystemService systemReady : mServices) {
				systemReady.systemReady();
			}

			List<String> preInstallPackages = AppSystemEnv.getPreInstallPackages();
			for (String preInstallPackage : preInstallPackages) {
				try {
					if (!BPackageManagerService.get().isInstalled(preInstallPackage, BUserHandle.USER_ALL)) {
						PackageInfo packageInfo = NexaCore.getPackageManager().getPackageInfo(preInstallPackage, 0);
						BPackageManagerService.get().installPackageAsUser(packageInfo.applicationInfo.sourceDir, InstallOption.installBySystem(), BUserHandle.USER_ALL);
					}
				} catch (PackageManager.NameNotFoundException ignored) {
				}
			}
		}
    }

}
