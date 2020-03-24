package com.xiaosong.common.penetrate;

import com.jfinal.core.Controller;
import com.xiaosong.common.server.ServerService;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbServerinfo;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.io.IOException;

public class PenetrateController extends Controller {
    private static ServerService srv = ServerService.me;
    private Logger logger = Logger.getLogger(PenetrateController.class);

    public void index() {
        String netappValue = getPara("natappValue");
        String token = getPara("token");
        try {
            TbServerinfo ser = srv.findSer();
            String value = null;
            if (netappValue.equals("true")) {
                value = "true";
                if (token.isEmpty()) {
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                    return;
                }
                ser.setNetappValue(value);
                ser.setToKen(token);
                boolean update = ser.update();
                if (update) {
                    String s= "nohup ./natapp -authtoken="+token+" -log=stdout &";
                    System.out.println("===="+s);
                    int i = Runtime.getRuntime().exec(s).waitFor();
                    if (i == 1) {
                        logger.error("内网穿透配置成功");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "内网穿透配置成功"));
                    } else {
                        logger.error("内网穿透配置失败");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "内网穿透配置失败"));
                    }
                } else {
                    logger.error("内网穿透配置失败");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "内网穿透配置失败"));
                }
            } else {
                value = "false";
                token = "";
                ser.setNetappValue(value);
                ser.setToKen(token);
                boolean update = ser.update();
                if (update) {
                    logger.error("内网穿透配置成功");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "内网穿透配置成功"));
                } else {
                    logger.error("内网穿透配置失败");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "内网穿透配置失败"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("内网穿透异常");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "内网穿透异常"));
        }

    }

}
