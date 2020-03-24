package com.xiaosong.common.floor;

import com.jfinal.core.Controller;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbBuilding;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 大楼配置
 */

public class FloorController extends Controller {
    public FloorService srv = FloorService.me;
    private static Logger logger = Logger.getLogger(FloorController.class);

    /**
     * 添加大楼信息
     */
    public void save() {
        try {
            //获取前台数据
            String orgCode = getPara("orgCode");     //大楼编码
            String orgName = getPara("orgName");     //大楼名称
            String pospCode = getPara("pospCode");   //上位机编码
            String netType = getPara("netType");     //联网方式（1-外网  2-内网）
            String faceComparesCope = getPara("faceComparesCope");//人脸比对阀值
            String accessType = getPara("accessType");//设备配置方式（1-二维码，2-人像识别，3-二维码+人像识别，4-二维码或人像识别）
            String key = getPara("key");             //服务器秘钥

            String str = null;
            if (netType.equals("0")) {
                str = "内网";
            } else {
                str = "外网";
            }
            TbBuilding td = getModel(TbBuilding.class);
            td.setOrgCode(orgCode);
            td.setOrgName(orgName);
            td.setPospCode(pospCode);
            td.setNetType(str);
            td.setFaceComparesCope(faceComparesCope);
            td.setAccessType(accessType);
            td.setKey(key);
            String code = srv.findByOrgCode();

            if(orgCode!=null){
                if (code == null) {
                    boolean save = td.save();
                    if (save) {
                        logger.info("添加成功~");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "保存成功"));
                    } else {
                        logger.error("添加失败");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "保存成功"));
                    }
                } else {
                    int update = srv.update(td, code);
                    if (update == 1) {
                        logger.info("修改成功");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "修改成功"));
                    } else {
                        logger.info("修改失败");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "修改失败"));
                    }
                }

            }else{
                logger.error("操作失败");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, null));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    /**
     * 删除 大楼配置信息
     */
    public void delete() {
        try {
            //获取前台数据
            String orgCode = getPara("orgCode");
            int i = srv.deleteFloor(orgCode);
            if (i == 1) {
                logger.info("删除大楼信息成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "删除大楼信息成功~"));
            } else {
                logger.error("删除大楼信息失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除大楼信息失败~"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    /**
     * 查询所有的大楼信息数据
     */
    public void index() {
        try {
            TbBuilding floor = srv.findFloor();
            if (floor != null) {
                logger.info("大楼信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, floor));
            } else {
                logger.error("大楼信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_NORMAL,null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "查询异常~"));
        }
    }

}
