package com.xiaosong.common.device;

import com.dhnetsdk.date.Constant;
import com.jfinal.core.Controller;
import com.xiaosong.config.MinniSDK;
import com.xiaosong.config.MinniSDK.*;
import com.xiaosong.config.SendAccessRecord;
import com.xiaosong.config.devicesInit;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbDevice;
import com.xiaosong.util.MD5Util;
import com.xiaosong.util.RetUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 设备配置
 */

public class DeviceController extends Controller {
    private DeviceService srv = DeviceService.me;
    private static Logger logger = Logger.getLogger(DeviceController.class);

    public static int enable_DAS = 1;
    public static int dAS_port = 9800;

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
            String status = getPara("status");          //设备状态（running-使用、free-闲置）
            String deviceType = getPara("deviceType");  //设备型号（TPS980、K5607等）
            String contralFloor = getPara("contralFloor");  //设备控制的楼层
            String relayController = getPara("relayController");  //控制器（0-自有，1-外接）
            String relayIP = getPara("relayIP");        //继电器IP地址
            String relayPort = getPara("relayPort");    //继电器端口

            String admin = getPara("admin");            //设备登录账号
            String password = getPara("password");      //设备登录密码

            String remark = getPara("remark");      //设备备注

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
            //设备状态（running-使用、free-闲置）
            String deviceStatus = null;
            if (status.equals("0")) {
                deviceStatus = "running";
            } else {
                deviceStatus = "free";
            }
            //控制器（0-自有，1-外接）
            String controller = null;
            if (relayController.equals("0")) {
                controller = "自有";
            } else if(relayController.equals("1")){
                controller = "外接";
            }else{
                controller = "无";
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
            if(remark==null){
                String random= RandomStringUtils.randomAlphanumeric(1);
                td.setRemark(random);
            }else{
                td.setRemark(remark);
            }

            //保存设备
            boolean save = srv.save(td);
            if (save) {
                // 根据设备型号 进行长连接
                List<TbDevice> devices = srv.findByDevName("人脸设备", "running");

                for (int i = 0; i < devices.size(); i++) {
                    if (deviceType.equals("DS-K5671") || deviceType.equals("DS-2CD8627FWD")) {
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
                    } else if (deviceType.equals("KS-250")) {
                        MinniSDK minniSDK = MinniSDK.INSTANCE;
                        BoxSDK_FACE_RESULT_CALLBACK face_result_callback = new MessageHander();
                        dAS_port = Integer.parseInt(devicePort);
                        //初始化旷世设备
                        BoxSDK_Config.ByReference boxSDK_config = new BoxSDK_Config.ByReference();
                        boxSDK_config.device_status_callback = null;        // 设备状态回调，此回调主要通知设备的上线和下线
                        boxSDK_config.ipc_status_callback = null;           // ipc状态回调，此回调主要通知ipc的上线和下线
                        boxSDK_config.face_result_callback = face_result_callback;          // 人脸识别结果回调
                        boxSDK_config.plate_result_callback = null;         // 车牌识别结果回调
                        boxSDK_config.face_file_result_callback = null;     // 人脸文件管理结果回调
                        boxSDK_config.plate_info_result_callback = null;    // 车牌信息管理结果回调
                        boxSDK_config.upgrade_device_callback = null;       // 升级设备的进度结果回调
                        boxSDK_config.face_history_result_callback = null;  // 人脸历史结果回调
                        boxSDK_config.plate_history_result_callback = null; // 车牌历史结果回调
                        boxSDK_config.face_feature_callback = null;         // 特征抽取结果回调
                        boxSDK_config.internal_info_callback = null;        // SDK内部信息回调，主要用于监控分析SDK和device的一些异常状态
                        boxSDK_config.user_data = null;                     // 用户数据
                        boxSDK_config.enable_DAS = enable_DAS;
                        boxSDK_config.DAS_port = dAS_port;          //设备端口

                        int isInit = minniSDK.BoxSDK_init(boxSDK_config);
                        if (isInit == 0) {
                            logger.info("BoxSDK init() success");
                            //登录设备
                            Map<String, String> map = new HashMap<String, String>();
                            map.put(com.dhnetsdk.date.Constant.deviceIp, devices.get(i).getDeviceIp());
                            map.put(com.dhnetsdk.date.Constant.username, admin);
                            map.put(com.dhnetsdk.date.Constant.password, password);
                            map.put(String.valueOf(Constant.devicePort), devicePort);
                            //登录并开启人脸匹对
                            srv.login(map);
                        } else {
                            logger.error("BoxSDK init() failed, error code：" + isInit);
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

    /**
     * 修改设备
     */
    public void update() {
        try {
            //获取前台数据
            String deviceId = getPara("deviceId");      //  设备编号（主键1）
            String deviceMode = getPara("deviceMode");  //  设备类别（FACE-人脸设备，QRCODE-二维码读头，SWI-上位机）
            String deviceIp = getPara("deviceIp");      //  设备IP地址
            String devicePort = getPara("devicePort");  //   设备使用端口
            String FQ_turnover = getPara("FQ_turnover");// 进出标识(针对人脸设备及二维码设备)
            String E_out = getPara("E_out");            //继电器输出口(4路或24路)
            String status = getPara("status");          //设备状态（running-使用、free-闲置）
            String deviceType = getPara("deviceType");  //设备型号（TPS980、K5607等）
            String contralFloor = getPara("contralFloor");  //设备控制的楼层
            String admin = getPara("admin");            //设备登录账号
            String password = getPara("password");      //设备登录密码
            String relayController = getPara("relayController");     //控制器（0-自有，1-外接）
            String relayIP = getPara("relayIP");                     //继电器ip
            String relayPort = getPara("relayPort");                //继电器端口

            String remark = getPara("remark");                //继电器端口


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
            if (relayController.equals("0")) {
                controller = "自有";
            } else {
                controller = "外接";
            }
            //设备状态（running-使用、free-闲置）
            String deviceStatus = null;
            if (status.equals("0")) {
                deviceStatus = "running";
            } else {
                deviceStatus = "free";
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
            if(remark==null){
                String random= RandomStringUtils.randomAlphanumeric(1);
                td.setRemark(random);
            }else{
                td.setRemark(remark);
            }

            //修改设备
            boolean update = srv.update(td);

            //查看大楼的设备选型
            List<String> types = srv.deviceByType(deviceId);
            logger.info(types.toString());
            //修改成功后操作
            if (update) {
                if (types.contains("DS-K5671") || types.contains("DS-2CD8627FWD")) {
                    //选型有海康门禁设备需要启动长连接做继电器开门
                    List<TbDevice> devices = srv.findByDevName("人脸设备", "running");
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
                    List<TbDevice> devices = srv.findByDevName("人脸设备", "running");
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
                    //旷世设备
                } else if (deviceType.equals("KS-250")) {
//                    List<TbDevice> devices = srv.findByDevName("人脸设备", "running");
//                    dAS_port = Integer.parseInt(devicePort);
//                    MinniSDK minniSDK = MinniSDK.INSTANCE;
//                    BoxSDK_FACE_RESULT_CALLBACK face_result_callback = new MessageHander();
//                    for (int i = 0; i < devices.size(); i++) {
//                        //初始化旷世设备

//                        BoxSDK_Config.ByReference boxSDK_config = new BoxSDK_Config.ByReference();
//                        boxSDK_config.device_status_callback = null;        // 设备状态回调，此回调主要通知设备的上线和下线
//                        boxSDK_config.ipc_status_callback = null;           // ipc状态回调，此回调主要通知ipc的上线和下线
//                        boxSDK_config.face_result_callback = face_result_callback; // 人脸识别结果回调
//                        boxSDK_config.plate_result_callback = null;         // 车牌识别结果回调
//                        boxSDK_config.face_file_result_callback = null;     // 人脸文件管理结果回调
//                        boxSDK_config.plate_info_result_callback = null;    // 车牌信息管理结果回调
//                        boxSDK_config.upgrade_device_callback = null;       // 升级设备的进度结果回调
//                        boxSDK_config.face_history_result_callback = null;  // 人脸历史结果回调
//                        boxSDK_config.plate_history_result_callback = null; // 车牌历史结果回调
//                        boxSDK_config.face_feature_callback = null;         // 特征抽取结果回调
//                        boxSDK_config.internal_info_callback = null;        // SDK内部信息回调，主要用于监控分析SDK和device的一些异常状态
//                        boxSDK_config.user_data = null;                     // 用户数据
//                        boxSDK_config.enable_DAS = enable_DAS;
//                        boxSDK_config.DAS_port = dAS_port;          //设备端口

//                        int isInit = minniSDK.BoxSDK_init(boxSDK_config);
//                        if (isInit == 0) {
//                            logger.info("BoxSDK init() success");
//                            //登录设备
//                            Map<String, String> map = new HashMap<String, String>();
//                            map.put(com.dhnetsdk.date.Constant.deviceIp, devices.get(i).getDeviceIp());
//                            map.put(com.dhnetsdk.date.Constant.username, admin);
//                            map.put(com.dhnetsdk.date.Constant.password, password);
//                            map.put(String.valueOf(Constant.devicePort), devicePort);
//                            //登录并开启人脸匹对
//                            srv.login(map);
//                        } else {
//                            logger.error("BoxSDK init() failed, error code：" + isInit);
//                        }
//                    }
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

    /**
     * 批量删除
     */
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
    public void findByType() {
        try {
            List<TbDevice> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = 10;
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

    public void findKS(){
        try {
            List<TbDevice> deviceList = srv.findKS();
            if (deviceList != null) {
                logger.info("旷世设备查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, deviceList, deviceList.size()));
            } else {
                logger.error("旷世设备查询失败,没有该设备~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "旷世设备查询失败,没有该设备~"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("旷世设备查询异常");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "旷世设备查询异常"));
        }
    }
}
