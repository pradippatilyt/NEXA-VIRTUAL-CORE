// IBXposedManagerService.aidl

package com.nexa.awesome.core.system.pm;

import java.util.List;
import com.nexa.awesome.entity.pm.InstalledModule;

interface IBXposedManagerService {
    boolean isXPEnable();
    void setXPEnable(boolean enable);
    boolean isModuleEnable(String packageName);
    void setModuleEnable(String packageName, boolean enable);
    List<InstalledModule> getInstalledModules();
}