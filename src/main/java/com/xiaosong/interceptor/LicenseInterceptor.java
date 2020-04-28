package com.xiaosong.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.handler.Handler;
import com.jfinal.kit.StrKit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

public class LicenseInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation invocation) {
        //inv.invoke();
        invocation.getController().getResponse().addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS,DELETE,PUT");
        invocation.getController().getResponse().addHeader("Access-Control-Allow-Headers", "content-type,authorization");
        invocation.getController().getResponse().addHeader("Access-Control-Allow-Origin", "*");
        invocation.getController().getResponse().addHeader("Access-Control-Request-Headers", "authorization");
        if (!"OPTIONS".equals(invocation.getController().getRequest().getMethod())) {
            System.out.println(1111);
            invocation.invoke();
        } else {
            invocation.getController().renderNull();
        }

    }
}