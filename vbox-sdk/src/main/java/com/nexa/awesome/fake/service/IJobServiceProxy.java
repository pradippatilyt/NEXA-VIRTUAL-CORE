package com.nexa.awesome.fake.service;

import android.app.job.JobInfo;
import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import black.android.app.job.BRIJobSchedulerStub;
import black.android.os.BRServiceManager;
import com.nexa.awesome.NexaCore;
import com.nexa.awesome.app.BActivityThread;
import com.nexa.awesome.fake.hook.BinderInvocationStub;
import com.nexa.awesome.fake.hook.MethodHook;
import com.nexa.awesome.fake.hook.ProxyMethod;
import com.nexa.awesome.utils.Slog;

public class IJobServiceProxy extends BinderInvocationStub {
    public static final String TAG = "JobServiceStub";

    public IJobServiceProxy() {
        super(BRServiceManager.get().getService(Context.JOB_SCHEDULER_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder jobScheduler = BRServiceManager.get().getService("jobscheduler");
        return BRIJobSchedulerStub.get().asInterface(jobScheduler);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    // ---- FIXED SCHEDULE ----
    @ProxyMethod("schedule")
    public static class Schedule extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (args == null || args.length == 0 || !(args[0] instanceof JobInfo)) {
                    Slog.w(TAG, "schedule called with invalid JobInfo, returning 0");
                    return 0; // safe fallback
                }
                JobInfo jobInfo = (JobInfo) args[0];
                if (jobInfo == null) {
                    Slog.w(TAG, "schedule called with null JobInfo, returning 0");
                    return 0;
                }
                JobInfo proxyJobInfo = NexaCore.getBJobManager().schedule(jobInfo);
                args[0] = proxyJobInfo != null ? proxyJobInfo : jobInfo;
                return method.invoke(who, args);
            } catch (Throwable t) {
                Slog.w(TAG, "schedule failed, returning 0", t);
                return 0;
            }
        }
    }

    @ProxyMethod("cancel")
    public static class Cancel extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0 && args[0] instanceof Integer) {
                args[0] = NexaCore.getBJobManager().cancel(BActivityThread.getAppConfig().processName, (Integer) args[0]);
                return method.invoke(who, args);
            }
            return null;
        }
    }

    @ProxyMethod("cancelAll")
    public static class CancelAll extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            NexaCore.getBJobManager().cancelAll(BActivityThread.getAppConfig().processName);
            return method.invoke(who, args);
        }
    }

    // ---- FIXED ENQUEUE ----
    @ProxyMethod("enqueue")
    public static class Enqueue extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (args == null || args.length == 0 || !(args[0] instanceof JobInfo)) {
                    Slog.w(TAG, "enqueue called with invalid JobInfo, returning 0");
                    return 0;
                }
                JobInfo jobInfo = (JobInfo) args[0];
                if (jobInfo == null) {
                    Slog.w(TAG, "enqueue called with null JobInfo, returning 0");
                    return 0;
                }
                JobInfo proxyJobInfo = NexaCore.getBJobManager().schedule(jobInfo);
                args[0] = proxyJobInfo != null ? proxyJobInfo : jobInfo;
                return method.invoke(who, args);
            } catch (Throwable t) {
                Slog.w(TAG, "enqueue failed, returning 0", t);
                return 0;
            }
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
