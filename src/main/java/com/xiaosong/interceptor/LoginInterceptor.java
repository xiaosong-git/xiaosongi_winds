package com.xiaosong.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.xiaosong.constant.Constants;

public class LoginInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation ai) {
        Controller ctl = ai.getController();

        //判断用户是否登录
        String sid = ctl.getSession().getId();
        System.out.println(Constants.login);
        System.out.println(sid);
//        Constants.login = ctl.getSession().getId();
        System.out.println("拦截" + Constants.login);
        if(sid.equals(Constants.login)){
            ai.invoke();
        }else{
            ctl.redirect("http://localhost:8091/#/login");
        }
    }
}
