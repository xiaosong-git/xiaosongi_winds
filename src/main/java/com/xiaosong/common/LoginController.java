package com.xiaosong.common;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.xiaosong.constant.Constant;
import com.xiaosong.constant.Constants;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LoginController extends Controller {
    private Logger logger = Logger.getLogger(LoginController.class);

    public void index() {

        List<String> list = new ArrayList<>();
        String user = "admin";
        String pass = "123456";
        list.add(user);
        list.add(pass);
        String userName = getPara("username");
        String password = getPara("password");
        if (!user.equals(userName)) {
            logger.error("用户名不正确");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "用户名不正确"));
        }
        else if (!pass.equals(password)) {
            logger.error("密码不正确");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "密码不正确"));
        }else{
            Constants.login=getSession().getId();
            System.out.println("login:"+Constants.login);
            CacheKit.put("LoginUserCache", Constants.login, list); // 将用户信息保存到缓存，用作超时判断
            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL,"登录成功"));
//            Constants.login = 0;
//            if(Constants.login == 0){
//                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL,"登录成功"));
//            }
        }

    }

}
