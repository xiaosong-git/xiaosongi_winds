package com.xiaosong.config.quartz;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.xiaosong.common.device.DeviceService;
import com.xiaosong.common.floor.FloorService;
import com.xiaosong.common.personnel.PersonController;
import com.xiaosong.common.personnel.PersonService;
import com.xiaosong.common.server.ServerService;
import com.xiaosong.common.service.FailReceiveService;
import com.xiaosong.common.service.StaffService;
import com.xiaosong.config.InitHCNetSDK;
import com.xiaosong.config.SendAccessRecord;
import com.xiaosong.config.QRCodeModel.GetCompanyUserScheduleModel;
import com.xiaosong.config.devicesInit;
import com.xiaosong.constant.Constants;
import com.xiaosong.model.TbCompanyuser;
import com.xiaosong.model.TbDevice;
import com.xiaosong.model.TbFailreceive;
import com.xiaosong.model.TbServerinfo;
import com.xiaosong.util.Base64_2;
import com.xiaosong.util.FilesUtils;
import com.xiaosong.util.MD5Util;
import com.xiaosong.util.OkHttpUtil;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时拉取员工数据 并下发
 */
public class GetCompanyUserRecord implements Job {
    private FloorService srvFloor = FloorService.me;    //大楼业务层
    private StaffService srvStaff = StaffService.me;    //员工业务层
    private DeviceService srvDevice = DeviceService.me; //设备业务层
    private FailReceiveService srvFail = FailReceiveService.me; //下发失败业务层
    private ServerService srvServer = ServerService.me; //人员业务层
    private SendAccessRecord sendAccessRecord = new SendAccessRecord(); //下发的通行记录
    private PersonService personService = PersonService.me; //人员业务层
    private static Logger logger = Logger.getLogger(GetVisitorRecord.class); //日志
    private OkHttpUtil okHttpUtil = new OkHttpUtil();
    private Map<String, String> sendMap = new HashMap<>();
    private Cache cache = Redis.use("xiaosong"); //缓存

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // TODO Auto-generated method stub
        try {
            if (!srvFloor.findDeviceType().equals("1")) {
                getOrgInformation();
            } else {
                return;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取人员信息
     *
     * @throws Exception
     */
    public void getOrgInformation() throws Exception {

        if (srvFloor.findOrgId().isEmpty()) {
            logger.error("大楼编号不存在.");
            return;
        }
        logger.info("开始更新大楼员工数据..");

        Map<String, String> map = new HashMap<>();
        map.put("org_code", srvFloor.findOrgId());
        String keysign = srvFloor.findOrgId() + srvFloor.findPospCode() + srvFloor.findKey();
        String sign = MD5Util.MD5(keysign);
        map.put("sign", sign);
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(Constants.baseURl);
        TbServerinfo tbServerinfo = srvServer.findSer();
        stringBuilder.append("http://" + tbServerinfo.getServerUrl() + ":" + tbServerinfo.getServerPort() + "/service/");
        stringBuilder.append(Constants.pullOrgCompanyUrl);
        //stringBuilder.append(Constants.ceshi);
        String url = stringBuilder.toString();
        logger.info("获取员工数据地址：" + url);

        String responseContent = okHttpUtil.post(url, map);

        if (responseContent != null) {
            GetCompanyUserScheduleModel getStaffScheduleResponseParentModel = JSON.parseObject(responseContent,
                    GetCompanyUserScheduleModel.class);
            // System.out.println(getStaffScheduleResponseParentModel.toString());
            if (null != getStaffScheduleResponseParentModel.getData()) {
                List<TbCompanyuser> companyUserList = getStaffScheduleResponseParentModel.getData();
                logger.info("需要拉取数据" + companyUserList.size() + "条");
                if (companyUserList == null || companyUserList.size() == 0) {
                    logger.warn("无新员工数据!");
                } else {
                    for (TbCompanyuser companyUser : companyUserList) {
                        //	System.out.println(companyUser.toString());
                        companyUser.setIsDel("1");
                        companyUser.setIsSued("1");
                        TbCompanyuser userfind = srvStaff.findByNameAndIdNO(companyUser.getUserName(),
                                companyUser.getIdNO(), "normal");
                        if (userfind == null) {
                            notExitUser(companyUser);
                        } else {
                            doExitUser(userfind, companyUser);
                        }
                    }
                }
            }
        } else {
            logger.error(url);
            //logger.sendErrorLog(towerInforService.findOrgId(), "请求网址"+url+"获取的数据为空", "","网络错误", Constants.errorLogUrl,keysign);
        }

        // 查找今天之前下发错误的
        List<TbCompanyuser> companyUsers = srvStaff.findBeforeToDay(getDate());
        if (companyUsers.size() == 0 || companyUsers == null) {
            logger.info("未出现下发错误的名单");
        } else {
            for (TbCompanyuser user : companyUsers) {
                if (!user.getCurrentStatus().equals("normal")) {
                    continue;
                }
                String issued = "0";
                List<TbFailreceive> faceFails = srvFail.findByName(user.getUserName(), "save");
                if (faceFails.size() == 0 || faceFails == null) {
                    logger.error("失败记录表无" + user.getUserName() + "数据");
                    continue;
                }
                for (TbFailreceive faceReceive : faceFails) {
                    String photo = isPhoto(user);
                    if (photo == null) {
                        logger.error("缺失照片");
                        //logger.sendErrorLog(towerInforService.findOrgId(), user.getUserName()+"缺失照片", "","数据错误", Constants.errorLogUrl,keysign);
                        continue;
                    }
                    TbDevice device = srvDevice.findByDeviceIp(faceReceive.getFaceIp());
                    if (null == device) {
                        logger.error("设备表缺少IP为" + faceReceive.getFaceIp() + "的设备");
                        return;
                    }
                    boolean isSuccess = true;
                    if (device.getDeviceType().equals("TPS980")) {
                        sendMap.put(Constants.currentStatus, user.getCurrentStatus());
                        sendMap.put(Constants.userName, user.getUserName());
                        sendMap.put(Constants.idNo, user.getIdNO());
                        isSuccess = personService.sendFaceHJ(faceReceive.getFaceIp(), sendMap, photo);
                    } else if (device.getDeviceType().equals("DS-K5671")) {
                        //linux下 初始化 海康sdk
                        InitHCNetSDK.run(device.getDeviceType());
                        //winds下 初始化海康SDK
//                        devicesInit.initHC();
                        isSuccess = sendAccessRecord.setCardAndFace(faceReceive.getFaceIp(), user, null, "admin", "wgmhao123");

                    } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                        File picAppendData = IPCxmlFile(user);
                        String filePath = Constants.StaffPath + "/" + user.getPhoto() + ".jpg";
                        File picture = new File(filePath);
                        isSuccess = sendAccessRecord.sendToIPC(faceReceive.getFaceIp(), picture, picAppendData, user, null, "admin", "wgmhao123");
                    } else if (device.getDeviceType().equals("DH-ASI728")) {
                        //初始化大华 设备
                        devicesInit.initDH();
                        sendMap.put(Constants.deviceIp, faceReceive.getFaceIp());   //设备ip*
                        sendMap.put(Constants.username, "admin");   //设备用户名*
                        sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                        sendMap.put(Constants.photoPath, photo);      //图片*
                        sendMap.put(Constants.userId, String.valueOf(user.getUserId()));       //用户id
                        sendMap.put(Constants.userName, user.getUserName());   //用户姓名*
                        sendMap.put(Constants.idNo, user.getIdNO());           //身份证号码
                        personService.insertInfo(sendMap);

                    }
                    if (!isSuccess) {
                        issued = "1";
                        logger.error("失败名单下发" + user.getUserName() + "再次失败");
                        //logger.sendErrorLog(towerInforService.findOrgId(), "失败名单下发" + user.getUserName() + "再次失败", "人脸设备IP"+faceReceive.getFaceIp(),"下发错误", Constants.errorLogUrl,keysign);
                        int count = faceReceive.getDownNum() + 1;
                        faceReceive.setDownNum(count);

                    } else {
                        faceReceive.setReceiveFlag("0");
                    }
                    faceReceive.update();
                }
                user.setIsSued(issued);
                user.update();
            }
        }

        //处理未删除的员工数据
        List<TbCompanyuser> faliList = srvStaff.findFailDel();
        if (faliList.size() == 0 || faliList == null) {
            logger.info("未出现未删除的名单");
        } else {
            for (TbCompanyuser deluser : faliList) {
                String photo = isPhoto(deluser);
                if (null == photo) {
                    return;
                }
                String companyfloor = deluser.getCompanyFloor();
                List<String> allFaceDecive = srvDevice.getAllFaceDeviceIP(companyfloor);
                if (allFaceDecive.size() > 0) {
                    String isdel = "0";
                    for (int i = 0; i < allFaceDecive.size(); i++) {

                        TbDevice device = srvDevice.findByDeviceIp(allFaceDecive.get(i));
                        if (null == device) {
                            logger.error("设备表缺少IP为" + allFaceDecive.get(i) + "的设备");
                            continue;
                        }
                        boolean isSuccess = true;
                        if (device.getDeviceType().equals("TPS980")) {
                            sendMap.put(Constants.currentStatus, deluser.getCurrentStatus());
                            sendMap.put(Constants.userName, deluser.getUserName());
                            sendMap.put(Constants.idNo, deluser.getIdNO());
                            isSuccess = personService.sendFaceHJ(allFaceDecive.get(i), sendMap, photo);
                        } else if (device.getDeviceType().equals("DS-K5671")) {
                            if (deluser.getIsDel().equals("0")) {
                                logger.info(deluser.getUserName() + "已删除名单");
                                continue;
                            } else {
                                //linux下 初始化 海康sdk
                                InitHCNetSDK.run(device.getDeviceType());
                                //winds下 初始化海康SDK
//                              devicesInit.initHC();
                                isSuccess = sendAccessRecord.setCardAndFace(allFaceDecive.get(i), deluser, null, "admin", "wgmhao123");

                            }
                        } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                            if (null == deluser.getIdFrontImgUrl()) {
                                isSuccess = true;
                            } else {
                                isSuccess = sendAccessRecord.delIPCpicture("normal", deluser.getIdFrontImgUrl());
                            }

                        } else if (device.getDeviceType().equals("DH-ASI728")) {
                            devicesInit.initDH();
                            sendMap.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                            sendMap.put(Constants.username, "admin");   //设备用户名*
                            sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                            isSuccess = personService.deleteDH(sendMap, deluser.getCardInfo());

                        }

                        if (isSuccess) {
                            logger.info("设备IP" + allFaceDecive.get(i) + "删除" + deluser.getUserName() + "成功");
                        } else {
                            isdel = "1";
                            TbFailreceive faceReceive = srvFail.findOne(allFaceDecive.get(i), deluser.getUserName(), deluser.getIdNO(), "staff");
                            if (null == faceReceive) {

                                TbFailreceive newFaceFail = new TbFailreceive();
                                newFaceFail.setFaceIp(allFaceDecive.get(i));
                                newFaceFail.setIdCard(deluser.getIdNO());
                                newFaceFail.setUserName(deluser.getUserName());
                                newFaceFail.setReceiveFlag("1");
                                newFaceFail.setUserType("staff");
                                newFaceFail.setDownNum(0);
                                newFaceFail.setOpera("delete");
                                newFaceFail.setReceiveTime(getDateTime());
                                newFaceFail.save();
                            } else {
                                int count = faceReceive.getDownNum();
                                count = count + 1;
                                faceReceive.setDownNum(count);
                                faceReceive.update();
                            }
                        }
                    }
                    deluser.setIsDel(isdel);
                    deluser.update();
                }
            }
        }
    }

