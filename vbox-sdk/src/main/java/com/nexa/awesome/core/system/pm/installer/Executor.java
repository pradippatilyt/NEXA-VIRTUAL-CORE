package com.nexa.awesome.core.system.pm.installer;

import com.nexa.awesome.core.system.pm.BPackageSettings;
import com.nexa.awesome.entity.pm.InstallOption;

public interface Executor {
    public static final String TAG = "InstallExecutor";

    int exec(BPackageSettings bPackageSettings, InstallOption installOption, int i);
}
