package com.xiaosong.constant;

import com.jfinal.kit.PathKit;

public class Constants {

    public static String login = "login";             //登录

    public static String deviceIp = "deviceIp";             //设备ip
    public static Integer devicePort = 37777;               //设备端口
    public static String username = "username";             //用户姓名
    public static String password = "password";             //用户密码


    public static String recordNo = "recordNo";             //记录集编号
    public static String idNo = "idNo";                     //用户身份证号码
    public static String CardInfo = "CardInfo";             //卡信息集 大华设备参数
    public static String cardNo = "cardNo";                 //卡号
    public static String userId = "userId";                 //用户ID
    public static String userName = "userName";             //卡名
    public static String cardPwd = "cardPwd";               //卡密码
    public static String cardStatus = "cardStatus";         //卡状态
    public static String cardType = "cardType";             //卡类型
    public static String useTimes = "useTimes";             //使用次数
    public static String isFirstEnter = "isFirstEnter";     //是否首卡, 1-true, 0-false
    public static String isValid = "isValid";               //是否有效, 1-true, 0-false
    public static String startValidTime = "startValidTime"; //有效开始时间
    public static String endValidTime = "endValidTime";     //有效结束时间
    public static String photoPath = "photoPath";           //照片地址
    public static String companyUserId = "companyUserId";   //公司id  *  海康参数
    public static String currentStatus = "currentStatus";   //用户状态 * 海景参数
    public static String type ="type";                      //人员类型
    public static String sectionId ="sectionId";            //部门ID
    public static String status ="status";                  //证件状态
    public static String idType ="idType";                  //证件类型
    public static String companyFloor ="companyFloor";      //公司楼层


    public static String AccessRecPath = "E:\\Recored";    //数据记录路径


    public static final String baseURl = "http://121.36.45.232:8082/visitor/";	//生产接口
//    public static final String baseURl = "http://121.36.45.232:8082/service/";	//测试接口
    public static String newpullStaffUrl = "foreign/newFindOrgCode";		    //新的访客拉取接口
    public static final String page = "1";
    public static final String baseFileURl = "http://47.98.205.206:8081/";
    public static final int PAGENUMBER = 10;
    public static String VisitorPath = PathKit.getWebRootPath() + "/img/";     //Linux系统文件存储路径 访客
    public static String newconfirmReceiveUrl = "foreign/newFindOrgCodeConfirm"; //新访客数据确认接收接口
    public static String StaffPath = PathKit.getWebRootPath() + "/img/";         //Linux系统文件存储路径 员工
    public static String pullOrgCompanyUrl = "companyUser/newFindApplyAllSucOrg";		//新增员工接口
    public static String accessRecordByBatch = "goldccm-imgServer/inAndOut/save";
    public static String shareRoom = "meeting/getFromOrgCode/1/10";		//共享茶室等接口
    public static String confirmShareRoom = "meeting/getFromOrgCodeConfirm";



}
