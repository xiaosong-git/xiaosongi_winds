package com.xiaosong.config.quartz;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dhnetsdk.lib.FileUtils;
import com.dhnetsdk.lib.NetSDKLib;
import com.dhnetsdk.lib.NetSDKLib.*;
import com.dhnetsdk.lib.ToolKits;
import com.dhnetsdk.module.LoginModule;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.sun.jna.Memory;
import com.xiaosong.common.device.DeviceService;
import com.xiaosong.common.floor.FloorService;
import com.xiaosong.common.personnel.PersonController;
import com.xiaosong.common.server.ServerService;
import com.xiaosong.common.service.FailReceiveService;
import com.xiaosong.common.service.StaffService;
import com.xiaosong.common.service.VisitorService;
import com.xiaosong.config.InitHCNetSDK;
import com.xiaosong.config.SendAccessRecord;
import com.xiaosong.config.QRCodeModel.GetStaffScheduleDataResponseModel;
import com.xiaosong.config.QRCodeModel.GetStaffScheduleResponseParentModel;
import com.xiaosong.config.QRCodeModel.GetStaffScheduleVisitorResponseModel;
import com.xiaosong.config.devicesInit;
import com.xiaosong.constant.Constants;
import com.xiaosong.constant.FaceDevResponse;
import com.xiaosong.model.*;
import com.xiaosong.util.*;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定时拉取访客数据
 */
public class GetVisitorRecord implements Job {

