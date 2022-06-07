package com.hauyu.adsdk.demo;

import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyUtils {
    public static void hookClick(View view) {
        try {
            Method method = View.class.getDeclaredMethod("getListenerInfo");
            method.setAccessible(true);
            Object mListenerInfo = method.invoke(view);
            Class<?> classListenerInfo = Class.forName("android.view.View$ListenerInfo");
            // 获取内部Field mOnClickListener
            Field field = classListenerInfo.getDeclaredField("mOnClickListener");
            // 然后获取Button的ListenerInfo对象mListenerInfo的mOnClickListener变量
            // --这就是真正的拿到了Button的监听回调View.OnClickListener的实例对象
            final View.OnClickListener onClickListener = (View.OnClickListener) field.get(mListenerInfo);
            Object proxyOnClickListener = Proxy.newProxyInstance(View.class.getClassLoader(),
                    new Class[]{View.OnClickListener.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Toast.makeText(view.getContext(),
                                    "你点击我嘛，我很烦的！" + view,
                                    Toast.LENGTH_LONG).show();
                            return method.invoke(onClickListener, args);
                        }
                    });

            // 2. 然后替换掉Button的点击事件
            field.set(mListenerInfo, proxyOnClickListener);
        } catch (Exception e) {
        }
    }
}
