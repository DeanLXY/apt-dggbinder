package com.wangx.dggapi;

import android.app.Activity;
import android.view.View;

import static android.os.Build.VERSION_CODES.M;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/12
 * @Project: DggBind
 */

public class DggBind {

    public static void bind(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        bind(activity, decorView);
    }

    private static void bind(Object object, View root) {
        try {
            Class<?> aClass = Class.forName(object.getClass().getCanonicalName() + "$$Proxy");
            IProxy proxy = (IProxy) aClass.newInstance();
            proxy.inject(object, root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
