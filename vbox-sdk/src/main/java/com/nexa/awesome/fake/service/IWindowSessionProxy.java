package com.nexa.awesome.fake.service;

import android.os.IInterface;
import android.view.WindowManager;

import java.lang.reflect.Method;

import com.nexa.awesome.NexaCore;
import com.nexa.awesome.fake.hook.BinderInvocationStub;
import com.nexa.awesome.fake.hook.MethodHook;
import com.nexa.awesome.fake.hook.ProxyMethod;


/**
 * Created by Milk on 4/6/21.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class IWindowSessionProxy extends BinderInvocationStub {
    public static final String TAG = "WindowSessionStub";

    private IInterface mSession;


    public IWindowSessionProxy(IInterface session) {
        super(session.asBinder());
        mSession = session;
    }

    @Override
    protected Object getWho() {
        return mSession;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {

    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    public Object getProxyInvocation() {
        return super.getProxyInvocation();
    }

    @ProxyMethod("addToDisplay")
    public static class AddToDisplay extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                if (arg instanceof WindowManager.LayoutParams) {
                    ((WindowManager.LayoutParams) arg).packageName = NexaCore.getHostPkg();
                }
            }
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("addToDisplayAsUser")
    public static class AddToDisplayAsUser extends AddToDisplay {
    }

    @ProxyMethod("grantInputChannel")
    public static class grantInputChannel extends AddToDisplay {
    }

    @ProxyMethod("relayout")
    public static class relayout extends AddToDisplay {
    }

    @ProxyMethod("addWithoutInputChannel")
    public static class addWithoutInputChannel extends AddToDisplay {
    }

    @ProxyMethod("addToDisplayWithoutInputChannel")
    public static class addToDisplayWithoutInputChannel extends AddToDisplay {
    }
}
