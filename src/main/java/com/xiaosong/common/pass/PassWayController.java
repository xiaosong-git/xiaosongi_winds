package com.xiaosong.common.pass;

import com.jfinal.core.Controller;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbBuilding;
import com.xiaosong.util.*;
import org.apache.log4j.Logger;

/**
 * 通行方式
 */

public class PassWayController extends Controller {
    private PassWayService srv = PassWayService.me;
    private static Logger logger = Logger.getLogger(PassWayController.class);

    /**
     * 查询大楼的 通行方式
     */
    public void index() {
        try {
            //查询大楼中的唯一编号
            TbBuilding tb = srv.findOrgCode();
            TbBuilding tbBuilding = srv.findByPassWay(tb.getOrgCode());
            if (tbBuilding != null) {
                logger.info("查询成功");
                renderJson(RetUtil.ok(tbBuilding));
            } else {
                logger.error("查询失败");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_ERROR, "查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询异常");
            renderJson(RetUtil.ok(ErrorCodeDef.CODE_ERROR, "查询异常~"));
        }
    }

    /**
     * 修改大楼的 通行方式
     */
    public void save() {

        try {
            //前端传入参数
            String user = getPara("staffCheckType");         //员工通行方式
            String vis = getPara("visitorCheckType");     //访客通行方式
            String sha = getPara("shareCheckType");         //共享通行方式

            //查询大楼中的唯一编号
            TbBuilding tb = srv.findOrgCode();
            //根据大楼编号 修改通行方式 员工通行方式
            String staff = null;
            if (user.equals("0")) {
                staff = "人脸通行";
            }else if(user.equals("1")){
                staff = "二维码通行";
            }else if(user.equals("2")){
                staff = "人脸二维码通行";
            }else if(user.equals("3")){
                staff = "禁止通行";
            }
            //根据大楼编号 修改通行方式 访客通行方式
            String visitor = null;
            if (vis.equals("0")) {
                visitor = "人脸通行";
            }else if(vis.equals("1")){
                visitor = "二维码通行";
            }else if(vis.equals("2")){
                visitor = "人脸二维码通行";
            }else if(vis.equals("3")){
                visitor = "禁止通行";
            }
            //根据大楼编号 修改通行方式 共享通行方式
            String share = null;
            if (sha.equals("0")) {
                share = "人脸通行";
            }else if(sha.equals("1")){
                share = "二维码通行";
            }else if(sha.equals("2")){
                share = "人脸二维码通行";
            }else if(sha.equals("3")){
                share = "禁止通行";
            }
            int byStaff = srv.updateByPassWay(staff, visitor, share, tb.getOrgCode());
            if (byStaff == 1) {
                logger.info("通行方式添加成功");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "通行方式添加成功"));
            } else {
                logger.error("通行方式添加失败");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_ERROR, "通行方式添加失败~"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("添加异常");
            renderJson(RetUtil.ok(ErrorCodeDef.CODE_ERROR, "添加异常~"));
        }
    }

}
