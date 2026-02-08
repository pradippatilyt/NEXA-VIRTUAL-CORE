// IRequestPermissionsResult.aidl
package com.nexa.awesome.core.system.am;

interface IRequestPermissionsResult {
    boolean onResult(int requestCode,in String[] permissions,in int[] grantResults);
}