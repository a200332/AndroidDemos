package com.whyalwaysmea.plugdemo.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.whyalwaysmea.plugdemo.PluginCons;
import com.whyalwaysmea.plugdemo.PluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by HanLong on 2017/7/27.
 */

public class HookInstrumentation extends Instrumentation {

    private static final String TAG = "HookInstrumentation";

    private Instrumentation mBase;
    private Context mContext;

    public HookInstrumentation(Instrumentation instrumentation) {
        this.mBase = instrumentation;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Log.d(TAG, "newActivity: className == " + className);
        String clazzName = intent.getStringExtra(PluginCons.FLAG_ACTIVITY_CLASS_NAME);
        if(!TextUtils.isEmpty(clazzName)) {
            return super.newActivity(PluginManager.getInstance().getClassLoader(), clazzName, intent);
        } else {
            return super.newActivity(PluginManager.getInstance().getClassLoader(), className, intent);
        }
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if(mContext != null) {
            Field field = null;
            try {
                field =ContextWrapper.class.getDeclaredField("mBase");
                field.setAccessible(true);
                field.set(activity, mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.callActivityOnCreate(activity, icicle);
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {

        // Hook之前, XXX到此一游!
        Log.d(TAG, "\n执行了startActivity, 参数如下: \n" + "who = [" + who + "], " +
                "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                "\ntarget = [" + target + "], \nintent = [" + intent +
                "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");

        // 开始调用原始的方法, 调不调用随你,但是不调用的话, 所有的startActivity都失效了.
        // 由于这个方法是隐藏的,因此需要使用反射调用;首先找到这个方法
        try {
            Method execStartActivity = Instrumentation.class.getDeclaredMethod(
                    "execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(mBase, who,
                    contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            // 某该死的rom修改了  需要手动适配
            e.printStackTrace();
            throw new RuntimeException("do not support!!!" + e.getMessage());
        }
    }

    public void setContext(Context context) {
        mContext = context;
    }
}