    /**
     * 不是在职 的员工 不接收
     *
     * @param companyUser 员工
     * @throws Exception
     */
    private void notExitUser(TbCompanyuser companyUser) throws Exception {

        // 无状态员工不接收
        if (!"normal".equals(companyUser.getCurrentStatus())) {
            logger.info("员工" + companyUser.getUserName() + "的状态是" + companyUser.getCurrentStatus() + ",上位机不接收");
            return;
        }
        String keysign = srvFloor.findOrgId() + srvFloor.findPospCode() + srvFloor.findKey();
        TbCompanyuser userfind = srvStaff.findByNameAndIdNO(companyUser.getUserName(),
                companyUser.getIdNO(), "离职");

        if (null != userfind) {
            srvStaff.deleteOne(userfind);
        }
        companyUser.setReceiveDate(getDate());
        companyUser.setReceiveTime(getTime());
        companyUser.save();

        if (companyUser.getPhoto() != null) {
            cache.set("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO(), companyUser.getPhoto());
            byte[] photoKey = Base64_2.decode(companyUser.getPhoto());
            String fileName = companyUser.getUserName() + companyUser.getCompanyId() + ".jpg";
            FilesUtils.getFileFromBytes(photoKey, Constants.StaffPath, fileName);
            //FilesUtils.getFileFromBytes(photoKey, "E:\\sts-space\\photoCache\\staff", fileName);
        } else {
            logger.error((companyUser.getUserName() + "该用户无照片"));
            //logger.sendErrorLog(towerInforService.findOrgId(), companyUser.getUserName() + "该用户无照片", "","数据错误", Constants.errorLogUrl,keysign);
            return;
        }

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }

        List<String> allFaceDecive = srvDevice.getAllFaceDeviceIP(companyfloor);
        System.out.println(allFaceDecive.size());
        if (allFaceDecive.size() > 0) {
            String issued = "0";
            for (int i = 0; i < allFaceDecive.size(); i++) {
                logger.info("需下发的人像识别仪器IP为：" + allFaceDecive.get(i));

                TbDevice device = srvDevice.findByDeviceIp(allFaceDecive.get(i));
                if (null == device) {
                    logger.error("设备表缺少IP为" + allFaceDecive.get(i) + "的设备");
                    continue;
                }
                String photo = isPhoto(companyUser);
                boolean isSuccess = true;
                if (device.getDeviceType().equals("TPS980")) {
                    sendMap.put(Constants.currentStatus, companyUser.getCurrentStatus());
                    sendMap.put(Constants.userName, companyUser.getUserName());
                    sendMap.put(Constants.idNo, companyUser.getIdNO());
                    isSuccess = personService.sendFaceHJ((String) allFaceDecive.get(i), sendMap,
                            companyUser.getPhoto());
                } else if (device.getDeviceType().equals("DS-K5671")) {
                    //linux下 初始化 海康sdk
                    InitHCNetSDK.run(device.getDeviceType());
                    //winds下 初始化海康SDK
//                  devicesInit.initHC();
                    isSuccess = sendAccessRecord.setCardAndFace(allFaceDecive.get(i), companyUser, null, "admin", "wgmhao123");
                } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                    File picAppendData = IPCxmlFile(companyUser);
                    String filePath = Constants.StaffPath + "/" + companyUser.getPhoto() + ".jpg";
                    File picture = new File(filePath);
                    isSuccess = sendAccessRecord.sendToIPC((String) allFaceDecive.get(i), picture, picAppendData, companyUser, null, "amdin", "wgmhao123");
                } else if (device.getDeviceType().equals("DH-ASI728")) {
                    devicesInit.initDH();
                    sendMap.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                    sendMap.put(Constants.username, "admin");   //设备用户名*
                    sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                    sendMap.put(Constants.photoPath, photo);      //图片*
                    sendMap.put(Constants.userId, String.valueOf(companyUser.getUserId()));       //用户id
                    sendMap.put(Constants.userName, companyUser.getUserName());   //用户姓名*
                    sendMap.put(Constants.idNo, companyUser.getIdNO());           //身份证号码
                    personService.insertInfo(sendMap);
                }
                // 针对下发失败的需要登记，待下次冲洗下发，已经下发成功的不在下发
                if (isSuccess == false) {
                    issued = "1";
                    TbFailreceive faceReceive = srvFail.findOne(allFaceDecive.get(i), companyUser.getUserName(), companyUser.getIdNO(), "staff");
                    if (null == faceReceive) {
                        PersonController personController = new PersonController();
                        TbFailreceive newFaceFail = personController.getModel(TbFailreceive.class);
                        newFaceFail.setFaceIp(allFaceDecive.get(i));
                        newFaceFail.setIdCard(companyUser.getIdNO());
                        newFaceFail.setUserName(companyUser.getUserName());
                        newFaceFail.setReceiveFlag("1");
                        newFaceFail.setUserType("staff");
                        newFaceFail.setDownNum(0);
                        newFaceFail.setOpera("save");
                        newFaceFail.setReceiveTime(getDateTime());
                        newFaceFail.save();
                    } else {
                        int count = faceReceive.getDownNum();
                        count = count + 1;
                        System.out.println(count);
                        faceReceive.setDownNum(count);
                        faceReceive.save();
                    }
                    logger.info("失败表记录" + companyUser.getUserName() + "数据");
                }
            }
            companyUser.setIsSued(issued);
            companyUser.update();
        }
    }

    private void doExitUser(TbCompanyuser companyUser, TbCompanyuser newUser) throws Exception {

        if (newUser.getCurrentStatus().equals("normal")) {
            if (companyUser.getIsSued().equals("0")) {
                return;
            } else {
                List<TbFailreceive> faceReceiveList = srvFail.findByName(companyUser.getUserName(), "save");
                if (faceReceiveList.size() <= 0) {

                    return;
                } else {

                    String issued = "0";
                    for (TbFailreceive faceReceive : faceReceiveList) {
                        TbCompanyuser user = srvStaff.findByNameAndIdNO(faceReceive.getUserName(),
                                faceReceive.getIdCard(), "normal");

                        String photo = isPhoto(user);
                        if (photo == null) {
                            logger.error("缺失照片");
                            //logger.sendErrorLog(towerInforService.findOrgId(),user.getUserName()+ "缺失照片", "","数据错误", Constants.errorLogUrl,keysign);
                            continue;
                        }
                        TbDevice device = srvDevice.findByDeviceIp(faceReceive.getFaceIp());
                        if (null == device) {
                            logger.error("设备表缺少IP为" + faceReceive.getFaceIp() + "的设备");
                            continue;
                        }
                        boolean isSuccess = true;
                        if (device.getDeviceType().equals("TPS980")) {
                            sendMap.put(Constants.currentStatus, companyUser.getCurrentStatus());
                            sendMap.put(Constants.userName, companyUser.getUserName());
                            sendMap.put(Constants.idNo, companyUser.getIdNO());
                            isSuccess = personService.sendFaceHJ(faceReceive.getFaceIp(), sendMap,
                                    photo);
                        } else if (device.getDeviceType().equals("DS-K5671")) {
                            //linux下 初始化 海康sdk
                            InitHCNetSDK.run(device.getDeviceType());
                            //winds下 初始化海康SDK
//                          devicesInit.initHC();
                            isSuccess = sendAccessRecord.setCardAndFace(faceReceive.getFaceIp(), user, null, "admin", "wgmhao123");

                        } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                            File picAppendData = IPCxmlFile(user);
                            String filePath = Constants.StaffPath + "/" + user.getUserName() + user.getCompanyId()
                                    + ".jpg";
                            File picture = new File(filePath);
                            isSuccess = sendAccessRecord.sendToIPC(faceReceive.getFaceIp(), picture, picAppendData, user, null, "admin", "wgmhao123");
                        } else if (device.getDeviceType().equals("DH-ASI728")) {
                            devicesInit.initDH();
                            sendMap.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                            sendMap.put(Constants.username, "admin");   //设备用户名*
                            sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                            sendMap.put(Constants.photoPath, photo);      //图片*
                            sendMap.put(Constants.userId, String.valueOf(companyUser.getUserId()));       //用户id
                            sendMap.put(Constants.userName, companyUser.getUserName());   //用户姓名*
                            sendMap.put(Constants.idNo, companyUser.getIdNO());           //身份证号码
                            personService.insertInfo(sendMap);

                        }
                        if (isSuccess == false) {
                            issued = "1";
                            logger.error("失败名单下发" + user.getUserName() + "再次失败");
                            //logger.sendErrorLog(towerInforService.findOrgId(), "失败名单下发" + user.getUserName() + "再次失败", "人脸设备IP"+faceReceive.getFaceIp(),"设备接收错误", Constants.errorLogUrl,keysign);
                            int count = faceReceive.getDownNum() + 1;
                            faceReceive.setDownNum(count);
                        } else {
                            faceReceive.setReceiveFlag("0");
                        }
                        faceReceive.update();
                    }
                    companyUser.setIsSued(issued);
                    companyUser.update();
                }
                return;
            }
        } else if (newUser.getCurrentStatus().equals("离职")) {
            if (companyUser.getIsSued().equals("1")) {
                return;
            }
            String photo = newUser.getPhoto();
            if (null == photo) {
                photo = isPhoto(companyUser);
            }
            String companyfloor = companyUser.getCompanyFloor();
            List<String> allFaceDecive = srvDevice.getAllFaceDeviceIP(companyfloor);
            if (allFaceDecive.size() > 0) {
                String isdel = "0";
                for (int i = 0; i < allFaceDecive.size(); i++) {
                    TbDevice device = srvDevice.findByDeviceIp(allFaceDecive.get(i));
                    if (null == device) {
                        logger.error("设备表缺少IP为" + allFaceDecive.get(i) + "的设备");
                        continue;
                    }
                    boolean isSuccess = true;
                    if (device.getDeviceType().equals("TPS980")) {
                        srvStaff.sendDelWhiteList(allFaceDecive.get(i), companyUser.getUserName(), companyUser.getIdNO());
                    } else if (device.getDeviceType().equals("DS-K5671")) {
                        //linux下 初始化 海康sdk
                        InitHCNetSDK.run(device.getDeviceType());
                        //winds下 初始化海康SDK
//                          devicesInit.initHC();
                        String cardStr = "S" + companyUser.getUserId();
                        isSuccess = sendAccessRecord.delFace(allFaceDecive.get(i), cardStr, "admin", "wgmhao123");
                        if (isSuccess) {
                            sendAccessRecord.delCard(allFaceDecive.get(i), companyUser, null, "admin", "wgmhao123");

                        }
                    } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                        if (null == companyUser.getIdFrontImgUrl()) {
                            isSuccess = true;
                        } else {
                            //linux 下 加载 海康sdk
                            InitHCNetSDK.run(device.getDeviceType());
                            // winds 下海康设备就初始化海康SDK
//                            devicesInit.initHC();
                            isSuccess = sendAccessRecord.delIPCpicture("staff", companyUser.getIdFrontImgUrl());
                        }
                    } else if (device.getDeviceType().equals("DH-ASI728")) {
                        devicesInit.initDH();
                        //初始化大华设备
                        devicesInit.initDH();
                        sendMap.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                        sendMap.put(Constants.username, "admin");   //设备用户名*
                        sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                        personService.deleteDH(sendMap,companyUser.getCardInfo());

                    }
                    if (isSuccess) {
                        logger.info("设备IP" + allFaceDecive.get(i) + "删除" + companyUser.getUserName() + "成功");
                    } else {
                        isdel = "1";
                        TbFailreceive faceReceive = srvFail.findOne(allFaceDecive.get(i), companyUser.getUserName(), companyUser.getIdNO(), "staff");
                        if (null == faceReceive) {
                            PersonController personController = new PersonController();
                            TbFailreceive newFaceFail = personController.getModel(TbFailreceive.class);
                            newFaceFail.setFaceIp(allFaceDecive.get(i));
                            newFaceFail.setIdCard(companyUser.getIdNO());
                            newFaceFail.setUserName(companyUser.getUserName());
                            newFaceFail.setReceiveFlag("1");
                            newFaceFail.setUserType("staff");
                            newFaceFail.setDownNum(0);
                            newFaceFail.setOpera("delete");
                            newFaceFail.setReceiveTime(getDateTime());
                            newFaceFail.save();
                        } else {
                            int count = faceReceive.getDownNum();
                            count = count + 1;
                            faceReceive.setDownNum(count);
                            faceReceive.save();
                        }
                    }
                }
                companyUser.setIsDel(isdel);
                companyUser.setCurrentStatus("离职");
                companyUser.update();
            }
        } else

        {

        }

    }

    /**
     * 照片
     *
     * @param companyUser 用户信息
     * @return
     * @throws Exception
     */
    private String isPhoto(TbCompanyuser companyUser) throws Exception {
        String photo = cache.get("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO());
        if (photo == null) {
            String filePath = Constants.StaffPath + companyUser.getPhoto() + ".jpg";

            //String filePath = "E:\\sts-space\\photoCache\\staff\\" + companyUser.getUserName()+ companyUser.getCompanyId() + ".jpg";
            File file = new File(filePath);
            if (!file.exists()) {
                logger.error(companyUser.getUserName() + "无照片");
                //logger.sendErrorLog(towerInforService.findOrgId(), companyUser.getUserName() + "无照片", "","数据错误", Constants.errorLogUrl,keysign);
                return null;
            } else {
                photo = Base64_2.encode(FilesUtils.getBytesFromFile(file));
                cache.set("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO(), photo);
            }
        }
        return photo;
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

    private String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 下发IPC人像时所需照片附加信息文件
     *
     * @param user
     * @return
     */
    public File IPCxmlFile(TbCompanyuser user) {
        // TODO Auto-generated method stub
        String filePath = Constants.StaffPath + "/" + user.getPhoto() + ".xml";
        File filepath = new File(Constants.StaffPath);
        if (!filepath.exists()) {
            filepath.mkdirs();
        }
        File file = new File(filePath);

        StringBuilder builder = new StringBuilder();
        builder.append("<FaceAppendData><name>S");
        builder.append(user.getUserName());
        builder.append("</name><certificateType>ID</certificateType><certificateNumber>");
        builder.append(user.getUserId());
        builder.append("</certificateNumber></FaceAppendData>");

        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8");
            StringBuilder outputString = new StringBuilder();
            outputString.append(builder.toString());
            out.write(outputString.toString());

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return file;
    }
}
