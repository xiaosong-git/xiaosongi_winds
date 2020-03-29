package com.xiaosong.common.floor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.xiaosong.common.device.DeviceService;
import com.xiaosong.common.personnel.PersonService;
import com.xiaosong.common.service.FailReceiveService;
import com.xiaosong.common.service.StaffService;
import com.xiaosong.config.QRCodeModel.GetAllStaffModel;
import com.xiaosong.config.devicesInit;
import com.xiaosong.constant.Constants;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.constant.FaceDevResponse;
import com.xiaosong.model.*;
import com.xiaosong.util.*;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大楼配置
 */

public class FloorController extends Controller {
    public FloorService srv = FloorService.me;
    OkHttpUtil okHttpUtil = new OkHttpUtil();
    private PersonService srvPer = PersonService.me;
    private DeviceService srvDevice = DeviceService.me; //设备业务层

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

            if (orgCode != null) {
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

            } else {
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
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "查询异常~"));
        }
    }

    public void pullUser() throws Exception {
        int pageNum = 1;
        int pageSize = 20;
        int totalPage = 0;
        int total = 0;
        Map<String, String> map = new HashMap<>();
        map.put("orgCode","hlxz");
        map.put("pageNum", String.valueOf(pageNum));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pospCode", "00000001");
        map.put("mac", "jLjEwTzY06RFHTbTT8cyfNt+pwJCMzJgFSmO5mKapOLeI41N6wUvvnJ2D6jf76kN7su5LeHgyD70HEGM2K39s46tjTB+vvm0M9eaEUlyJBjykCVAYVt9GUFSrYKu2UTijLUPuQuH8yD/CrIDgZV3gGUT+YMNjwxVccSq4967vGLCYrujmb227R3MiuBuGT4w01+J+NctdWiuPPf4VsyJ5inzAxpWziBqmYaktrAPMqXJ0fdTfC+gsnx9C1FFTzFef00O3TQ/32WbX6tzX8OjCqkTLH4lGN7+cnvtWuMqmuhp0BNJHlXM8pDUlf+Q1iE10REyUV2AT4IdQ7T60MO7ZQ==");
        StringBuilder stringBuilder = new StringBuilder();

        // stringBuilder.append("http://192.168.10.129:8098/visitor/companyUser/findApplyAllSucOrg");
        stringBuilder.append(Constants.baseURl);
        stringBuilder.append(Constants.pullOrgCompanyUrl);

        String url = stringBuilder.toString();
        logger.info("获取员工数据地址：" + url);
        // System.out.println("获取员工数据地址：" + url);

        String responseContent = okHttpUtil.post(url, map);
        GetAllStaffModel allStaffModel = JSON.parseObject(responseContent, GetAllStaffModel.class);
        totalPage = allStaffModel.getData().getTotalPage();
        total = allStaffModel.getData().getTotal();
        if (total > 0) {
            srv.deleteAll();
            List<TbCompanyuser> companyUserList = allStaffModel.getData().getRows();
            for (TbCompanyuser companyUser : companyUserList) {
                sendUsers(companyUser);
            }
            for (int i = 2; i <= totalPage; i++) {
                pageNum = i;
                map.put("pageNum", String.valueOf(pageNum));
                responseContent = okHttpUtil.post(url, map);
                allStaffModel = JSON.parseObject(responseContent, GetAllStaffModel.class);
                List<TbCompanyuser> companyUserList2 = allStaffModel.getData().getRows();
                for (TbCompanyuser companyUser : companyUserList2) {
                    sendUsers(companyUser);
                }
            }
            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL));
            logger.info("成功~");
        } else {
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
            logger.info("失败~");
        }
    }

    private void sendUsers(TbCompanyuser companyUser) throws Exception {

        // 非正常状态员工不接收
        if (!"normal".equals(companyUser.getCurrentStatus())) {
            return;
        }
        companyUser.setIsSued("1");
        companyUser.setIsDel("1");
        companyUser.setReceiveDate(getDate());
        companyUser.setReceiveTime(getTime());
        companyUser.save();

        if (companyUser.getPhoto() != null) {
            //	redisUtils.set("photo_" + companyUser.getCompanyId() + "_" + companyUser.getIdNO(), companyUser.getPhoto());
            byte[] photoKey = Base64_2.decode(companyUser.getPhoto());
            String fileName = companyUser.getUserName() + companyUser.getCompanyId() + ".jpg";
            File fileload = FilesUtils.getFileFromBytes(photoKey, Constants.StaffPath, fileName);
            logger.info("初始化员工存放照片地址" + fileload.getAbsolutePath());
        } else {
            logger.warn(companyUser.getUserName() + "该用户无照片");
            //String keysign = towerInforService.findOrgId() + towerInforService.findPospCode()
            //		+ towerInforService.findKey();
            //logger.sendErrorLog(towerInforService.findOrgId(), companyUser.getUserName() + "该用户无照片", "", "数据错误",
            //		Constants.errorLogUrl, keysign);
            return;
        }

        String companyfloor = null;
        if (null != companyUser.getCompanyFloor()) {
            companyfloor = companyUser.getCompanyFloor();
        }

	/*	String photo = isPhoto(companyUser);
        if (photo == null) {
			return;
		}*/
        System.out.println(companyfloor);
        List<String> allFaceDecive = srvDevice.getAllFaceDeviceIP(companyfloor);
        System.out.println(allFaceDecive.size());

        if (allFaceDecive.size() <= 0 || allFaceDecive == null) {
            System.out.println("无设备下发");
            return;
        }
        System.out.println("共需要下发" + allFaceDecive.size() + "台");
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
                //海景设备TPS980
                if (device.getDeviceType().equals("TPS980")) {
                    isSuccess = sendWhiteList((String) allFaceDecive.get(i), companyUser,
                            companyUser.getPhoto());
                    //海康门禁设备DS-K5671
                } else if (device.getDeviceType().equals("DS-K5671")) {
                    //isSuccess =hcNetSDKService.setCardAndFace(allFaceDecive.get(i), companyUser, null);
                    //linux 下 加载 海康sdk
//                            InitHCNetSDK.run(deviceType);
                    // winds 下海康设备就初始化海康SDK
                    devicesInit.initHC();
                    Map<String, String> hcMap = new HashMap<>();
                    hcMap.put("strCardNo", "S" + companyUser.getUserId());
                    hcMap.put("userId", String.valueOf(companyUser.getUserId()));
                    hcMap.put("userName", companyUser.getUserName());
                    String filePath = Constants.StaffPath + "\\" + companyUser.getUserName() + companyUser.getCompanyId() + ".jpg";
                    hcMap.put("filePath", filePath);
                    hcMap.put("personType", "staff");
                    hcMap.put(Constants.username, "admin");
                    hcMap.put(Constants.password, "wgmhao123");
                    hcMap.put(Constants.deviceIp, allFaceDecive.get(i));
                    isSuccess = srvPer.insertInfoHKGuard(hcMap);

                    //isSuccess = setUser(device, companyUser);
                } else if (device.getDeviceType().equals("DH-ASI728")) {
                    //初始化大华设备
                    devicesInit.initDH();
                    Map<String, String> hcMap = new HashMap<>();
                    hcMap.put("strCardNo", "S" + companyUser.getUserId());
                    hcMap.put("userId", String.valueOf(companyUser.getUserId()));
                    hcMap.put("userName", companyUser.getUserName());
                    String filePath = Constants.StaffPath + "\\" + companyUser.getUserName() + companyUser.getCompanyId() + ".jpg";
                    hcMap.put("filePath", filePath);
                    hcMap.put("personType", "staff");
                    hcMap.put(Constants.username, "admin");
                    hcMap.put(Constants.password, "wgmhao123");
                    hcMap.put(Constants.deviceIp, allFaceDecive.get(i));

                    isSuccess = srvPer.insertInfo(hcMap);

                }
                // 针对下发失败的需要登记，待下次冲洗下发，已经下发成功的不在下发

                System.out.println(allFaceDecive.get(i));
                if (isSuccess == false) {
                    issued = "1";
                    TbFailreceive faceReceive = FailReceiveService.me.findOne(allFaceDecive.get(i), companyUser.getUserName(), companyUser.getIdNO(), "staff");

                    if (null == faceReceive) {
                        TbFailreceive newFaceFail = new TbFailreceive();
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
                        count++;
                        faceReceive.setDownNum(count);
                        System.out.println("*****************" + count);
                        faceReceive.save();
                    }
                }
            }
            companyUser.setIsSued(issued);
            companyUser.update();
        }
        return;
    }

    private String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
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

    private boolean sendWhiteList(String deviceIp, TbCompanyuser visitor, String photo) throws Exception {

        JSONObject paramsJson = new JSONObject();
        String URL = "http://" + deviceIp + ":8080/office/addOrDelUser";
        // String option = user.getCurrentStatus().equals("在职") ? "save" : "delete";
        // System.out.println(service.getUserrealname()+"++"+service.getIdNO());
        paramsJson.put("name", visitor.getUserName());
        paramsJson.put("idCard", visitor.getIdNO());
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
            logger.info(visitor.getUserName() + "下发" + deviceIp + "成功");
        } else {
            logger.error(visitor.getUserName() + "下发" + deviceIp + "失败");
            return false;
        }
        if ("001".equals(faceResponse.getResult())) {
            logger.info("人脸设备接收" + visitor.getUserName() + "成功");
            return true;
        } else {
            logger.error("人脸设备接收" + visitor.getUserName() + "失败，失败原因：" + faceResponse.getMessage());
            return false;
        }

    }
}
