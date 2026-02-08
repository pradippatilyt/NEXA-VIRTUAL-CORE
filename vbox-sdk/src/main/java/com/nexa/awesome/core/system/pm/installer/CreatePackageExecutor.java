package com.nexa.awesome.core.system.pm.installer;

import com.nexa.awesome.core.env.BEnvironment;
import com.nexa.awesome.core.system.pm.BPackageSettings;
import com.nexa.awesome.entity.pm.InstallOption;
import com.nexa.awesome.utils.FileUtils;


public class CreatePackageExecutor implements Executor {
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        FileUtils.mkdirs(BEnvironment.getAppDir(ps.pkg.packageName));
        FileUtils.mkdirs(BEnvironment.getAppLibDir(ps.pkg.packageName));
        return 0;
    }
}
