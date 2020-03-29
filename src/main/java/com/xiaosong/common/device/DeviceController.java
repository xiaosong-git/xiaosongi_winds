package com.xiaosong.common.device;

import com.dhnetsdk.date.Constant;
import com.jfinal.core.Controller;
import com.sun.jna.Pointer;
import com.xiaosong.config.MinniSDK;
import com.xiaosong.config.MinniSDK.*;
import com.xiaosong.config.SendAccessRecord;
import com.xiaosong.config.devicesInit;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbDevice;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备配置
 */

public class DeviceController extends Controller {
    private DeviceService srv = DeviceService.me;
    private static Logger logger = Logger.getLogger(DeviceController.class);

    /**
     * 保存设备
     **/
    public void save() {
        try {
            //获取前台数据
//            String deviceId = getPara("deviceId");      //  设备编号（主键1）
            String deviceMode = getPara("deviceMode");  //  设备类别（FACE-人脸设备，QRCODE-二维码读头，SWI-上位机）
            String deviceIp = getPara("deviceIp");      //  设备IP地址
            String devicePort = getPara("devicePort");  //   设备使用端口
            String FQ_turnover = getPara("FQ_turnover");// 进出标识(针对人脸设备及二维码设备)
            String E_out = getPara("E_out");            //继电器输出口(4路或24路)
            String status = getPara("status");          //设备状态（running-使用、free-闲置、bad-损坏）
            String deviceType = getPara("deviceType");  //设备型号（TPS980、K5607等）
            String contralFloor = getPara("contralFloor");  //设备控制的楼层
            String relayController = getPara("relayController");  //控制器（0-自有，1-外接）
            String relayIP = getPara("relayIP");        //继电器IP地址
            String relayPort = getPara("relayPort");    //继电器端口

            String admin = getPara("admin");            //设备登录账号
            String password = getPara("password");      //设备登录密码
            String idCard = getPara("idCard");          //身份证号码
            String type = getPara("type");              //设备登录密码
            String name = getPara("name");              //设备登录密码

            //  设备类别（FACE-人脸设备，QRCODE-二维码读头）
            String Mode = null;
            if (deviceMode.equals("FACE")) {
                Mode = "人脸设备";
            } else {
                Mode = "二维码读头";
            }
            // 进出标识(针对人脸设备及二维码设备)
            String fq = null;
            if (FQ_turnover.equals("0")) {
                fq = "进";
            } else {
                fq = "出";
            }
            //设备状态（running-使用、free-闲置、bad-损坏）
            String deviceStatus = null;
            if (status.equals("0")) {
                deviceStatus = "使用";
            } else {
                deviceStatus = "闲置";
            }
            //控制器（0-自有，1-外接）
            String controller = null;
            if(relayController.equals("0")){
                controller = "自有";
            }else{
                controller = "外接";
            }
            //创建实体类对象 并赋值
            TbDevice td = getModel(TbDevice.class);
//            td.setDeviceId(deviceId);
            td.setDeviceMode(Mode);
            td.setDeviceIp(deviceIp);
            td.setDevicePort(Integer.valueOf(devicePort));
            td.setFqTurnover(fq);
            td.setEOut(E_out);
            td.setStatus(deviceStatus);
            td.setDeviceType(deviceType);
            td.setContralFloor(contralFloor);
            td.setRelayController(controller);
            td.setRelayIP(relayIP);
            td.setRelayPort(relayPort);
            td.setAdmin(admin);
            td.setPassword(password);

            //保存设备
            boolean save = srv.save(td);
            if (save) {
                // 根据设备型号 进行长连接
                List<TbDevice> devices = srv.findByDevName("人脸设备", "使用");

                for (int i = 0; i < devices.size(); i++) {
                    if (deviceType.contains("TPS980")) {
                        //海景设备

                        boolean bool = srv.HJInfo(type, deviceIp, name, idCard);
                        if (bool) {
                            logger.info("添加通行记录成功..");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "添加通行记录.."));
                        } else {
                            logger.info("添加通行记录失败..");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "添加通行失败.."));

                        }
                    } else if (deviceType.equals("DS-K5671") || deviceType.equals("DS-2CD8627FWD")) {
                        //linux下 初始化 海康sdk
//                        InitHCNetSDK.run(deviceType);
                        //winds下 初始化海康SDK
                        devicesInit.initHC();
                        SendAccessRecord accessRecord = new SendAccessRecord();
                        logger.info("长连接" + devices.get(i).getDeviceIp());
                        accessRecord.sendAccessRecord(devices.get(i).getDeviceIp(), admin, password);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (deviceType.equals("DH-ASI728")) {
                        //初始化大华设备
                        devicesInit.initDH();

                        //选型有大华门禁设备需要启动长连接做继电器开门
                        //查找所有运行的大华设备做长连接
                        if (devices.get(i).getDeviceType().contains("DH-ASI728")) {
                            SendAccessRecord accessRecord = new SendAccessRecord();

                            logger.info("长连接" + devices.get(i).getDeviceIp());
                            Map<String, String> map = new HashMap<String, String>();
                            map.put(com.dhnetsdk.date.Constant.deviceIp, devices.get(i).getDeviceIp());
                            map.put(com.dhnetsdk.date.Constant.username, admin);
                            map.put(com.dhnetsdk.date.Constant.password, password);

                            accessRecord.dhSendAccessRecord(map);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();

                            }
                        }
                        //旷世设备
                    }else if(deviceType.equals("KS-250")){
                        MinniSDK minniSDK = MinniSDK.INSTANCE;
                        //初始化旷世设备
                        BoxSDK_Config boxSDK_config = new BoxSDK_Config();
                        Pointer user_data = boxSDK_config.user_data;
                        int isInit = minniSDK.BoxSDK_init(user_data);
                        if(isInit==0){
                            logger.info("BoxSDK init() success");
                            //登录设备
                            Map<String, String> map = new HashMap<String, String>();
                            map.put(com.dhnetsdk.date.Constant.deviceIp, devices.get(i).getDeviceIp());
                            map.put(com.dhnetsdk.date.Constant.username, admin);
                            map.put(com.dhnetsdk.date.Constant.password, password);
                            map.put(String.valueOf(Constant.devicePort), devicePort);
                            //登录并开启人脸匹对
                            srv.login(map);
                        }else{
                            logger.error("BoxSDK init() failed, error code："+isInit);
                        }
                    }
                }
                logger.info("保存成功");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "保存成功"));
            } else {
                logger.error("保存失败");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "保存失败"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("设备异常");
            renderJson(RetUtil.ok(ErrorCodeDef.CODE_ERROR, "设备异常"));
        }
    }

    public void update() {
        try {
            //获取前台数据
            String deviceId = getPara("deviceId");      //  设备编号（主键1）
            String deviceMode = getPara("deviceMode");  //  设备类别（FACE-人脸设备，QRCODE-二维码读头，SWI-上位机）
            String deviceIp = getPara("deviceIp");      //  设备IP地址
            String devicePort = getPara("devicePort");  //   设备使用端口
            String FQ_turnover = getPara("FQ_turnover");// 进出标识(针对人脸设备及二维码设备)
            String E_out = getPara("E_out");            //继电器输出口(4路或24路)
            String status = getPara("status");          //设备状态（running-使用、free-闲置、bad-损坏）
            String deviceType = getPara("deviceType");  //设备型号（TPS980、K5607等）
            String contralFloor = getPara("contralFloor");  //设备控制的楼层
            String admin = getPara("admin");            //设备登录账号
            String password = getPara("password");      //设备登录密码
            String idCard = getPara("idCard");          //身份证号码
            String type = getPara("type");              //设备登录密码
            String name = getPara("name");              //设备登录密码
            String relayController = getPara("relayController");     //控制器（0-自有，1-外接）
            String relayIP = getPara("relayIP");                     //继电器ip
            String relayPort = getPara("relayPort");                //继电器端口


            //  设备类别（FACE-人脸设备，QRCODE-二维码读头）
            String Mode = null;
            if (deviceMode.equals("FACE")) {
                Mode = "人脸设备";
            } else {
                Mode = "二维码读头";
            }
            // 进出标识(针对人脸设备及二维码设备)
            String fq = null;
            if (FQ_turnover.equals("0")) {
                fq = "进";
            } else {
                fq = "出";
            }
            //控制器（0-自有，1-外接）
            String controller = null;
            if(relayController.equals("0")){
                controller = "自有";
            }else{
                controller = "外接";
            }
            //设备状态（running-使用、free-闲置、bad-损坏）
            String deviceStatus = null;
            if (status.equals("0")) {
                deviceStatus = "使用";
            } else {
                deviceStatus = "闲置";
            }
            //创建实体类对象 并赋值
            TbDevice td = getModel(TbDevice.class);
            td.setDeviceId(deviceId);
            td.setDeviceMode(Mode);
            td.setDeviceIp(deviceIp);
            td.setDevicePort(Integer.valueOf(devicePort));
            td.setFqTurnover(fq);
            td.setEOut(E_out);
            td.setContralFloor(contralFloor);
            td.setStatus(deviceStatus);
            td.setDeviceType(deviceType);
            td.setAdmin(admin);
            td.setPassword(password);
            td.setRelayController(controller);
            td.setRelayIP(relayIP);
            td.setRelayPort(relayPort);

            //修改设备
            boolean update = srv.update(td);

            //查看大楼的设备选型
            List<String> types = srv.deviceByType(deviceId);
            logger.info(types.toString());
            //修改成功后操作
            if (update) {

                if (types.contains("TPS980")) {
                    //海景设备
                    boolean bool = srv.HJInfo(type, deviceIp, name, deviceId);
                    if (bool) {
                        logger.info("布防成功..");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "布防成功.."));
                    } else {
                        logger.info("布防失败..");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "布防失败.."));

                    }
                } else if (types.contains("DS-K5671") || types.contains("DS-2CD8627FWD")) {
                    //选型有海康门禁设备需要启动长连接做继电器开门
                    List<TbDevice> devices = srv.findByDevName("人脸设备", "使用");
                    //查找所有运行的海康设备做长连接
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getDeviceType().contains("DS-K5671") || devices.get(i).getDeviceType().contains("DS-2CD8627FWD")) {
                            SendAccessRecord accessRecord = new SendAccessRecord();
                            //linux 下 加载 海康sdk
//                            InitHCNetSDK.run(deviceType);
                            // winds 下海康设备就初始化海康SDK
                            devicesInit.initHC();

                            logger.info("长连接" + devices.get(i).getDeviceIp());
                            accessRecord.sendAccessRecord(devices.get(i).getDeviceIp(), admin, password);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (types.contains("DH-ASI728")) {

                    //初始化大华设备
                    devicesInit.initDH();

                    //选型有大华门禁设备需要启动长连接做继电器开门
                    List<TbDevice> devices = srv.findByDevName("人脸设备", "使用");
                    //查找所有运行的大华设备做长连接
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getDeviceType().contains("DH-ASI728")) {
                            SendAccessRecord accessRecord = new SendAccessRecord();

                            logger.info("长连接" + devices.get(i).getDeviceIp());
                            Map<String, String> map = new HashMap<String, String>();
                            map.put(com.dhnetsdk.date.Constant.deviceIp, devices.get(i).getDeviceIp());
                            map.put(com.dhnetsdk.date.Constant.username, admin);
                            map.put(com.dhnetsdk.date.Constant.password, password);

                            accessRecord.dhSendAccessRecord(map);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
                logger.info("修改成功!");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "修改成功!"));
            } else {
                logger.error("修改失败");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_ERROR, "修改失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }

    }

    /**
     * 删除设备的信息
     */
    public void delete() {
        try {
            //获取前台数据
            String deviceId = getPara("deviceId");
            int i = srv.deleteDevice(deviceId);
            if (i == 1) {
                logger.info("删除设备信息成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "删除设备信息成功~"));
            } else {
                logger.info("删除设备信息失败~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "删除设备信息失败~"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    public void batchDel() {
        try {
            //获取前台数据
            String deviceId = getPara("deviceId");
            String[] split = deviceId.split(",");
            for (String id : split) {
                int i = srv.deleteDevice(id);
                if (i == 1) {
                    logger.info("批量删除设备信息成功~");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "批量删除设备信息成功~"));
                } else {
                    logger.info("批量删除设备信息失败~");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "批量删除设备信息失败~"));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    /**
     * 查询 所有的设备
     */
    public void index() {
        try {
            List<TbDevice> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = 10;
            int index = (page - 1) * number;
            List<TbDevice> devices = srv.findDevice();
            for (int i = index; i < devices.size() && i < (index + number); i++) {
                list.add(devices.get(i));
            }
            if (list != null) {
                logger.info("设备信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, devices.size()));
            } else {
                logger.error("设备信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "设备信息查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    /**
     * 条件查询
     */
    public void findByType(){
        try {
            List<TbDevice> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = 10 ;
            int index = (page - 1) * number;

            String deviceMode = getPara("deviceMode");

            List<TbDevice> companyusers = null;
            if (deviceMode != null) {
                companyusers = srv.findByType(deviceMode);
            } else {
                companyusers = srv.findDevice();
            }

            for (int i = index; i < companyusers.size() && i < (index + number); i++) {
                list.add(companyusers.get(i));
            }
            if (companyusers != null) {
                logger.info("设备查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, companyusers.size()));
            } else {
                logger.error("设备查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "设备查询失败"));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }
}
