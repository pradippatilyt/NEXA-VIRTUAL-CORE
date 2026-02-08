package com.nexa.awesome.core;

import android.os.Binder;
import android.os.Build;
import android.os.Process;
/*
import com.android.apksig.ApkVerifier;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.function.Consumer;*/
import android.util.Log;
import androidx.annotation.Keep;
import android.content.Context;
import java.io.File;
import java.util.List;
import dalvik.system.DexFile;
import com.nexa.awesome.NexaCore;
import com.nexa.awesome.app.BActivityThread;
import com.nexa.awesome.utils.compat.DexFileCompat;

public class NativeCore {
    
    public static final String TAG = "NativeCore";
    private static boolean isInjected = false;
    public static String libtarget = "libbgmi.so";

    static {
        System.loadLibrary("blackbox");

		if (!isInjected) {
			Context context = NexaCore.getContext();
			File libFile = new File(context.getFilesDir(), "loader/" + libtarget);
			if (libFile.exists()) {
				Log.i(TAG, "Loading " + libtarget + " from loader directory");
				try {
					System.load(libFile.getAbsolutePath());
					isInjected = true;
					Log.i(TAG, libtarget + " successfully loaded from: " + libFile.getAbsolutePath());
				} catch (UnsatisfiedLinkError e) {
					Log.e(TAG, "Failed to load " + libtarget + ": " + e.getMessage());
				}
			} else {
				Log.e(TAG, libtarget + " not found at: " + libFile.getAbsolutePath());
			}
		} else {
			Log.i(TAG, "Library already injected");
		}
    }

    public static native void init(int apiLevel);
    public static native void enableIO();
    public static native void addIORule(String targetPath, String relocatePath);
    public static native void hideXposed();
    
    @Keep
    public static int getCallingUid(int origCallingUid) {
        // 系统uid
        if (origCallingUid > 0 && origCallingUid < Process.FIRST_APPLICATION_UID)
            return origCallingUid;
        // 非用户应用
        if (origCallingUid > Process.LAST_APPLICATION_UID)
            return origCallingUid;

        if (origCallingUid == NexaCore.getHostUid()) {
            return BActivityThread.getCallingBUid();
        }
        return origCallingUid;
    }

    @Keep
    public static String redirectPath(String path) {
        return IOCore.get().redirectPath(path);
    }

    @Keep
    public static File redirectPath(File path) {
        return IOCore.get().redirectPath(path);
    }
    
    /*
    public void runApk(Context ctx) {
        try {
            File apkFile = new File(ctx.getApplicationInfo().sourceDir);
            ApkVerifier verifier = new ApkVerifier.Builder(apkFile).build();
            ApkVerifier.Result result = verifier.verify();
            if (!result.isVerified()) {
                throw new SecurityException("APK verification failed");
            }
            if (Build.VERSION.SDK_INT >= 24) {
                result.getSignerCertificates().forEach(new Consumer<X509Certificate>() {
                    @Override
                    public void accept(X509Certificate cert) {
                        verifyCertificateSignature(cert);
                    }
                });
            }
        } catch (Exception e) {
            throw new SecurityException("APK verification error", e);
        }
    }

    private void verifyCertificateSignature(X509Certificate cert) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String hash = bytesToHex(md.digest(cert.getEncoded()));
            if (!SHA256.equalsIgnoreCase(hash)) {
                throw new SecurityException("Certificate signature mismatch");
            }
        } catch (Exception e) {
            throw new RuntimeException("Certificate verification error", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }*/

}
