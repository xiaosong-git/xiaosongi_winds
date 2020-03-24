package com.xiaosong.common.personnel;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dhnetsdk.lib.FileUtils;
import com.dhnetsdk.lib.NetSDKLib.*;
import com.dhnetsdk.lib.ToolKits;
import com.dhnetsdk.module.LoginModule;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.xiaosong.config.HCNetSDK;
import com.xiaosong.constant.Constant;
import com.xiaosong.constant.Constants;
import com.xiaosong.constant.FaceDevResponse;
import com.xiaosong.model.TbCompanyuser;
import com.xiaosong.model.TbDevice;
import com.xiaosong.util.HttpUtil;
import com.xiaosong.util.QRCodeUtil;
import com.xiaosong.util.ThirdResponseObj;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PersonService {
    public static final PersonService me = new PersonService();
    private static Logger logger = Logger.getLogger(PersonService.class);

    /**
     * 添加用户
     *
     * @param tc 用户参数
     * @return
     */
    public boolean save(TbCompanyuser tc) {
        return tc.save();
    }

    public static int cardInfo ;
    /**
     * 添加卡
     *
     * @param map 添加参数
     * @return true:成功   false:失败
     */
    public boolean insertInfo(Map<String, String> map) {
        //登录
        LoginModule.login(map.get(Constants.deviceIp), Constants.devicePort, map.get(Constants.username), map.get(Constants.password));

        /**
         * 门禁卡记录集信息
         */
        NET_RECORDSET_ACCESS_CTL_CARD accessCardInfo = new NET_RECORDSET_ACCESS_CTL_CARD();

        // 卡号
        String cardNo = "S" + map.get(Constants.userId);
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
        String[] endTimes = "2099-01-01 23:59:59".split(" ");
        accessCardInfo.stuValidEndTime.dwYear = Integer.parseInt(endTimes[0].split("-")[0]);
        accessCardInfo.stuValidEndTime.dwMonth = Integer.parseInt(endTimes[0].split("-")[1]);
        accessCardInfo.stuValidEndTime.dwDay = Integer.parseInt(endTimes[0].split("-")[2]);
        accessCardInfo.stuValidEndTime.dwHour = Integer.parseInt(endTimes[1].split(":")[0]);
        accessCardInfo.stuValidEndTime.dwMinute = Integer.parseInt(endTimes[1].split(":")[1]);
        accessCardInfo.stuValidEndTime.dwSecond = Integer.parseInt(endTimes[1].split(":")[2]);

        /**
         * 记录集操作
         */
        NET_CTRL_RECORDSET_INSERT_PARAM insert = new NET_CTRL_RECORDSET_INSERT_PARAM();
        insert.stuCtrlRecordSetInfo.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;   // 记录集类型
        insert.stuCtrlRecordSetInfo.pBuf = accessCardInfo.getPointer();

        accessCardInfo.write();
        insert.write();
        boolean bRet = LoginModule.netsdk.CLIENT_ControlDevice(LoginModule.m_hLoginHandle,
                CtrlType.CTRLTYPE_CTRL_RECORDSET_INSERT, insert.getPointer(), 5000);
        insert.read();
        accessCardInfo.read();

        //生成二维码 并存放winds/linux 路径下
        String savePath = PathKit.getWebRootPath()+"\\qrcode\\"+map.get(Constants.userName) + map.get(Constants.userId) + ".jpg";
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
//            card = insert.stuCtrlRecordSetResult.nRecNo;
            logger.info("添加卡信息成功,卡信息记录集编号 : " + insert.stuCtrlRecordSetResult.nRecNo);
//            map.put(Constants.CardInfo, String.valueOf(insert.stuCtrlRecordSetResult.nRecNo));
            cardInfo = insert.stuCtrlRecordSetResult.nRecNo;
        }

        addFaceInfo(map.get(Constants.userId), map);
        return true;
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
//                FileUtils.compressImage(fileMap.get(i), "E:\\sts-space\\photoCache\\staff\\" + map.get(Constants.userName) + " .jpg", 390, 520);
//                filePath = "E:\\sts-space\\photoCache\\staff\\" + map.get(Constants.userName) + " .jpg";

                FileUtils.compressImage(fileMap.get(i), Constant.BASE_UPLOAD_PATH + map.get(Constants.userName) + ".jpg", 390, 520);
                filePath = Constant.BASE_UPLOAD_PATH + map.get(Constants.userName) + ".jpg";

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

    /**
     * 修改卡信息
     *
     * @param map 修改参数
     * @return true:成功   false:失败
     */
    public boolean updateDH(Map<String, String> map) {
        //登录
        LoginModule.login(map.get(Constants.deviceIp), Constants.devicePort, map.get(Constants.username), map.get(Constants.password));

        /**
         * 门禁卡记录集信息
         */
        NET_RECORDSET_ACCESS_CTL_CARD accessCardInfo = new NET_RECORDSET_ACCESS_CTL_CARD();
        // 记录集编号， 修改、删除卡信息必须填写
        accessCardInfo.nRecNo = Integer.parseInt(map.get(Constants.userId));

        // 卡号
        String cardNo = "S" + map.get(Constants.userId);
        System.arraycopy(cardNo.getBytes(), 0, accessCardInfo.szCardNo, 0, cardNo.getBytes().length);

        // 用户ID
        System.arraycopy(AccordingToName(map.get(Constants.userName)).getBytes(), 0, accessCardInfo.szUserID, 0, AccordingToName(map.get(Constants.userName)).getBytes().length);

        // 卡名(设备上显示的姓名)
        try {
            System.arraycopy(map.get(Constants.userName).getBytes("GBK"), 0, accessCardInfo.szCardName, 0, map.get(Constants.userName).getBytes("GBK").length);
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
        NET_CTRL_RECORDSET_PARAM update = new NET_CTRL_RECORDSET_PARAM();
        update.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;    // 记录集信息类型
        update.pBuf = accessCardInfo.getPointer();

        accessCardInfo.write();
        update.write();
        boolean bRet = LoginModule.netsdk.CLIENT_ControlDevice(LoginModule.m_hLoginHandle,
                CtrlType.CTRLTYPE_CTRL_RECORDSET_UPDATE, update.getPointer(), 5000);
        update.read();
        accessCardInfo.read();
        //生成二维码 并存放winds/linux 路径下
        String savePath = PathKit.getWebRootPath()+"\\qrcode\\"+map.get(Constants.userName) + map.get(Constants.userId) + ".jpg";
        boolean result = QRCodeUtil.CreateQRCode(cardNo, savePath, 9, null);
        if (result) {
            logger.info("二维码图片生成成功！");
        } else {
            logger.error("二维码图片生成失败！");
        }
        if (!bRet) {
            System.err.println("修改卡信息失败." + ToolKits.getErrorCodePrint());
            return false;
        } else {
            System.out.println("修改卡信息成功 ");
        }
        updateDHFace(map.get(Constants.userId), map);
        return true;
    }

    /**
     * 修改大华人脸
     *
     * @param userId 用户ID
     * @param map    修改的参数
     * @return
     */
    public static boolean updateDHFace(String userId, Map<String, String> map) {
        int emType = EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_UPDATE;  // 修改
        /**
         *  入参
         */
        NET_IN_UPDATE_FACE_INFO stIn = new NET_IN_UPDATE_FACE_INFO();

        //人脸
        String filePath = map.get(Constants.photoPath);
        try {
            Map<Integer, String> fileMap = FileUtils.readfile(filePath, null);
            Thread.sleep(2000);
            for (int i = 0; i < fileMap.size(); i++) {
//                FileUtils.compressImage(fileMap.get(i), "E:\\sts-space\\photoCache\\staff\\" + map.get(Constants.userName) + " .jpg", 390, 520);
//                filePath = "E:\\sts-space\\photoCache\\staff\\" + map.get(Constants.userName) + " .jpg";
                FileUtils.compressImage(fileMap.get(i), Constant.BASE_UPLOAD_PATH + map.get(Constants.userName) + ".jpg", 390, 520);
                filePath = Constant.BASE_UPLOAD_PATH + map.get(Constants.userName) + ".jpg";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Memory memory = ToolKits.readPictureFile(filePath);

        // 用户ID
        System.arraycopy(userId.getBytes(), 0, stIn.szUserID, 0, userId.getBytes().length);

        // 人脸照片个数
        stIn.stuFaceInfo.nFacePhoto = 1;

        // 每张图片的大小
        stIn.stuFaceInfo.nFacePhotoLen[0] = (int) memory.size();
        System.out.println((int) memory.size());

        // 人脸照片数据,大小不超过100K, 图片格式为jpg
        stIn.stuFaceInfo.pszFacePhotoArr[0].pszFacePhoto = memory;

        /**
         *  出参
         */
        NET_OUT_UPDATE_FACE_INFO stOut = new NET_OUT_UPDATE_FACE_INFO();

        stIn.write();
        stOut.write();
        boolean bRet = LoginModule.netsdk.CLIENT_FaceInfoOpreate(LoginModule.m_hLoginHandle, emType, stIn.getPointer(), stOut.getPointer(), 5000);
        stIn.read();
        stOut.read();
        if (bRet) {
            System.out.println("修改人脸成功!");
        } else {
            System.err.println("修改人脸失败!" + ToolKits.getErrorCodePrint());
            return false;
        }
        //退出登录
        LoginModule.logout();

        return true;
    }

    int lUserID;// 用户句柄


    HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    NativeLong m_lUploadHandle;

    NativeLong m_UploadStatus;

    /**
     * 海康设备 人脸添加
     *
     * @param map 下发参数
     * @return
     * @throws UnsupportedEncodingException
     */
    public boolean insertInfoHKGuard(Map<String, String> map) throws UnsupportedEncodingException {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String strCardNo = "S" + map.get(Constants.userId);
        boolean setCard = setCardInfo(map.get(Constants.deviceIp), Integer.parseInt(map.get(Constants.userId)),
                map.get(Constants.userName), strCardNo, "normal", map);
        if (!setCard) {
            return false;
        }
        //生成二维码 并存放winds/linux 路径下
        String savePath = PathKit.getWebRootPath()+"\\qrcode\\"+map.get(Constants.userName) + map.get(Constants.userId) + ".jpg";
        boolean result = QRCodeUtil.CreateQRCode(strCardNo, savePath, 9, null);
        if (result) {
            logger.info("二维码图片生成成功！");
        } else {
            logger.error("二维码图片生成失败！");
        }
        return setFace(map.get(Constants.deviceIp), strCardNo, map);
    }

    /**
     * 添加卡信息
     *
     * @param deviceIP     设备ip
     * @param dwEmployeeNo 公司ud
     * @param name         用户名
     * @param strCardNo    卡号
     * @param isdel        是否删除
     * @param map          设备参数
     * @return
     * @throws UnsupportedEncodingException
     */
    public boolean setCardInfo(String deviceIP, int dwEmployeeNo, String name, String strCardNo, String isdel, Map<String, String> map)
            throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        lUserID = initAndLoginHK(deviceIP, map);
        if (lUserID < 0) {
            return false;
        }
        int iErr = 0;

        // 设置卡参数
        HCNetSDK.NET_DVR_CARD_CFG_COND m_struCardInputParamSet = new HCNetSDK.NET_DVR_CARD_CFG_COND();
        m_struCardInputParamSet.read();
        m_struCardInputParamSet.dwSize = m_struCardInputParamSet.size();
        m_struCardInputParamSet.dwCardNum = 1;
        m_struCardInputParamSet.byCheckCardNo = 1;

        Pointer lpInBuffer = m_struCardInputParamSet.getPointer();
        m_struCardInputParamSet.write();

        Pointer pUserData = null;
        FRemoteCfgCallBackCardSet fRemoteCfgCallBackCardSet = new FRemoteCfgCallBackCardSet();

        int lHandle = this.hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_SET_CARD_CFG_V50, lpInBuffer,
                m_struCardInputParamSet.size(), fRemoteCfgCallBackCardSet, pUserData);
        if (lHandle < 0) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            logger.error("建立长连接失败，错误号：" + iErr);
            return false;
        }

        HCNetSDK.NET_DVR_CARD_CFG_V50 struCardInfo = new HCNetSDK.NET_DVR_CARD_CFG_V50(); // 卡参数
        struCardInfo.read();
        struCardInfo.dwSize = struCardInfo.size();
        struCardInfo.dwModifyParamType = 0x6DAF;// 0x00000001 + 0x00000002 + 0x00000004 + 0x00000008 +
        // 0x00000010 + 0x00000020 + 0x00000080 + 0x00000100 + 0x00000200 + 0x00000400 +
        // 0x00000800;
        /***
         * #define CARD_PARAM_CARD_VALID 0x00000001 //卡是否有效参数 #define CARD_PARAM_VALID
         * 0x00000002 //有效期参数 #define CARD_PARAM_CARD_TYPE 0x00000004 //卡类型参数 #define
         * CARD_PARAM_DOOR_RIGHT 0x00000008 //门权限参数 #define CARD_PARAM_LEADER_CARD
         * 0x00000010 //首卡参数 #define CARD_PARAM_SWIPE_NUM 0x00000020 //最大刷卡次数参数 #define
         * CARD_PARAM_GROUP 0x00000040 //所属群组参数 #define CARD_PARAM_PASSWORD 0x00000080
         * //卡密码参数 #define CARD_PARAM_RIGHT_PLAN 0x00000100 //卡权限计划参数 #define
         * CARD_PARAM_SWIPED_NUM 0x00000200 //已刷卡次数 #define CARD_PARAM_EMPLOYEE_NO
         * 0x00000400 //工号 #define CARD_PARAM_NAME 0x00000800 //姓名
         */
        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            struCardInfo.byCardNo[i] = 0;
        }
        for (int i = 0; i < strCardNo.length(); i++) {
            struCardInfo.byCardNo[i] = strCardNo.getBytes()[i];
        }
        if ("delete".equals(isdel)) {
            struCardInfo.byCardValid = 0;// 0-无效,1-有效
        } else {
            struCardInfo.byCardValid = 1;
        }
        struCardInfo.byCardType = 1;
        struCardInfo.byLeaderCard = 0;
        struCardInfo.byDoorRight[0] = 1; // 门1有权限
        struCardInfo.wCardRightPlan[0].wRightPlan[0] = 1; // 门1关联卡参数计划模板1

        // 卡有效期
        struCardInfo.struValid.byEnable = 1;
        struCardInfo.struValid.struBeginTime.wYear = 2010;
        struCardInfo.struValid.struBeginTime.byMonth = 12;
        struCardInfo.struValid.struBeginTime.byDay = 1;
        struCardInfo.struValid.struBeginTime.byHour = 0;
        struCardInfo.struValid.struBeginTime.byMinute = 0;
        struCardInfo.struValid.struBeginTime.bySecond = 0;
        struCardInfo.struValid.struEndTime.wYear = 2024;
        struCardInfo.struValid.struEndTime.byMonth = 12;
        struCardInfo.struValid.struEndTime.byDay = 1;
        struCardInfo.struValid.struEndTime.byHour = 0;
        struCardInfo.struValid.struEndTime.byMinute = 0;
        struCardInfo.struValid.struEndTime.bySecond = 0;

        struCardInfo.dwMaxSwipeTime = 0; // 无次数限制
        struCardInfo.dwSwipeTime = 0;
        struCardInfo.byCardPassword = "123456".getBytes();
        struCardInfo.dwEmployeeNo = dwEmployeeNo;
        struCardInfo.wSchedulePlanNo = 1;
        struCardInfo.bySchedulePlanType = 2;
        struCardInfo.wDepartmentNo = 1;

        byte[] strCardName = AccordingToName(name).getBytes("GBK");
        for (int i = 0; i < HCNetSDK.NAME_LEN; i++) {
            struCardInfo.byName[i] = 0;
        }
        for (int i = 0; i < strCardName.length; i++) {
            struCardInfo.byName[i] = strCardName[i];
        }

        struCardInfo.write();
        Pointer pSendBufSet = struCardInfo.getPointer();

        if (!hCNetSDK.NET_DVR_SendRemoteConfig(lHandle, 0x3, pSendBufSet, struCardInfo.size())) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            logger.error("ENUM_ACS_SEND_DATA失败，错误号：" + iErr);
            return false;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle)) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            logger.error("断开长连接失败，错误号：" + iErr);
            return false;
        }
        logger.error("断开长连接成功!");
        return true;
    }

    /**
     * @param deviceIP  设备ip
     * @param strCardNo 卡号
     * @param map       人脸 参数
     * @return
     * @throws UnsupportedEncodingException
     */
    public boolean setFace(String deviceIP, String strCardNo, Map<String, String> map)
            throws UnsupportedEncodingException {
        // TODO Auto-generated method stub
        lUserID = initAndLoginHK(deviceIP, map);
        if (lUserID < 0) {
            return false;
        }
        int iErr = 0; // 错误号

        // 设置人脸参数
        HCNetSDK.NET_DVR_FACE_PARAM_COND m_struFaceSetParam = new HCNetSDK.NET_DVR_FACE_PARAM_COND();
        m_struFaceSetParam.dwSize = m_struFaceSetParam.size();

        // String strCardNo = "201909";// 人脸关联的卡号
        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            m_struFaceSetParam.byCardNo[i] = 0;
        }
        System.arraycopy(strCardNo.getBytes(), 0, m_struFaceSetParam.byCardNo, 0, strCardNo.length());

        m_struFaceSetParam.byEnableCardReader[0] = 1;
        m_struFaceSetParam.dwFaceNum = 1;
        m_struFaceSetParam.byFaceID = 1;
        m_struFaceSetParam.write();

        Pointer lpInBuffer = m_struFaceSetParam.getPointer();

        Pointer pUserData = null;
        FRemoteCfgCallBackFaceSet fRemoteCfgCallBackFaceSet = new FRemoteCfgCallBackFaceSet();

        int lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_SET_FACE_PARAM_CFG, lpInBuffer,
                m_struFaceSetParam.size(), fRemoteCfgCallBackFaceSet, pUserData);
        if (lHandle < 0) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            logger.error("建立长连接失败，错误号：" + iErr);
            return false;
        }

        HCNetSDK.NET_DVR_FACE_PARAM_CFG struFaceInfo = new HCNetSDK.NET_DVR_FACE_PARAM_CFG(); // 卡参数
        struFaceInfo.read();
        struFaceInfo.dwSize = struFaceInfo.size();

        // strCardNo = "201909";// 人脸关联的卡号
        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            struFaceInfo.byCardNo[i] = 0;
        }

        System.arraycopy(strCardNo.getBytes(), 0, struFaceInfo.byCardNo, 0, strCardNo.length());


        struFaceInfo.byEnableCardReader[0] = 1; // 需要下发人脸的读卡器，按数组表示，每位数组表示一个读卡器，数组取值：0-不下发该读卡器，1-下发到该读卡器
        struFaceInfo.byFaceID = 1; // 人脸ID编号，有效取值范围：1~2
        struFaceInfo.byFaceDataType = 1; // 人脸数据类型：0- 模板（默认），1- 图片

        /*****************************************
         * 从本地文件里面读取JPEG图片二进制数据
         *****************************************/
        FileInputStream picfile = null;
        int picdataLength = 0;
        try {

            File picture = new File(map.get(Constants.photoPath));
            if (!picture.exists()) {
                return false;
            }

            picfile = new FileInputStream(picture);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

        try {
            picdataLength = picfile.available();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (picdataLength < 0) {
            System.out.println("input file dataSize < 0");
            logger.error("input file dataSize < 0");
            logger.error("照片数据错误");
            return false;
        }

        HCNetSDK.BYTE_ARRAY ptrpicByte = new HCNetSDK.BYTE_ARRAY(picdataLength);
        try {
            picfile.read(ptrpicByte.byValue);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        ptrpicByte.write();
        /**************************/

        struFaceInfo.dwFaceLen = picdataLength;
        struFaceInfo.pFaceBuffer = ptrpicByte.getPointer();

        struFaceInfo.write();
        Pointer pSendBufSet = struFaceInfo.getPointer();
        logger.info(lHandle + "*" + pSendBufSet + "*" + 0x9 + "*" + struFaceInfo.size());
        // ENUM_ACS_INTELLIGENT_IDENTITY_DATA = 9, //智能身份识别终端数据类型，下发人脸图片数据
        if (!hCNetSDK.NET_DVR_SendRemoteConfig(lHandle, 0x9, pSendBufSet, struFaceInfo.size())) {

            iErr = hCNetSDK.NET_DVR_GetLastError();
            logger.error("NET_DVR_SendRemoteConfig失败，错误号：" + iErr);
            return false;
        }


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (fRemoteCfgCallBackFaceSet.sendFlag != 1 || fRemoteCfgCallBackFaceSet.faceFlag != 1) {
            logger.error("下发人脸参数失败");
            return false;
        }
        if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle)) {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            logger.error("断开长连接失败，错误号：" + iErr);
            return false;
        }
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
     * 根据 楼层 查询设备型号 设备ip 设备用户名 设备密码
     *
     * @param contralFloor 楼层
     */
    public List<TbDevice> findByFloor(String contralFloor) {
        return TbDevice.dao.find("select * from tb_device where contralFloor = ?", contralFloor);
    }

    /**
     * 查询所有的 员工
     *
     * @return
     */
    public List<TbCompanyuser> findCompanyuser() {
        return TbCompanyuser.dao.find("select * from tb_companyuser");
    }

    /**
     * 根据 用户主键查询 用户所在楼层
     *
     * @param companyUserId 用户主键
     */
    public TbCompanyuser findCompanyUser(String companyUserId) {
        return TbCompanyuser.dao.findFirst("select * from tb_companyuser where companyUserId = ?",companyUserId);

    }

    /**
     * 删除大华设备的人脸
     *
     * @param map 删除参数
     */
    public boolean deleteDH(Map<String, String> map, Integer cardInfo) {
        //登录
        LoginModule.login(map.get(Constants.deviceIp), Constants.devicePort, map.get(Constants.username), map.get(Constants.password));

        /**
         * 记录集操作
         */
        NET_CTRL_RECORDSET_PARAM msg = new NET_CTRL_RECORDSET_PARAM();
        msg.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
        msg.pBuf = new IntByReference(cardInfo).getPointer();

        msg.write();
        boolean bRet = LoginModule.netsdk.CLIENT_ControlDevice(LoginModule.m_hLoginHandle,
                CtrlType.CTRLTYPE_CTRL_RECORDSET_REMOVE, msg.getPointer(), 5000);
        msg.read();

        if (!bRet) {
            System.err.println("删除卡信息失败." + ToolKits.getErrorCodePrint());
            return false;
        } else {
            System.out.println("删除卡信息成功.");
        }

        return true;
    }

    /**
     * 删除用户
     *
     * @param companyUserId 用户主键
     */
    public int delete(String companyUserId) {
        return Db.delete("delete from tb_companyuser where companyUserId = ?", companyUserId);
    }

    public List<TbCompanyuser> findByName(String username) {
        return TbCompanyuser.dao.find("select * from tb_companyuser where username like '%" + username + "%'");

    }

    class FRemoteCfgCallBackCardSet implements HCNetSDK.FRemoteConfigCallback {

        public int sendFlag = -1;        //卡状态下发返回标记（1成功，-1失败,0正在下发）

        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData) {
            switch (dwType) {
                case 0:// NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.BYTE_ARRAY struCallbackStatus = new HCNetSDK.BYTE_ARRAY(40);
                    struCallbackStatus.write();
                    Pointer pStatus = struCallbackStatus.getPointer();
                    pStatus.write(0, lpBuffer.getByteArray(0, struCallbackStatus.size()), 0, dwBufLen);
                    struCallbackStatus.read();

                    int iStatus = 0;
                    byte[] byCardNo;
                    for (int i = 0; i < 4; i++) {
                        int ioffset = i * 8;
                        int iByte = struCallbackStatus.byValue[i] & 0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus) {
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            logger.info("下发卡参数成功");
                            sendFlag = 1;
                            break;
                        case 1001:
                            byCardNo = new byte[32];
                            System.arraycopy(struCallbackStatus.byValue, 4, byCardNo, 0, 32);
                            logger.info("正在下发卡参数中,卡号:" + new String(byCardNo).trim());
                            sendFlag = 0;
                            break;
                        case 1002:
                            int iErrorCode = 0;
                            for (int i = 0; i < 4; i++) {
                                int ioffset = i * 8;
                                int iByte = struCallbackStatus.byValue[i + 4] & 0xff;
                                iErrorCode = iErrorCode + (iByte << ioffset);
                            }
                            byCardNo = new byte[32];
                            System.arraycopy(struCallbackStatus.byValue, 8, byCardNo, 0, 32);
                            logger.error("下发卡参数失败,卡号:" + new String(byCardNo).trim() + ",错误号:" + iErrorCode);
                            sendFlag = -1;
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /***
     *
     * 门禁设备人脸下发回调函数
     * @author Admin
     *
     */
    class FRemoteCfgCallBackFaceSet implements HCNetSDK.FRemoteConfigCallback {

        public int faceFlag = -1;    //人脸数据状态
        public int sendFlag = -1;    //人脸下发状态

        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData) {
            switch (dwType) {
                case 0:// NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.BYTE_ARRAY struCallbackStatus = new HCNetSDK.BYTE_ARRAY(40);
                    struCallbackStatus.write();
                    Pointer pStatus = struCallbackStatus.getPointer();
                    pStatus.write(0, lpBuffer.getByteArray(0, struCallbackStatus.size()), 0, dwBufLen);
                    struCallbackStatus.read();

                    int iStatus = 0;
                    byte[] byCardNo;

                    for (int i = 0; i < 4; i++) {
                        int ioffset = i * 8;
                        int iByte = struCallbackStatus.byValue[i] & 0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus) {
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            logger.info("下发人脸参数成功");
                            sendFlag = 1;
                            break;
                        case 1001:
                            byCardNo = new byte[32];
                            System.arraycopy(struCallbackStatus.byValue, 4, byCardNo, 0, 32);
                            logger.info("正在下发人脸参数,卡号:" + new String(byCardNo).trim());
                            sendFlag = 0;
                            break;
                        case 1002:
                            int iErrorCode = 0;
                            for (int i = 0; i < 4; i++) {
                                int ioffset = i * 8;
                                int iByte = struCallbackStatus.byValue[i + 4] & 0xff;
                                iErrorCode = iErrorCode + (iByte << ioffset);
                            }
                            byCardNo = new byte[32];
                            System.arraycopy(struCallbackStatus.byValue, 8, byCardNo, 0, 32);
                            logger.error("下发人脸参数失败,错误号:" + iErrorCode + ",卡号:" + new String(byCardNo).trim());
                            sendFlag = -1;
                            break;
                    }
                    break;
                case 2:// 获取状态数据
                    HCNetSDK.NET_DVR_FACE_PARAM_STATUS m_struFaceStatus = new HCNetSDK.NET_DVR_FACE_PARAM_STATUS();
                    m_struFaceStatus.write();
                    Pointer pStatusInfo = m_struFaceStatus.getPointer();
                    pStatusInfo.write(0, lpBuffer.getByteArray(0, m_struFaceStatus.size()), 0, m_struFaceStatus.size());
                    m_struFaceStatus.read();
                    if (m_struFaceStatus.byCardReaderRecvStatus[0] == 1) {
                        logger.info("人脸读卡器状态正常，照片可下发");
                        faceFlag = 1;
                    } else {
                        logger.error("人脸读卡器状态错误，状态值：" + m_struFaceStatus.byCardReaderRecvStatus[0]);
                        faceFlag = -1;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public int initAndLoginHK(String sDeviceIP, Map<String, String> map) {
        // TODO Auto-generated method stub

        HCNetSDK.NET_DVR_USER_LOGIN_INFO struLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        HCNetSDK.NET_DVR_DEVICEINFO_V40 struDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        Pointer PointerstruDeviceInfoV40 = struDeviceInfo.getPointer();
        Pointer PointerstruLoginInfo = struLoginInfo.getPointer();
        String passwordFieldPwd = map.get(Constants.password);
        String sUserName = map.get(Constants.username);
        int iPort = 8000;
        for (int i = 0; i < sDeviceIP.length(); i++) {
            struLoginInfo.sDeviceAddress[i] = (byte) sDeviceIP.charAt(i);
        }
        for (int i = 0; i < passwordFieldPwd.length(); i++) {
            struLoginInfo.sPassword[i] = (byte) passwordFieldPwd.charAt(i);
        }
        for (int i = 0; i < sUserName.length(); i++) {
            struLoginInfo.sUserName[i] = (byte) sUserName.charAt(i);
        }
        struLoginInfo.wPort = (short) iPort;
        struLoginInfo.write();
        lUserID = hCNetSDK.NET_DVR_Login_V40(PointerstruLoginInfo, PointerstruDeviceInfoV40);
        if (lUserID < 0) {
            logger.error("注册失败，失败号：" + hCNetSDK.NET_DVR_GetLastError());
        }
        return lUserID;
    }

    /**
     * 海景设备人脸下发
     *
     * @param deviceIp 设备Ip
     * @param map      公司员工参数
     * @param photo    员工照片
     * @return
     */
    public boolean sendFaceHJ(String deviceIp, Map map, String photo) {
        // TODO Auto-generated method stub
        JSONObject paramsJson = new JSONObject();
        String URL = "http://" + deviceIp + ":8080/office/addOrDelUser";
        String option = map.get(Constants.currentStatus).equals("在职") ? "save" : "delete";
        paramsJson.put("name", AccordingToName((String) map.get(Constants.userName)));
        paramsJson.put("idCard", map.get(Constants.idNo));
        paramsJson.put("op", option);
        if ("save".equals(option)) {
            paramsJson.put("type", "staff");
            paramsJson.put("imageFile", photo);
        }

        StringEntity entity = new StringEntity(paramsJson.toJSONString(), "UTF-8");
        ThirdResponseObj thirdResponseObj = null;
        entity.setContentType("aaplication/json");
        try {
            thirdResponseObj = HttpUtil.http2Se(URL, entity, "UTF-8");
        } catch (Exception e) {
            logger.error(URL);
            return false;
        }
        FaceDevResponse faceResponse = JSON.parseObject(thirdResponseObj.getResponseEntity(), FaceDevResponse.class);

        if ("001".equals(faceResponse.getResult())) {
            logger.info(map.get(Constants.userName) + "人脸下发成功");
            return true;
        } else {
            logger.error(map.get(Constants.userName) + "人脸下发失败，失败原因：" + faceResponse.getMessage());
            return false;
        }
    }

    /**
     * 海景设备人脸删除
     *
     * @param deviceIp 设备Ip
     * @param map      公司员工参数
     * @param photo    员工照片
     * @return
     */
    public boolean deleteFaceHJ(String deviceIp, Map map, String photo) {
        // TODO Auto-generated method stub
        JSONObject paramsJson = new JSONObject();
        String URL = "http://" + deviceIp + ":8080/office/addOrDelUser";
        String option = map.get(Constants.currentStatus).equals("normal") ? "save" : "delete";
        paramsJson.put("name", AccordingToName((String) map.get(Constants.userName)));
        paramsJson.put("idCard", map.get(Constants.idNo));
        paramsJson.put("op", option);
        if ("save".equals(option)) {
            paramsJson.put("type", "staff");
            paramsJson.put("imageFile", photo);
        }

        StringEntity entity = new StringEntity(paramsJson.toJSONString(), "UTF-8");
        ThirdResponseObj thirdResponseObj = null;
        entity.setContentType("aaplication/json");
        try {
            thirdResponseObj = HttpUtil.http2Se(URL, entity, "UTF-8");
        } catch (Exception e) {
            logger.error(URL);
            return false;
        }
        FaceDevResponse faceResponse = JSON.parseObject(thirdResponseObj.getResponseEntity(), FaceDevResponse.class);

        if ("001".equals(faceResponse.getResult())) {
            logger.info(map.get(Constants.userName) + "人脸删除成功");
            return true;
        } else {
            logger.error(map.get(Constants.userName) + "人脸删除失败，失败原因：" + faceResponse.getMessage());
            return false;
        }
    }

    /**
     * 对下发人脸的员工 添加到数据库
     *
     * @param requestMap
     */
    public boolean addData(Map<String, String> requestMap, TbCompanyuser tc, String photo) {

        String userId = requestMap.get(Constants.userId);               //用户ID
        String userName = requestMap.get(Constants.userName);           //用户姓名
        String idNO = requestMap.get(Constants.idNo);                   //证件号
        String companyFloor = requestMap.get(Constants.companyFloor);   //公司所在楼层

        tc.setCompanyId(0);
        tc.setSectionId(0);
        tc.setUserId(Integer.valueOf(userId));
        tc.setUserName(userName);
        tc.setReceiveDate(getDate());
        tc.setReceiveTime(getTime());
        tc.setRoleType("普通员工");
        tc.setStatus("确认");
        tc.setIdType("身份证");
        tc.setIdNO(idNO);
        tc.setCompanyFloor(companyFloor);
        tc.setCurrentStatus("在职");
        tc.setIsSued("1");
        tc.setPhoto(photo);
        tc.setQrCode(userName+userId+".jpg");
        tc.setCardInfo(cardInfo);
        return tc.save();

    }

    /**
     * 修改人脸数据
     *
     * @param map
     * @param
     * @return
     */
    public int updateData(Map<String, String> map, String photo) {
        String userId = map.get(Constants.userId);               //用户ID
        String userName = map.get(Constants.userName);           //用户姓名
        String idNO = map.get(Constants.idNo);                   //证件号
        String companyFloor = map.get(Constants.companyFloor);   //公司所在楼层

        return Db.update("UPDATE tb_companyuser SET \n" +
                "companyId= ?, sectionId= ?, userId=?, userName=?, receiveDate=?, \n" +
                "receiveTime=?, roleType=?, status=?, idType=?, idNO=?, \n" +
                "companyFloor=?, currentStatus=?, isSued=?, \n" +
                "photo=?, cardInfo=? WHERE companyUserId=?",0,0,Integer.valueOf(userId),userName,
                    getDate(),getTime(),"普通员工","确认","身份证",idNO,companyFloor,"在职","1",
                photo,findCompanyUser(userId).getCardInfo(),map.get(Constants.companyUserId));
    }
    /**
     * 获取当前时间 年月日
     *
     * @return
     */
    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    //获取当前时间 时分秒
    private String getTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }
}
