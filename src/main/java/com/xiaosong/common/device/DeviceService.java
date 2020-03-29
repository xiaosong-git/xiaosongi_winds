package com.xiaosong.common.device;


import com.dhnetsdk.date.Constant;
import com.jfinal.plugin.activerecord.Db;
import com.sun.jna.Pointer;
import com.xiaosong.config.MinniSDK;
import com.xiaosong.config.MinniSDK.*;
import com.xiaosong.model.*;
import com.xiaosong.util.Control24DeviceUtil;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class DeviceService {
    public static final DeviceService me = new DeviceService();
    private static Logger logger = Logger.getLogger(DeviceService.class);

    /**
     * 根据设备编号 查询 产品选型
     *
     * @param deviceId 设备编号
     * @return
     */
    public List<String> deviceByType(String deviceId) {

        TbDevice tb = TbDevice.dao.findFirst("select * from tb_device where deviceid = ?", deviceId);
        String deviceSelect = tb.getDeviceType();

        if (null == deviceSelect) {
            return null;
        } else {
            List<String> deviceTypes = new ArrayList<String>();
            deviceTypes = Arrays.asList(deviceSelect.split("\\|"));
            return deviceTypes;
        }
    }


    /**
     * 根据条件  查询所有设备
     *
     * @param face    设备类型
     * @param running 状态
     * @return
     */
    public List<TbDevice> findByDevName(String face, String running) {
//        return Db.find("select d.* from tb_devices d where d.deviceName = ? and d.status = ?", face, running);
        return TbDevice.dao.find("select d.* from tb_device d where d.deviceMode = ? and d.status = ?", face, running);
    }

    /**
     * 根据ip查询 大楼配置表
     *
     * @param deviceIp 设备ip
     * @return
     */
    public TbDevice findByFaceIP(String deviceIp) {
        List<TbDevice> list = TbDevice.dao.find("select * from tb_device where deviceIp = ?", deviceIp);
        if (list.size() <= 0 || list == null) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 根据userId查询用户名的数据
     *
     * @param userId
     * @return
     */
    public TbCompanyuser findByUserId(int userId) {
        List<TbCompanyuser> list = TbCompanyuser.dao.find("select * from tb_companyuser where userId = ?", userId);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 根据身份证号查询 访问者信息
     *
     * @param idCardNo 身份证号
     * @return
     */
    public TbVisitor findVisitorId(String idCardNo) {
        List<TbVisitor> list = TbVisitor.dao.find("select * from tb_visitor where id  = ?", idCardNo.substring(1));
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }

    }

    /**
     * 根据条件查询所有的 访问者
     *
     * @param userrealname 访问人姓名
     * @param idNO         身份证号
     * @param dateTime     访问时间
     * @return
     */
    public List<TbVisitor> findByBetweenTime(String userrealname, String idNO, String dateTime) {
        return TbVisitor.dao.find("SELECT * from tb_visitor where visitorName= ? AND visitorIdCard= ? and ? between preStartTime and endDateTime", userrealname, idNO, dateTime);
    }

    /**
     * 查询设备的人脸对阀值
     *
     * @return
     */
    public String findFaceComparesCope() {
        TbBuilding tbDl = TbBuilding.dao.findFirst("SELECT * from tb_building ");
        return tbDl.getFaceComparesCope();

    }

    /**
     * 通过设备IP查找设备
     *
     * @param faceIP ip
     * @return
     */
    public TbDevice findByDeviceIp(String faceIP) {
        List<TbDevice> list = TbDevice.dao.find("select * from tb_device WHERE deviceIp = ?", faceIP);
        if (list.size() <= 0 || list == null) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 查询 设备的大楼编号
     *
     * @return
     */
    public String findOrgId() {
        return Db.queryStr("SELECT orgCode from tb_building ");
    }

    /**
     * 查询 设备的 上位机编号
     *
     * @return
     */
    public String findPospCode() {
        return Db.queryStr("SELECT pospcode from tb_building");
    }


    /**
     * 查询设备id
     */
    public List<TbDevice> findByDeviceId() {
        List<TbDevice> list = TbDevice.dao.find("select deviceId from tb_device");
        if (list.size() <= 0 || list == null) {
            return null;
        }
        return list;

    }

    /**
     * 保存设备
     *
     * @param device 设备
     * @return
     */
    public boolean save(TbDevice device) {
        return device.save();
    }

    /**
     * 修改设备
     *
     * @param device 设备
     */
    public boolean update(TbDevice device) {
        return device.update();
    }

    /**
     * 根据用户id 查询 共享数据表
     *
     * @param userId 用户id
     * @return
     */
    public TbShareroom findByUser(int userId) {
        List<TbShareroom> list = TbShareroom.dao.find("select * from tb_shareRoom where userId = ?", userId);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 保存通行记录到数据库
     *
     * @param accessRecord 通行记录参数
     */
    public void saveAccessrecord(TbAccessrecord accessRecord) {
        accessRecord.save();
    }

    /**
     * 海景通行记录
     *
     * @param type     类型
     * @param deviceIp 设备ip
     * @param name     用户
     * @param idCard   身份证号码
     * @return
     */
    public boolean HJInfo(String type, String deviceIp, String name, String idCard) throws Exception {
        TbDevice device = findByDeviceIp(deviceIp);
        TbDevicerelated devRelated = TbDevicerelated.dao.findFirst("select * from tb_devicerelated where faceIP = ?", deviceIp);
        if (null == device) {
            return false;
        }
        if (type.equals("visitor")) {

            if (devRelated.getTurnOver().equals("out")) {
                String card = "v_" + name + "_" + idCard;
//                if (redisUtils.get(key) == null) {
//                    redisUtils.set("v_" + name + "_" + idCard, "locked");
//                    redisUtils.expire(key, 4);
//                    open(devRelated, name, idCard, type);
//                    return RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "成功");
//                } else {
//                    return RetUtil.fail(ErrorCodeDef.CODE_ERROR, "已锁定");
//                }
                open(devRelated, name, idCard, type, card);
            } else {
                // 获取访客有无访问数据
                List<TbVisitor> staffs = findByBetweenTime(name, idCard, getDateTime());
                if (staffs.size() > 0) {
                    String card = "v_" + name + "_" + idCard;
//                    if (redisUtils.get(key) == null) {
//                        redisUtils.set("v_" + name + "_" + idCard, "locked");
//                        redisUtils.expire(key, 4);
//                        open(devRelated, name, idCard, type);
//                        return RetUtil.fail(ErrorCodeDef.CODE_NORMAL, "成功");
//                    } else {
//                        return RetUtil.fail(ErrorCodeDef.CODE_ERROR, "已锁定");
//                    }
                    open(devRelated, name, idCard, type, card);
                } else {
//                    logger.info("该访客访问时间过期，访问无效");
                    return false;
                }
            }


        } else {

            String card = "S" + idCard;
//            if (redisUtils.get(key) == null) {
//                redisUtils.set("s_" + name + "_" + idCard, "locked");
//                redisUtils.expire(key, 4);
//            Control24DeviceUtil.controlDevice(devRelated.getRelayIP(), 8080, devRelated.getRelayOUT(), null);
            open(devRelated, name, idCard, type, card);

            return true;
//            } else {
//                return RetUtil.fail(ErrorCodeDef.CODE_ERROR, "已锁定");
//            }
        }

        return false;
    }

    // 开门并记录通行
    private void open(TbDevicerelated devRelated, String name, String idCard, String type, String cardNo) throws Exception {


        Control24DeviceUtil.controlDevice(devRelated.getRelayIP(), 8080, devRelated.getRelayOUT(),
                null);
        saverecord(name, idCard, type, devRelated.getFaceIP(), devRelated.getRelayOUT(), cardNo);
    }

    /**
     * 保存 通行记录
     *
     * @param name       通行人员名称
     * @param idCard     通行人员身份证号码
     * @param personType 通行人员类型
     * @param faceIP     设备ip
     * @param OUT        继电器输出口
     */
    public void saverecord(String name, String idCard, String personType, String faceIP, String OUT, String card) {
        // TODO Auto-generated method stub
        TbDevice device = findByDeviceIp(faceIP);
        TbAccessrecord accessRecord = new TbAccessrecord();
        accessRecord.setOrgCode(findOrgId());
        accessRecord.setPospCode(findPospCode());
        accessRecord.setInOrOut(device.getFqTurnover());

        accessRecord.setScanDate(getDate());
        accessRecord.setScanTime(getTime());
        accessRecord.setOutNumber(OUT);
        accessRecord.setDeviceType("FACE");
        accessRecord.setDeviceIp(faceIP);
        accessRecord.setUserType(personType);
        accessRecord.setUserName(name);
        accessRecord.setIdCard(idCard);
        accessRecord.setCardNO(card);
        accessRecord.setIsSendFlag("F");
        saveAccessrecord(accessRecord);
    }

    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    private String getTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    private String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 根据 设备编号删除 设备
     *
     * @param deviceId 设备编号
     */
    public int deleteDevice(String deviceId) {
        return Db.delete("delete from tb_device where deviceId = ?", deviceId);
    }

    /**
     * 查询所有的设备
     */
    public List<TbDevice> findDevice() {
        return TbDevice.dao.find("select * from tb_device");
    }

    /**
     * 根据 楼层 获取设备的ip
     *
     * @param companyFloor 楼层
     * @return
     */
    public List<String> getAllFaceDeviceIP(String companyFloor) {
        List<String> allFaceIP = new ArrayList<String>();
        if (StringUtils.isEmpty(companyFloor) || "undefined".equals(companyFloor)) {
            List<TbDevice> list = TbDevice.dao.find("select * from tb_device");
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    String faceIp = ((TbDevice) list.get(i)).getDeviceIp();
                    TbDevice device = findByDeviceIp(faceIp);
                    if (StringUtils.isEmpty(device.getDeviceType())) {
                        allFaceIP.add(faceIp);
                    }

                }
            }

        } else {
            List<TbDevice> list = new ArrayList<TbDevice>();
            if (companyFloor.contains("|")) {
                String[] floors = companyFloor.split("\\|");
                for (String floor : floors) {
                    list = TbDevice.dao.find("select * from tb_device where contralFloor = ?", floor);

                    if (list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            String faceIp = ((TbDevice) list.get(i)).getDeviceIp();
                            // 排查重复 当设备包含大楼N次时，只取一次
                            TbDevice device = findByDeviceIp(faceIp);
                            if (StringUtils.isEmpty(device.getDeviceType())) {
                                if (!allFaceIP.contains(faceIp)) {
                                    allFaceIP.add(faceIp);
                                }
                            }

                        }
                    }
                }

            } else {

                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        String faceIp = ((TbDevice) list.get(i)).getDeviceIp();
                        TbDevice device = findByDeviceIp(faceIp);
                        if (StringUtils.isEmpty(device.getDeviceType())) {
                            allFaceIP.add(faceIp);
                        }
                    }
                }
            }
        }

        return allFaceIP;
    }

    /**
     * 根据下发标识 查询 通行记录
     *
     * @param isSendFlag 下发标识
     * @return
     */
    public List<TbAccessrecord> findByIsSendFlag(String isSendFlag) {
        return TbAccessrecord.dao.find("select * from tb_accessrecord where isSendFlag = ?", isSendFlag);
    }

    /**
     * 根据 开始id 和结束 id 修改通行记录
     *
     * @param startId 开始id
     * @param endId   结束 id
     */
    public void updateSendFlag(Integer startId, Integer endId) {
        Db.update("update tb_accessrecord set isSendFlag = 'T' where id between ? and ?", startId, endId);
    }

    /**
     * 根据 时间  身份证号码 ,姓名 查询 通行记录
     *
     * @param time   时间
     * @param idCard 身份证号码
     * @param name   姓名
     * @return
     */
    public List<TbAccessrecord> findAccessrecord(String time, String idCard, String name) {
        return TbAccessrecord.dao.find("select * from tb_accessrecord where scanTime = ? and idCard = ? and userName = ?", time, idCard, name);
    }

    /**
     * 条件查询
     */
    public List<TbDevice> findByType(String deviceMode) {
        return TbDevice.dao.find("select * from tb_device where deviceMode like '%" + deviceMode + "%'");

    }

    /**
     * 旷世设备登录  and 开启接收人脸实时结果
     */
    public void login(Map<String, String> map) {
        MinniSDK minniSDK = MinniSDK.INSTANCE;
        //设备登录所需参数
        //登录句柄
        int handle = minniSDK.BoxSDK_INVALID_DEVICE_HANDLE;
        BoxSDK_DeviceConfig deviceConfig = new BoxSDK_DeviceConfig();
        //设备登录 参数
        deviceConfig.ip= map.get(Constant.deviceIp).getBytes();                //设备登录ip
        deviceConfig.port = Integer.parseInt(map.get(Constant.devicePort));    //设备登录端口
        deviceConfig.username =  map.get(Constant.username).getBytes();        //设备登录用户名
        deviceConfig.password =  map.get(Constant.password).getBytes();        //设备登录密码
        Pointer configPointer = deviceConfig.getPointer();
        int isLogin = minniSDK.BoxSDK_login_device(configPointer, 5000, handle);
        if (isLogin == 0) {
            logger.error("sync login device(" + map.get(Constant.deviceIp) + "," + Constant.devicePort + ") success, device handle:" + handle);
            //开启接收人脸实时结果
            BoxSDK_FaceResultConfig config = new BoxSDK_FaceResultConfig();
            config.enable = isLogin;                                 // 接收人脸结果
            config.enable_feature = isLogin;                         // 接收人脸的特征
            config.enable_capture_image = isLogin;                   // 接收人脸抓拍的抓拍图
            config.enable_original_image = isLogin;                  // 接收人脸抓拍的全景图
            config.enable_recog_capture_image = isLogin;             // 接收人脸识别的底库图
            config.enable_recog_original_image = isLogin;            // 接收人脸识别的底库图
            config.enable_recog_library_image = isLogin;             // 接收人脸的底库图
            config.enable_fever_capture_image = isLogin;             // 接收人脸的发烧抓拍图
            config.enable_fever_original_image = isLogin;            // 接收人脸的发烧全景图
            config.enable_respirator_capture_image = isLogin;        // 接收人脸的无口罩抓拍图
            config.enable_respirator_original_image = isLogin;       // 接收人脸的无口罩全景图
            Pointer pointer = config.getPointer();
            int isSuccess = minniSDK.BoxSDK_set_face_result_config(handle, pointer, 5000);
            if(isSuccess==0){
                logger.info("mock device ( "+handle+" ) set_face_result_config success");
            }else{
                logger.error("mock device ( "+handle+" ) set_face_result_config failed, error code "+isSuccess);
                //退出设备
//            minniSDK.BoxSDK_logout_device(handle);
            }
        } else {
            logger.error("sync login device ( " + map.get(Constant.deviceIp) + "," + Constant.devicePort + " ) failed, error code:" + isLogin);
            //销毁sdk
            minniSDK.BoxSDK_release();
        }
    }

}
