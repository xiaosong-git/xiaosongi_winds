package com.xiaosong.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.xiaosong.constant.Constant;
import com.xiaosong.constant.Constants;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.jetty.security.LoginService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录session过滤器
 *
 * @author Administrator
 */
public class XiaoSongInterceptor implements Interceptor {

    static {
        //自动快速地使用缺省Log4j环境。
        BasicConfigurator.configure();
    }

    public void intercept(Invocation inv) {

        //inv.invoke();
        inv.getController().getResponse().addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS,DELETE,PUT");
        inv.getController().getResponse().addHeader("Access-Control-Allow-Headers", "content-type,authorization");
        inv.getController().getResponse().addHeader("Access-Control-Allow-Origin", "*");
        inv.getController().getResponse().addHeader("Access-Control-Request-Headers", "authorization");
        if (!"OPTIONS".equals(inv.getController().getRequest().getMethod())) {
            inv.invoke();
        } else {
            inv.getController().renderNull();
        }


//        Controller ctl = inv.getController();
//
//        //判断用户是否登录
//        Constants.login = ctl.getSession().getId();
//        System.out.println("拦截---" + Constants.login);
//        List user = CacheKit.get("LoginUserCache", Constants.login);
//
//        String action = inv.getActionKey();
//        System.out.println(action);
//        if (user != null || action.indexOf("/login") >= 0) {
//            inv.invoke();
//        } else {
////            ctl.renderJson(RetUtil.fail());
//            ctl.redirect("/login");
//        }
        //Controller con = inv.getController();

        //String s = inv.getActionKey();
        //if(s.contains("/login")) {
        //    String admin = con.getPara("username");
        //    List<String> list=CacheKit.get(Constant.SYS_ACCOUNT, Constants.login);
        //    if(list!=null) {
        //        if(list.get(0).equals(admin)) {
        //            inv.invoke();
        //        }else {
        //            con.redirect("192.168.4.135:8091/#/login");
        //            return;
        //        }
        //    }else {
        //        con.redirect("192.168.4.135:8091/#/login");
        //        return ;
        //    }
        //}else {
        //    inv.invoke();
        //}


        //inv.invoke();
//        inv.getController().getResponse().addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS,DELETE,PUT");
//        inv.getController().getResponse().addHeader("Access-Control-Allow-Headers", "content-type,authorization");
//        inv.getController().getResponse().addHeader("Access-Control-Allow-Origin", "*");
//        inv.getController().getResponse().addHeader("Access-Control-Request-Headers", "authorization");
//        if (!"OPTIONS".equals(inv.getController().getRequest().getMethod())) {
//            inv.invoke();
//        } else {
//            inv.getController().renderNull();
//        }
    }

    public void intercept1(Invocation ai) {

        Controller ctl = ai.getController();

        String loginPath = "/login.html";
        //判断用户是否登录
        Object obj = ctl.getSession().getAttribute("/login");
        if (obj == null) {
            System.out.println("用户未登录: " + loginPath);
            //ctl.redirect(loginPath);
            ctl.render(loginPath);//此处要如何跳转到首页？

        } else {
            System.out.println("用户已登录");

            try {

                ai.invoke();

                System.out.println("After action invoking*****************************************");
            } catch (Exception e) {

                Map<String, Object> error = new HashMap<String, Object>();
                String exceInfo = e.getMessage();
                error.put("info", exceInfo);

                String jsonStr = JsonKit.toJson(error);
                ai.getController().renderText(jsonStr);
            } finally {

            }
        }
    }
}
