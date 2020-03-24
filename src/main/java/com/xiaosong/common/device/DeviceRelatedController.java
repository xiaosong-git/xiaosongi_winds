package com.xiaosong.common.device;

import com.jfinal.core.Controller;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbDevicerelated;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 设备使用配置
 */
public class DeviceRelatedController extends Controller {
    public DeviceRelatedService srv = DeviceRelatedService.me;
    private static Logger logger = Logger.getLogger(DeviceRelatedController.class);


    /**
     * 查询设备使用的所有记录
     */
    public void index() {
        try {
            List<TbDevicerelated> record = srv.findRecord();
            if (record != null) {
                logger.info("大楼信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, record, record.size()));
            } else {
                logger.error("大楼信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "大楼信息查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "查询异常~"));
        }
    }

    /**
     * 保存设备使用记录
     */
    public void save() {
        String relatedId = getPara("relatedId");  //关联编号(自增)（主键1）
        String faceIP = getPara("faceIP");        //人脸设备IP地址
        String QRCodeIP = getPara("QRCodeIP");    //读头IP地址
        String relayIP = getPara("relayIP");      //继电器IP地址
        String relayPort = getPara("relayPort");  //继电器端口
        String relayOUT = getPara("relayOUT");    //继电器电源输出口
        String contralFloor = getPara("contralFloor");  //对应的控制楼层
        String turnOver = getPara("turnOver");     //进出标识（in/out）

        TbDevicerelated td = getModel(TbDevicerelated.class);
        td.setRelatedId(Integer.valueOf(relatedId));
        td.setFaceIP(faceIP);
        td.setQRCodeIP(QRCodeIP);
        td.setRelayIP(relayIP);
        td.setRelayPort(relayPort);
        td.setRelayOUT(relayOUT);
        td.setContralFloor(contralFloor);
        td.setTurnOver(turnOver);

        boolean save = srv.save(td);
        if (save) {
            logger.info("设备使用添加成功");
            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "服务器添加成功"));
        } else {
            logger.info("服务器添加失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器添加失败"));
        }
    }

    /**
     * 修改设备使用记录
     */
    public void update() {
        try {
            String relatedId = getPara("relatedId");  //关联编号(自增)（主键1）
            String faceIP = getPara("faceIP");        //人脸设备IP地址
            String QRCodeIP = getPara("QRCodeIP");    //读头IP地址
            String relayIP = getPara("relayIP");      //继电器IP地址
            String relayPort = getPara("relayPort");  //继电器端口
            String relayOUT = getPara("relayOUT");    //继电器电源输出口
            String contralFloor = getPara("contralFloor");  //对应的控制楼层
            String turnOver = getPara("turnOver");     //进出标识（in/out）

            TbDevicerelated td = getModel(TbDevicerelated.class);
            td.setRelatedId(Integer.valueOf(relatedId));
            td.setFaceIP(faceIP);
            td.setQRCodeIP(QRCodeIP);
            td.setRelayIP(relayIP);
            td.setRelayPort(relayPort);
            td.setRelayOUT(relayOUT);
            td.setContralFloor(contralFloor);
            td.setTurnOver(turnOver);
            boolean update = srv.update(td);
            if (update) {
                logger.info("设备使用修改成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "设备使用修改成功~"));
            } else {
                logger.error("设备使用修改失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "设备使用修改失败~"));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("设备使用异常!");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "设备使用异常!"));
        }

    }

    /**
     * 删除设备使用记录
     */
    public void delete() {
        try {
            //前端传入参数
            String relatedId = getPara("relatedId");  //关联编号(自增)（主键1）
            int delete = srv.delete(relatedId);
            if (delete == 1) {
                logger.info("删除设备使用成功.");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "删除设备使用成功"));
            } else {
                logger.error("删除设备使用失败!");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除设备使用失败!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("设备使用异常!");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "设备使用异常!"));
        }
    }
}