    private FloorService srvFloor = FloorService.me;   //大楼业务层
    private VisitorService srvVisitor = VisitorService.me;  //访客业务层
    private DeviceService srvDevice = DeviceService.me; //设备业务层
    private ServerService srvServer = ServerService.me; //服务器业务层
    private FailReceiveService srvFail = FailReceiveService.me; //下发失败业务层
    private StaffService srvStaff = StaffService.me; //员工业务层
    private SendAccessRecord sendAccessRecord = new SendAccessRecord();
    private static Logger logger = Logger.getLogger(GetVisitorRecord.class);
    private OkHttpUtil okHttpUtil = new OkHttpUtil();
    private Cache cache = Redis.use("xiaosong");
    private Map<String, String> sendMap = new HashMap<>();


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            if (!"1".equals(srvFloor.findDeviceType())) {
                getStaff();
            } else {
                return;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getStaff() throws Exception {
        if (!srvFloor.findByOrgCode().isEmpty()) {
            // System.out.println("开始拉取数据");
            String towerNumber = srvFloor.findByOrgCode();
            StringBuilder stringBuilder = new StringBuilder();
            // 拉取的数据地址
            //stringBuilder.append(Constants.baseURl);
            TbServerinfo ser = srvServer.findSer();
            stringBuilder.append("http://" + ser.getServerUrl() + ":" + ser.getServerPort() + "/service/");

            stringBuilder.append(Constants.newpullStaffUrl);
            stringBuilder.append("/");
            stringBuilder.append(srvFloor.findPospCode());
            stringBuilder.append("/");
            stringBuilder.append(towerNumber.trim());
            stringBuilder.append("/");
            stringBuilder.append(Constants.page);
            stringBuilder.append("/");
            stringBuilder.append(Constants.PAGENUMBER);

            String url = stringBuilder.toString();
            logger.info("拉取地址：" + url);

            String responseContent = okHttpUtil.get(url);

            if (responseContent.isEmpty()) {
                logger.error("没获取到访问数据");
//                String keysign = towerInforService.findOrgId()+towerInforService.findPospCode()+towerInforService.findKey();
                //logger.sendErrorLog(towerInforService.findOrgId(), "没获取到访客访问的数据", "","数据错误", Constants.errorLogUrl,keysign);
                return;
            }

            // 返回数据转成json模式
            GetStaffScheduleResponseParentModel getStaffScheduleResponseParentModel = JSON.parseObject(responseContent,
                    GetStaffScheduleResponseParentModel.class);
            if (getStaffScheduleResponseParentModel == null) {
                logger.error("返回数据格式不正确");
                //String keysign = towerInforService.findOrgId()+towerInforService.findPospCode()+towerInforService.findKey();
                //logger.sendErrorLog(towerInforService.findOrgId(), "访客访问的返回数据格式不正确", "","数据错误", Constants.errorLogUrl,keysign);
                return;
            }

            // 获取其数据
            GetStaffScheduleDataResponseModel getStaffScheduleDataResponseModel = getStaffScheduleResponseParentModel
                    .getData();

            // 数据分析
            List<GetStaffScheduleVisitorResponseModel> getStaffScheduleVisitorResponseModels = getStaffScheduleDataResponseModel
                    .getRows();
            List<TbVisitor> staffList = srvVisitor.findByIssued("1");

            if ((getStaffScheduleVisitorResponseModels == null)
                    || (getStaffScheduleVisitorResponseModels.size() <= 0)) {
                logger.warn("无访问数据内容");
            } else {
                for (GetStaffScheduleVisitorResponseModel getStaffScheduleVisitorResponseModel : getStaffScheduleVisitorResponseModels) {
                    TbVisitor tbStaff = new TbVisitor();
                    tbStaff.setCity(getStaffScheduleVisitorResponseModel.getCity());
                    tbStaff.setEndDateTime(getStaffScheduleVisitorResponseModel.getEndDate().substring(0, 16));
                    tbStaff.setOrgCode(getStaffScheduleVisitorResponseModel.getOrgCode());
                    tbStaff.setProvince(getStaffScheduleVisitorResponseModel.getProvince());
                    tbStaff.setSoleCode(getStaffScheduleVisitorResponseModel.getSoleCode());
                    tbStaff.setStartDateTime(getStaffScheduleVisitorResponseModel.getStartDate().substring(0, 16));
                    //访问时间提前半小时有效
                    String preTime = preStartTime(getStaffScheduleVisitorResponseModel.getStartDate(), -30);
                    tbStaff.setPreStartTime(preTime);
                    tbStaff.setVisitorName(getStaffScheduleVisitorResponseModel.getUserRealName());
                    tbStaff.setVisitDate(getStaffScheduleVisitorResponseModel.getVisitDate());
                    tbStaff.setVisitTime(getStaffScheduleVisitorResponseModel.getVisitTime());
                    tbStaff.setByVisitorName(getStaffScheduleVisitorResponseModel.getVistorRealName());
//                    tbStaff.setDatetype(getStaffScheduleVisitorResponseModel.getDateType());
                    tbStaff.setUserId(getStaffScheduleVisitorResponseModel.getUserId());
                    tbStaff.setDelFlag("1");
                    tbStaff.setDelPosted("1");
                    if ("二维码通行".equals(srvFloor.findVisitorCheckType()) || getStaffScheduleVisitorResponseModel.getUserIdNO() == null) {
                        tbStaff.setIsSued("0");
                    } else {
                        tbStaff.setIsSued("1");
                    }
                    tbStaff.setIsPosted("0");
                    tbStaff.setVisitId(getStaffScheduleVisitorResponseModel.getVisitId());
                    tbStaff.setVisitorIdCard(getStaffScheduleVisitorResponseModel.getUserIdNO());
                    tbStaff.setPhoto(getStaffScheduleVisitorResponseModel.getPhoto());
                    tbStaff.setByVisitorIdCard(getStaffScheduleVisitorResponseModel.getVisitorIdNO());
                    String uIdStaffId = UUID.randomUUID().toString().replaceAll("\\-", "");
                    tbStaff.setId(uIdStaffId);

                    // 存数据
                    srvVisitor.save(tbStaff);
                    // 向云端确认存储
                    confirmReceiveData(towerNumber, tbStaff.getVisitId());
                    if ("二维码通行".equals(srvFloor.findVisitorCheckType()) || tbStaff.getByVisitorIdCard() == null) {

                        return;
                    } else {
                        if (getStaffScheduleVisitorResponseModel.getPhoto() != null) {

                            cache.set("photo_" + uIdStaffId, getStaffScheduleVisitorResponseModel.getPhoto());

                            byte[] photoKey = Base64_2.decode(getStaffScheduleVisitorResponseModel.getPhoto());
                            String fileName = getStaffScheduleVisitorResponseModel.getUserRealName()
                                    + getStaffScheduleVisitorResponseModel.getVisitId() + ".jpg";
                            FilesUtils.getFileFromBytes(photoKey, Constants.VisitorPath, fileName);

                            //FilesUtils.getFileFromBytes(photoKey, "E:\\sts-space\\photoCache\\service", fileName);
                        } else {
                            logger.error(getStaffScheduleVisitorResponseModel.getUserRealName() + "该用户无照片");
                            continue;
                        }
                        staffList.add(tbStaff);
                    }
                }
            }
            if (staffList.size() == 0 || staffList == null) {
                logger.info("无访客下发数据");
                return;
            }
            sendFaceData(staffList);
        }

    }

    private void confirmReceiveData(String towerNumber, String visitId) throws Exception {

        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(Constants.baseURl);
        TbServerinfo tbServerinfo = srvServer.findSer();
        stringBuilder.append("http://" + tbServerinfo.getServerUrl() + ":" + tbServerinfo.getServerPort() + "/service/");

        stringBuilder.append(Constants.newconfirmReceiveUrl);
        stringBuilder.append("/");
        stringBuilder.append(srvFloor.findPospCode());
        stringBuilder.append("/");
        stringBuilder.append(towerNumber);
        stringBuilder.append("/");
        stringBuilder.append(visitId);
        String url = stringBuilder.toString();
        String responseContent = okHttpUtil.get(url);
        logger.info(responseContent);
    }

    private void sendFaceData(List<TbVisitor> visitors) throws Exception {
        // TODO Auto-generated method stub

        // 访客下发名单
        for (TbVisitor visitor : visitors) {
            // 该访客是否还有员工身份
            TbCompanyuser companyUser = srvStaff.findByNameAndIdNO(visitor.getVisitorName(), visitor.getByVisitorIdCard(), "在职");
            if (null == companyUser) {
                logger.info("正常下发访客");
                // 正常下发
                if (visitor.getIsPosted().equals("1")) {
                    //失败数据再下发
                    doPosted(visitor);
                } else {
                    //新数据下发
                    doFirstPost(visitor);
                }
            } else {
                // 访客通过被访者所在楼层将访客信息下发到指定设备
                TbCompanyuser interviewee = srvStaff.findByNameAndIdNO(visitor.getByVisitorName(), visitor.getByVisitorIdCard(), "在职");
                if (null == interviewee) {
                    continue;
                }
                String floor = interviewee.getCompanyFloor();
                // 被访者相关联设备,即访客需要下发的设备
                List<String> allinterDecive = srvDevice.getAllFaceDeviceIP(floor);
                // 访客的员工身份相关联设备
                List<String> allStaffDecive = srvDevice.getAllFaceDeviceIP(companyUser.getCompanyFloor());
                for (int i = 0; i < allinterDecive.size(); i++) {
                    //访客员工身份的相关联设备是否与访问楼层相关联的设备重叠
                    if (allStaffDecive.contains(allinterDecive.get(i))) {
                        if (companyUser.getIsSued().equals("1")) {
                            //失败记录表查找该员工下发失败的设备
                            List<TbFailreceive> failRecord = srvFail.findByNameAndid(companyUser.getUserName(), companyUser.getIdNO(), "在职");
                            if (null == failRecord) {
                                //
                                logger.info("失败记录表无访客的员工身份信息");
                                continue;
                            } else {
                                for (TbFailreceive faileData : failRecord) {
                                    if (faileData.getFaceIp().equals(allinterDecive.get(i))) {
                                        //指定员工下发指定设备
                                        logger.info(allinterDecive.get(i) + "接收访客信息");
                                        sendPointDevice(visitor, allinterDecive.get(i));
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        } else {
                            visitor.setIsSued("0");
                            visitor.setIsPosted("1");
                            srvFloor.updateFloor(visitor);
                            continue;
                        }
                    } else {
                        //指定员工下发指定设备
                        sendPointDevice(visitor, allinterDecive.get(i));
                    }
                }

            }

        }

    }

    //指定一台设备下发
    private void sendPointDevice(TbVisitor vistor, String deviceIp) throws Exception {
        String idNO = vistor.getByVisitorIdCard();
        String visitorname = vistor.getByVisitorName();

        if (idNO == null || visitorname == null) {
            logger.warn("被访人证件号或者姓名为空，找不到该员工数据");
            return;
        } else {
            TbCompanyuser companyUser = srvStaff.findByNameAndIdNO(visitorname, idNO, "在职");

            if (null == companyUser) {
                return;
            }

            String photo = isPhoto(vistor);
            if (photo == null) {
                return;
            }
            if (null != deviceIp) {
                logger.info("需下发的人像识别仪器IP为：" + deviceIp);

                TbDevice device = srvDevice.findByDeviceIp(deviceIp);
                if (null == device) {
                    logger.error("设备表缺少IP为" + deviceIp + "的设备");
                    return;
                }
                boolean isSuccess = true;
                if (device.getDeviceType().equals("TPS980")) {
                    isSuccess = this.sendWhiteList(deviceIp, vistor, photo);
                } else if (device.getDeviceType().equals("DS-K5671")) {
                    //linux 下 加载 海康sdk
                    InitHCNetSDK.run(device.getDeviceType());
                    // winds 下海康设备就初始化海康SDK
//                  devicesInit.initHC();
                    isSuccess = sendAccessRecord.setCardAndFace(deviceIp, null, vistor, "admin", "wgmhao123");

                } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                    File picAppendData = IPCxmlFile(vistor);
                    String filePath = Constants.VisitorPath + "/" + vistor.getVisitorName() + vistor.getVisitId() + ".jpg";
                    File picture = new File(filePath);
                    isSuccess = sendAccessRecord.sendToIPC(deviceIp, picture, picAppendData, null, vistor, "admin", "wgmhao123");
                } else if (device.getDeviceType().equals("DH-ASI728")) {
                    //winds下 初始化 sdk
                    devicesInit.initDH();
                    sendMap.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                    sendMap.put(Constants.username, "admin");   //设备用户名*
                    sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                    sendMap.put(Constants.photoPath, photo);      //图片*
                    sendMap.put(Constants.userId, String.valueOf(vistor.getUserId()));       //用户id
                    sendMap.put(Constants.userName, vistor.getVisitorName());   //用户姓名*
                    sendMap.put(Constants.idNo, vistor.getVisitorIdCard());     //身份证号码
                    isSuccess = insertInfo(sendMap);
                }
                logger.info("下发数据结果" + isSuccess);
                if (vistor.getIsPosted().equals("0")) {
                    String issued = "0";
                    if (isSuccess == false) {
                        issued = "1";
                        TbFailreceive faceReceive = srvFail.findOne(deviceIp, vistor.getVisitorName(), vistor.getVisitorIdCard(), "service");
                        if (null == faceReceive) {
                            TbFailreceive newFaceFail = new TbFailreceive();
                            newFaceFail.setFaceIp(deviceIp);
                            newFaceFail.setIdCard(vistor.getVisitorIdCard());
                            newFaceFail.setUserName(vistor.getVisitorName());
                            newFaceFail.setReceiveFlag("1");
                            newFaceFail.setUserType("service");
                            newFaceFail.setDownNum(0);
                            newFaceFail.setOpera("save");
                            newFaceFail.setReceiveTime(getDateTime());
                            newFaceFail.setVisitorUUID(vistor.getId());
                            newFaceFail.save();
                        } else {
                            int count = faceReceive.getDownNum();
                            count++;
                            faceReceive.setDownNum(count);
                            faceReceive.save();
                        }
                        faceReceive.save();
                    }

                    vistor.setIsSued(issued);
                    vistor.setIsPosted("1");
                    vistor.update();
                } else {
                    String issued = "0";
                    TbFailreceive facefail = srvFail.findByVisitorUUId(vistor.getId());
                    if (isSuccess == false) {
                        issued = "1";
                    } else {
                        issued = "0";
                        facefail.setReceiveFlag("0");
                        facefail.update();
                    }
                    vistor.setIsSued(issued);
                    vistor.setIsPosted("1");
                    vistor.update();
                }

            }
        }
    }

    /**
     * 新数据下发
     *
     * @param visitor 访客数据
     * @throws Exception
     */
    private void doFirstPost(TbVisitor visitor) throws Exception {

        String idNO = visitor.getByVisitorIdCard();
        String visitorname = visitor.getByVisitorName();

        if (idNO == null || visitorname == null) {
            logger.warn("被访人证件号或者姓名为空，找不到该员工数据");
            return;
        } else {
            TbCompanyuser companyUser = srvStaff.findByNameAndIdNO(visitorname, idNO, "在职");

            if (null == companyUser) {
                return;
            }

            String companyfloor = null;
            if (null != companyUser.getCompanyFloor()) {
                companyfloor = companyUser.getCompanyFloor();
            }
            List<String> allFaceDecive = srvDevice.getAllFaceDeviceIP(companyfloor);
            String photo = isPhoto(visitor);
            if (photo == null) {
                return;
            }
            if (allFaceDecive.size() > 0) {
                String issued = "0";
                for (int i = 0; i < allFaceDecive.size(); i++) {
                    logger.info("需下发的人像识别仪器IP为：" + allFaceDecive.get(i));
                    TbDevice device = srvDevice.findByDeviceIp(allFaceDecive.get(i));
                    if (null == device) {
                        logger.error("设备表缺少IP为" + allFaceDecive.get(i) + "的设备");
                        continue;
                    }
                    boolean isSuccess = true;
                    if (device.getDeviceType().equals("TPS980")) {
                        isSuccess = this.sendWhiteList((String) allFaceDecive.get(i), visitor, photo);
                    } else if (device.getDeviceType().equals("DS-K5671")) {
                        //linux 下 加载 海康sdk
                        InitHCNetSDK.run(device.getDeviceType());
                        // winds 下海康设备就初始化海康SDK
//                      devicesInit.initHC();
                        isSuccess = sendAccessRecord.setCardAndFace(allFaceDecive.get(i), null, visitor, "admin", "wgmhao123");

                    } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                        File picAppendData = IPCxmlFile(visitor);
                        String filePath = Constants.VisitorPath + "/" + visitor.getVisitorName() + visitor.getVisitId() + ".jpg";
                        File picture = new File(filePath);
                        isSuccess = sendAccessRecord.sendToIPC((String) allFaceDecive.get(i), picture, picAppendData, null, visitor, "admin", "wgmhao123");
                    } else if (device.getDeviceType().equals("DH-ASI728")) {
                        devicesInit.initDH();
                        sendMap.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                        sendMap.put(Constants.username, "admin");   //设备用户名*
                        sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                        sendMap.put(Constants.photoPath, photo);      //图片*
                        sendMap.put(Constants.userId, String.valueOf(visitor.getUserId()));       //用户id
                        sendMap.put(Constants.userName, visitor.getVisitorName());   //用户姓名*
                        sendMap.put(Constants.idNo, visitor.getVisitorIdCard());     //身份证号码
                        isSuccess = insertInfo(sendMap);

                    }
                    visitor.setIsPosted("1");
                    logger.info("下发数据结果" + isSuccess);
                    if (isSuccess == false) {
                        issued = "1";
                        TbFailreceive faceReceive = srvFail.findOne(allFaceDecive.get(i), visitor.getVisitorName(), visitor.getVisitorIdCard(), "service");
                        if (null == faceReceive) {
                            PersonController personController = new PersonController();
                            TbFailreceive newFaceFail = personController.getModel(TbFailreceive.class);
                            newFaceFail.setFaceIp(allFaceDecive.get(i));
                            newFaceFail.setIdCard(visitor.getVisitorIdCard());
                            newFaceFail.setUserName(visitor.getVisitorName());
                            newFaceFail.setReceiveFlag("1");
                            newFaceFail.setUserType("service");
                            newFaceFail.setDownNum(0);
                            newFaceFail.setOpera("save");
                            newFaceFail.setReceiveTime(getDateTime());
                            newFaceFail.setVisitorUUID(visitor.getId());
                            newFaceFail.save();
                        } else {
                            int count = faceReceive.getDownNum();
                            count++;
                            faceReceive.setDownNum(count);
                            faceReceive.save();
                        }
                    }
                    continue;
                }
                visitor.setIsSued(issued);
                visitor.update();
            }
            return;
        }

    }

    /**
     * 失败数据再下发
     *
     * @param vistor 访客数据
     * @throws Exception
     */
    private void doPosted(TbVisitor vistor) throws Exception {

        if (vistor.getIsSued().equals("0")) {
            return;
        } else {
            List<TbFailreceive> faceReceiveList = srvFail.findByFaceFlag("1", "service");
            if (faceReceiveList.size() <= 0) {
                return;
            } else {
                String issued = "0";
                for (TbFailreceive faceReceive : faceReceiveList) {

                    TbVisitor tbStaff = srvVisitor.findByUUID(faceReceive.getVisitorUUID());
                    if (null == tbStaff) {
                        continue;
                    }
                    String photo = isPhoto(tbStaff);
                    if (photo == null) {
                        logger.error("缺失照片");
                        continue;
                    }
                    TbDevice device = srvDevice.findByDeviceIp(faceReceive.getFaceIp());
                    if (null == device) {
                        logger.error("设备表缺少IP为" + faceReceive.getFaceIp() + "的设备");
                        return;
                    }
                    boolean isSuccess = true;
                    if (device.getDeviceType().equals("TPS980")) {
                        isSuccess = this.sendWhiteList(faceReceive.getFaceIp(), tbStaff, photo);
                    } else if (device.getDeviceType().equals("DS-K5671")) {
                        //linux 下 加载 海康sdk
                        InitHCNetSDK.run(device.getDeviceType());
                        // winds 下海康设备就初始化海康SDK
//                      devicesInit.initHC();
                        isSuccess = sendAccessRecord.setCardAndFace(faceReceive.getFaceIp(), null, vistor, "admin", "wgmhao123");

                    } else if (device.getDeviceType().equals("DS-2CD8627FWD")) {
                        File picAppendData = IPCxmlFile(vistor);
                        String filePath = Constants.VisitorPath + "/" + vistor.getVisitorName() + vistor.getVisitId() + ".jpg";
                        File picture = new File(filePath);
                        isSuccess = sendAccessRecord.sendToIPC(faceReceive.getFaceIp(), picture, picAppendData, null, vistor, "admin", "wgmhao123");
                    } else if (device.getDeviceType().equals("DH-ASI728")) {
                        devicesInit.initDH();
                        sendMap.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                        sendMap.put(Constants.username, "admin");   //设备用户名*
                        sendMap.put(Constants.password, "wgmhao123");   //设备密码*
                        sendMap.put(Constants.photoPath, photo);      //图片*
                        sendMap.put(Constants.userId, String.valueOf(vistor.getUserId()));       //用户id
                        sendMap.put(Constants.userName, vistor.getVisitorName());   //用户姓名*
                        sendMap.put(Constants.idNo, "idNo");           //身份证号码
                        isSuccess = insertInfo(sendMap);

                    }
                    //boolean isSuccess =false;
                    if (isSuccess ) {
                        issued = "1";
                        logger.error("失败名单下发" + tbStaff.getVisitorName() + "再次失败");
                    } else {
                        faceReceive.setReceiveFlag("0");
                        faceReceive.update();
                    }
                }
                vistor.setIsSued(issued);
                vistor.update();
            }
            return;
        }

    }

    /**
     * 照片
     * @param vistor 访客数据
     * @return
     * @throws Exception
     */
    private String isPhoto(TbVisitor vistor) throws Exception {
        String photo = cache.get("photo_" + vistor.getId());
        if (null == photo) {
            String filePath = Constants.VisitorPath + "/" + vistor.getVisitorName() + vistor.getVisitId() + ".jpg";
            System.out.println(filePath);
            //String filePath = "E:\\sts-space\\photoCache\\service\\"+vistor.getUserrealname() + vistor.getSolecode()+ ".jpg";
            File file = new File(filePath);
            if (!file.exists()) {
                logger.error(vistor.getVisitorName() + "无照片");
                return null;
            } else {
                photo = Base64_2.encode(FilesUtils.getBytesFromFile(file));
                cache.set("photo_" + vistor.getId(), photo);
            }
        }
        return photo;
    }

    /**
     * 海景 访客数据下发
     *
     * @param deviceIp 设备ip
     * @param visitor  访客数据
     * @param photo    照片
     * @return
     * @throws Exception
     */
    private boolean sendWhiteList(String deviceIp, TbVisitor visitor, String photo) throws Exception {

        JSONObject paramsJson = new JSONObject();
        String URL = "http://" + deviceIp + ":8080/office/addOrDelUser";
        // String option = user.getCurrentStatus().equals("在职") ? "save" : "delete";
        // System.out.println(service.getUserrealname()+"++"+service.getIdNO());
        paramsJson.put("name", visitor.getVisitorName());
        paramsJson.put("idCard", visitor.getVisitorIdCard());
        paramsJson.put("op", "save");
        paramsJson.put("type", "service");
        paramsJson.put("imageFile", photo);

        StringEntity entity = new StringEntity(paramsJson.toJSONString(), "UTF-8");
        ThirdResponseObj thirdResponseObj = null;
        entity.setContentType("aaplication/json");
        try {
            thirdResponseObj = HttpUtil.http2Se(URL, entity, "UTF-8");
        } catch (Exception e) {
            logger.error("访客数据下发设备" + deviceIp + "错误-->" + e.getMessage());
        }
        if (thirdResponseObj == null) {
            return false;
        }

        FaceDevResponse faceResponse = JSON.parseObject(thirdResponseObj.getResponseEntity(), FaceDevResponse.class);

        if ("success".equals(thirdResponseObj.getCode())) {
            logger.info(visitor.getVisitorName() + "下发" + deviceIp + "成功");
        } else {
            logger.error(visitor.getVisitorName() + "下发" + deviceIp + "失败");
            return false;
        }
        if ("001".equals(faceResponse.getResult())) {
            logger.info("人脸设备接收" + visitor.getVisitorName() + "成功");
            return true;
        } else {
            logger.error("人脸设备接收" + visitor.getVisitorName() + "失败，失败原因：" + faceResponse.getMessage());
            return false;
        }

    }


    /**
     * 获取当前时间 年月日,时分秒
     * @return
     */
    private String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 获取当前时间 年月日, 时分
     * @param dateTime
     * @param pretime
     * @return
     * @throws Exception
     */
    private String preStartTime(String dateTime, int pretime) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dt = sdf.parse(dateTime);
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(dt);
        rightNow.add(Calendar.MINUTE, pretime);
        return sdf.format(rightNow.getTime());
    }

    /**
     * 海康摄像头 照片
     * @param visitor  访客数据
     * @return
     */
    public File IPCxmlFile(TbVisitor visitor) {
        // TODO Auto-generated method stub
        String filePath = Constants.VisitorPath + "/" + visitor.getVisitorName() + visitor.getUserId() + ".xml";
        File filepath = new File(Constants.StaffPath);
        if (!filepath.exists()) {
            filepath.mkdirs();
        }
        File file = new File(filePath);

        StringBuilder builder = new StringBuilder();
        builder.append("<FaceAppendData><name>V");
        builder.append(visitor.getVisitorName());
        builder.append("</name><certificateType>ID</certificateType><certificateNumber>");
        builder.append(visitor.getUserId());
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

    /**
     *  大华添加设备卡信息
     *
     * @param map 添加参数
     * @return true:成功   false:失败
     */
    public Boolean insertInfo(Map<String, String> map) {
        //登录
        LoginModule.login(map.get(Constants.deviceIp), Constants.devicePort, map.get(Constants.username), map.get(Constants.password));

        /**
         * 门禁卡记录集信息
         */
        NET_RECORDSET_ACCESS_CTL_CARD accessCardInfo = new NET_RECORDSET_ACCESS_CTL_CARD();

        // 卡号
        String cardNo = "V" + map.get(Constants.userId);
        System.arraycopy(cardNo.getBytes(), 0, accessCardInfo.szCardNo, 0, cardNo.getBytes().length);

        // 用户ID
        System.arraycopy(map.get(Constants.userId).getBytes(), 0, accessCardInfo.szUserID, 0, map.get(Constants.userId).getBytes().length);

        // 卡名(设备上显示的姓名)
        try {
            System.arraycopy(AccordingToName(map.get(Constants.userName)).getBytes("GBK"), 0, accessCardInfo.szCardName, 0, map.get(Constants.userName).getBytes("GBK").length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 卡密码
        System.arraycopy("0000".getBytes(), 0, accessCardInfo.szPsw, 0, "0000".getBytes().length);

        //-- 设置开门权限
        accessCardInfo.nDoorNum = 2;
        accessCardInfo.sznDoors[0] = 0;
        accessCardInfo.sznDoors[1] = 1;
        accessCardInfo.nTimeSectionNum = 2;      // 与门数对应
        accessCardInfo.sznTimeSectionNo[0] = 255; // 表示第一个门全天有效
        accessCardInfo.sznTimeSectionNo[1] = 255; // 表示第二个门全天有效

        // 有效开始时间
        String[] startTimes = "2020-01-01 00:00:00".split(" ");
        accessCardInfo.stuValidStartTime.dwYear = Integer.parseInt(startTimes[0].split("-")[0]);
        accessCardInfo.stuValidStartTime.dwMonth = Integer.parseInt(startTimes[0].split("-")[1]);
        accessCardInfo.stuValidStartTime.dwDay = Integer.parseInt(startTimes[0].split("-")[2]);
        accessCardInfo.stuValidStartTime.dwHour = Integer.parseInt(startTimes[1].split(":")[0]);
        accessCardInfo.stuValidStartTime.dwMinute = Integer.parseInt(startTimes[1].split(":")[1]);
        accessCardInfo.stuValidStartTime.dwSecond = Integer.parseInt(startTimes[01].split(":")[2]);

        // 有效结束时间
        String[] endTimes = "2030-01-01 23:59:59".split(" ");
        accessCardInfo.stuValidEndTime.dwYear = Integer.parseInt(endTimes[0].split("-")[0]);
        accessCardInfo.stuValidEndTime.dwMonth = Integer.parseInt(endTimes[0].split("-")[1]);
        accessCardInfo.stuValidEndTime.dwDay = Integer.parseInt(endTimes[0].split("-")[2]);
        accessCardInfo.stuValidEndTime.dwHour = Integer.parseInt(endTimes[1].split(":")[0]);
        accessCardInfo.stuValidEndTime.dwMinute = Integer.parseInt(endTimes[1].split(":")[1]);
        accessCardInfo.stuValidEndTime.dwSecond = Integer.parseInt(endTimes[1].split(":")[2]);

        /**
         * 记录集操作
         */
        NetSDKLib.NET_CTRL_RECORDSET_INSERT_PARAM insert = new NetSDKLib.NET_CTRL_RECORDSET_INSERT_PARAM();
        insert.stuCtrlRecordSetInfo.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;   // 记录集类型
        insert.stuCtrlRecordSetInfo.pBuf = accessCardInfo.getPointer();

        accessCardInfo.write();
        insert.write();
        boolean bRet = LoginModule.netsdk.CLIENT_ControlDevice(LoginModule.m_hLoginHandle,
                CtrlType.CTRLTYPE_CTRL_RECORDSET_INSERT, insert.getPointer(), 5000);
        insert.read();
        accessCardInfo.read();

        //二维码 存放路径
        String savePath = PathKit.getWebRootPath() + "\\qrcode\\" + map.get(Constants.userName) + map.get(Constants.userId) + ".jpg";
        boolean result = QRCodeUtil.CreateQRCode(cardNo, savePath, 9, null);
        if (result) {
            logger.info("二维码图片生成成功！");
        } else {
            logger.error("二维码图片生成失败！");
        }

        if (!bRet) {
            logger.error("添加卡信息失败." + ToolKits.getErrorCodePrint());
            return false;
        } else {
            logger.info("添加卡信息成功,卡信息记录集编号 : " + insert.stuCtrlRecordSetResult.nRecNo);
        }
        addFaceInfo(map.get(Constants.userId), map);
        return true;
    }

    //姓名隐藏
    private String AccordingToName(String username) {
        StringBuffer name = null;
        if (username.length() == 1 || username.length() > 5) {
            return null;
        }
        if (username.length() >= 2 || username.length() <= 3) {
            name = new StringBuffer(username);
            //创建StringBuffer对象strb
            name.setCharAt(1, '*');    //修改指定位置的字符
            //输出strb 的长度
            name.setLength(6);      //设置字符串长度，超出部分会被裁剪
        }
        if (username.length() >= 4) {
            name = new StringBuffer(username);
            //创建StringBuffer对象strb
            name.setCharAt(1, '*');    //修改指定位置的字符
            name.setCharAt(2, '*');    //修改指定位置的字符
            //输出strb 的长度
            name.setLength(6);
        }
        String str = new String(name);
        return str;
    }

    /**
     * 添加人脸
     *
     * @param userId 用户ID
     * @param map    图片缓存
     * @return
     */
    private boolean addFaceInfo(String userId, Map<String, String> map) {
        int emType = EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_ADD;  // 添加

        //人脸
        String filePath = map.get(Constants.photoPath);
        try {
            Map<Integer, String> fileMap = FileUtils.readfile(filePath, null);
            Thread.sleep(1000);
            for (int i = 0; i < fileMap.size(); i++) {
                FileUtils.compressImage(fileMap.get(i), "E:\\sts-space\\photoCache\\staff\\" + map.get(Constants.userName) + " .jpg", 390, 520);
                filePath = "E:\\sts-space\\photoCache\\staff\\" + map.get(Constants.userName) + " .jpg";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Memory memory = ToolKits.readPictureFile(filePath);

        /**
         *  入参
         */
        NET_IN_ADD_FACE_INFO stIn = new NET_IN_ADD_FACE_INFO();

        // 用户ID
        System.arraycopy(userId.getBytes(), 0, stIn.szUserID, 0, userId.getBytes().length);

        // 人脸照片个数
        stIn.stuFaceInfo.nFacePhoto = 1;

        // 每张图片的大小
        stIn.stuFaceInfo.nFacePhotoLen[0] = (int) memory.size();

        // 人脸照片数据,大小不超过100K, 图片格式为jpg
        stIn.stuFaceInfo.pszFacePhotoArr[0].pszFacePhoto = memory;

        /**
         *  出参
         */
        NET_OUT_ADD_FACE_INFO stOut = new NET_OUT_ADD_FACE_INFO();

        stIn.write();
        stOut.write();
        boolean bRet = LoginModule.netsdk.CLIENT_FaceInfoOpreate(LoginModule.m_hLoginHandle, emType, stIn.getPointer(), stOut.getPointer(), 5000);
        stIn.read();
        stOut.read();
        if (bRet) {
            logger.info("添加人脸成功!");
        } else {
            logger.error("添加人脸失败!" + ToolKits.getErrorCodePrint());
            return false;
        }
        File file = new File(filePath);
        file.delete();
        //退出登录
        LoginModule.logout();
        return true;
    }

}
