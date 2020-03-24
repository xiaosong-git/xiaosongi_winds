package com.xiaosong.common.device;

import com.jfinal.core.Controller;
import com.xiaosong.common.floor.FloorService;
import com.xiaosong.common.service.StaffService;
import com.xiaosong.common.service.VisitorService;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.*;
import com.xiaosong.model.base.QrCodeModel.QRCodeCommonMessage;
import com.xiaosong.util.*;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 人脸设备二维码
 */
public class QRCodeController extends Controller {

    private String TotalPages = "1";
    private StaffService srvUser = StaffService.me;
    private VisitorService srvVisitor = VisitorService.me;
    private DeviceRelatedService srvDevRel = DeviceRelatedService.me;
    private FloorService srvFloor = FloorService.me;
    private DeviceService srvDev = DeviceService.me;
    private CheckUtils checkUtils = new CheckUtils();
    private static Logger logger = Logger.getLogger(QRCodeController.class);

    public void index() throws Exception {
        HttpServletRequest request = getRequest();
        String content = request.getParameter("content");
        System.out.println(content);
        String faceRecogIp = IPUtil.getIp(request);
        // 读取扫描二维码数据

        String splitStrings[] = content.split("\\|");
        String commonMessage = splitStrings[0].trim();
        String[] commonMessageSplit = commonMessage.split("\\&");

        if (splitStrings.length != 2 || commonMessageSplit.length != 5) {
            logger.error("二维码扫描数据错误");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "二维码扫描数据错误"));
        }
        if (!commonMessageSplit[3].equals(TotalPages)) {
            logger.error("二维码扫描数据错误");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "二维码扫描数据错误"));
        }

        // 解析头部分内容
        QRCodeCommonMessage qrCodeCommonMessage = new QRCodeCommonMessage();
        qrCodeCommonMessage.setIdentifier(commonMessageSplit[0]);
        qrCodeCommonMessage.setBitMapType(commonMessageSplit[1]);
        qrCodeCommonMessage.setCurrentPage(commonMessageSplit[2]);
        qrCodeCommonMessage.setTotalPages(commonMessageSplit[3]);
        qrCodeCommonMessage.setViewTime(commonMessageSplit[4]);

        // BASE64加密内容解析
        String strByte = new String(Base64_2.decode(splitStrings[1].trim()), "UTF-8");
        logger.info("BASE64加密内容解析内容:" + strByte);
        List<String> contentStringLists = parseContent(strByte);
        System.out.println(contentStringLists.toString());
        if (qrCodeCommonMessage.getBitMapType().equals("1")) {
            String userName = contentStringLists.get(0);
            String companyId = contentStringLists.get(1);
            String userId = contentStringLists.get(2);
            if (userName.isEmpty() || companyId.isEmpty() || userId.isEmpty()) {
                logger.error("二维码数据空");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "二维码数据空"));
            }
            TbCompanyuser user = srvUser.findByUserId(userId);
            if (null == user) {
                logger.error("用户所在公司不在该大楼");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "用户所在公司不在该大楼"));
            }
            if (!user.getUserName().equals(userName) || user.getCompanyId() != Integer.valueOf(companyId)) {
                logger.error("二维码数据错误，名字不匹配或者公司ID不匹配");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "二维码数据错误，名字不匹配或者公司ID不匹配"));
            } else {
                TbDevicerelated devRelated = srvDevRel.findByFaceIP(faceRecogIp);
                open(devRelated, user.getUserName(), user.getIdNO(), "staff");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_NORMAL, "二维码扫描成功"));
            }
        } else if (qrCodeCommonMessage.getBitMapType().equals("2")) {
            String soleCode = contentStringLists.get(1);
            if (StringUtils.isEmpty(soleCode)) {
                logger.error("用户唯一标识码soleCode为空");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "用户唯一标识码soleCode为空"));
            }
            String startTime = "";
            String endTime = "";

            if (contentStringLists.size() == 4) {
                startTime = contentStringLists.get(2);
                endTime = contentStringLists.get(3);
            } else {
                startTime = contentStringLists.get(9);
                endTime = contentStringLists.get(10);
            }
            List<TbVisitor> visitorInfos = srvVisitor.findByVisitId(soleCode, startTime, endTime);
            System.out.println(visitorInfos.size());
            if (visitorInfos == null || visitorInfos.size() <= 0) {
                logger.error("没有该visitId的访问数据");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "没有该visitId的访问数据"));
            } else {

                TbVisitor visitorInfo = visitorInfos.get(0);
                TbCompanyuser companyUser = srvUser.findByNameAndIdNO(visitorInfo.getByVisitorName(),
                        visitorInfo.getByVisitorIdCard(), "normal");
                if (null == companyUser) {
                    logger.error("员工表无该员工数据信息");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "被访者不是该大楼员工"));
                }
                TbDevicerelated devRelated = srvDevRel.findByFaceIP(faceRecogIp);
                if (devRelated.getTurnOver().equals("out")) {
                    open(devRelated, visitorInfo.getVisitorName(), visitorInfo.getVisitorIdCard(), "visitor");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_NORMAL, "二维码扫描成功"));
                }
                boolean success = checkUtils.verificationCache(visitorInfo);
                if (success) {

                    TbDevice device = srvDev.findByDeviceIp(faceRecogIp);

                    if (null == device) {
                        logger.error("找不到该IP的设备");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "找不到该IP的设备"));
                    } else {

                        String floor = companyUser.getCompanyFloor();
                        if (devRelated.getContralFloor().contains(floor)) {
                            open(devRelated, visitorInfo.getVisitorName(), visitorInfo.getVisitorIdCard(), "visitor");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_NORMAL, "二维码扫描成功"));
                        } else {
                            logger.error("被访问者不在该楼层");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "被访问者不在该楼层"));
                        }

                    }
                } else {
                    logger.error("已过访问有效期");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "已过访问有效期"));
                }
            }

        } else if (qrCodeCommonMessage.getBitMapType().equals("3")) {
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "失败"));
        } else {
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "失败"));
        }
    }

    private void open(TbDevicerelated devRelated, String name, String idCard, String type) throws Exception {

        //继电器 控制
        Control24DeviceUtil.controlDevice(devRelated.getRelayIP(), 8080, devRelated.getRelayOUT(),
                srvFloor.findOrgId());
        //保存记录
        saverecord(name, idCard, type, devRelated.getQRCodeIP(), devRelated.getRelayOUT());
    }

    /**
     * 保存记录
     *
     * @param name       姓名
     * @param idCard     身份证号码
     * @param personType 人员类型
     * @param faceIP     人脸设备ip
     * @param OUT        输出口
     */
    private void saverecord(String name, String idCard, String personType, String faceIP, String OUT) {
        // TODO Auto-generated method stub
        TbDevice device = srvDev.findByDeviceIp(faceIP);
        TbAccessrecord accessRecord = new TbAccessrecord();
        accessRecord.setOrgCode(srvFloor.findOrgId());
        accessRecord.setPospCode(srvFloor.findPospCode());
        accessRecord.setScanDate(getDate());
        accessRecord.setScanTime(getTime());
        accessRecord.setInOrOut(device.getFqTurnover());
        accessRecord.setOutNumber(OUT);
        accessRecord.setDeviceType("QRCODE");
        accessRecord.setDeviceIp(faceIP);
        accessRecord.setUserType(personType);
        accessRecord.setUserName(name);
        accessRecord.setIdCard(idCard);
        accessRecord.setIsSendFlag("F");
        accessRecord.save();
    }

    /*
     * 解析“[]”内容
     *
     * SoleCode(contentStringLists.get(0)) //唯一身份识别码
     * RealName(contentStringLists.get(1)) //访客姓名 IdNO(contentStringLists.get(2))
     * //访客证件号 Province(contentStringLists.get(3)) //访问的省
     * City(contentStringLists.get(4)) //访问的市
     * VisitorCompany(contentStringLists.get(5)) //被访问者公司名字
     * VisitorName(contentStringLists.get(6)) //被访问者名字
     * Phone(contentStringLists.get(7)) //访问者手机号
     * HeadImgUrl(contentStringLists.get(8)) //访问者照片(目前不存照片)
     * StarDate(contentStringLists.get(9)) //访问开始时间
     * EndDate(contentStringLists.get(10)) //访问结束时间
     * UserCompanyId(contentStringLists.get(11)) //访问者的公司ID
     * UserCompanyName(contentStringLists.get(12)) //访问者的公司名字
     */
    public static List<String> parseContent(String content) {
        List<String> ls = new ArrayList<String>();
        Pattern pattern = Pattern.compile("(\\[[^\\]]*\\])");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String s = matcher.group();
            if (s.length() > 2) {
                s = s.substring(1, s.length() - 1);
            } else {
                s = "";
            }
            ls.add(s);
        }
        return ls;
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

}
