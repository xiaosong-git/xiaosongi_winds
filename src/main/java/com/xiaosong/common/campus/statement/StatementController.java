package com.xiaosong.common.campus.statement;

import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbPersonnel;
import com.xiaosong.model.TbTemperatureRecord;
import com.xiaosong.util.RetUtil;
import com.xiaosong.util.XLSFileKit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 报表统计
 */
public class StatementController extends Controller {
    public StatementService srv = StatementService.me;
    private static Logger logger = Logger.getLogger(StatementController.class);

    /**
     * 报表下载
     */
    public void index() {
        try {
            String userName = getPara("userName");      //人员姓名
            String status = getPara("status");          //状态
            String groupName = getPara("groupName");  //班级
            String dormitory = getPara("dormitory");  //宿舍
            String beginTime = getPara("beginTime");  //开始时间
            String endTime = getPara("endTime");      //结束时间
            String name = "通行人员报表_";
//            if (userName == null && status == null && groupName == null && dormitory == null & beginTime == null && endTime == null) {
//                name = "通行人员报表_";
//            }
//            if (userName != null) {
//                name += userName + "_";
//            }
//            if (status != null) {
//                name += "体温正常"  + "_";
//            }
//            if (groupName != null) {
//                name += groupName + "_";
//            }
//            if (dormitory != null) {
//                name += "宿舍"+dormitory+"_";
//            }
//            if (beginTime != null) {
//                name += getDate();
//            }
//            if (endTime != null) {
//                name += getDate();
//            }

            // 导出`Excel`名称
            String fileName = name + ".xls";

            // excel`保存路径
            String filePath = getRequest().getRealPath("/") + "/file/export/";
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            filePath += fileName;
            XLSFileKit xlsFileKit = new XLSFileKit(filePath);
            List<List<Object>> content = new ArrayList<List<Object>>();
            List<String> title = new ArrayList<String>();
            // 添加`title`,对应的从数据库检索出`datas`的`title`
            title.add("序号");
            title.add("姓名");
            title.add("学号");
            title.add("班级");
            title.add("体温");
            title.add("时间");
            //根据组名查询人员信息
            List<TbTemperatureRecord> tbStatementList = srv.findByBim(userName, status, groupName, dormitory, beginTime, endTime);
            int i = 0;
            OK:
            while (true) {
                if (tbStatementList.size() < (i + 1)) {
                    break OK;
                }
                // 判断单元格是否为空，不为空添加数据
                int index = i + 1;
                List<Object> row = new ArrayList<Object>();
                row.add(index + "");
                //row.add(null == tbStatements.get(i).getId() ? "" : tbStatements.get(i).getId());
                row.add(null == tbStatementList.get(i).get("name1") ? "" : tbStatementList.get(i).get("name1"));
                row.add(null == tbStatementList.get(i).get("studentNumber") ? "" : tbStatementList.get(i).get("studentNumber"));
                row.add(null == tbStatementList.get(i).get("groupName") ? "" : tbStatementList.get(i).get("groupName"));
                row.add(null == tbStatementList.get(i).get("temperature") ? "" : tbStatementList.get(i).get("temperature"));
                row.add(null == tbStatementList.get(i).get("date") ? "" : tbStatementList.get(i).get("date"));
                content.add(row);
                i++;
            }

            xlsFileKit.addSheet(content, "通行人员报表", title);
            boolean save = xlsFileKit.save();
            if (save) {
                logger.info("报表导出成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "报表导出成功~"));
                File file1 = new File(getRequest().getRealPath("/") + "/file/export/" + name + ".xls");
                renderFile(file1);
            } else {
                logger.error("报表导出失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表导出失败~"));
            }
//        renderJson(new Record().set("relativePath", relativePath));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("报表导出异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表导出异常~"));
        }
    }

    /**
     * 查询所有 的 报表信息
     */
//    public void findAll() {
//        try {
//            List<Object> list = new ArrayList<>();
//            int page = Integer.parseInt(getPara("currentPage"));
//            int number = Integer.parseInt(getPara("pageSize"));
//            int index = (page - 1) * number;
//            List<TbPersonnel> tbPersonnels = srv.findByPerName();
//            for (int i = index; i < tbPersonnels.size() && i < (index + number); i++) {
//                list.add(tbPersonnels.get(i));
//            }
//
//
//            if (list != null) {
//                logger.info("报表信息查询成功~");
//                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, tbPersonnels.size()));
//            } else {
//                logger.error("报表信息查询失败~");
//                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表信息查询失败"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("报表信息查询异常~");
//            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表信息查询异常~"));
//        }
//    }

    /**
     * 报表分页模糊查询
     */
    public void bim() {
        try {
            String userName = getPara("userName");      //人员姓名
            String status = getPara("status");      //状态
            String groupName = getPara("groupName");  //班级
            String dormitory = getPara("dormitory");  //宿舍
            String beginTime = getPara("beginTime");  //开始时间
            String endTime = getPara("endTime");  //结束时间
            List<TbTemperatureRecord> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));  //当前页
            int number = Integer.parseInt(getPara("pageSize"));   //一页显示数量
            int index = (page - 1) * number;
            List<TbTemperatureRecord> tbStatementList = srv.findByBim(userName, status, groupName, dormitory, beginTime, endTime);
            for (int i = index; i < tbStatementList.size() && i < (index + number); i++) {
                list.add(tbStatementList.get(i));
            }
            if (tbStatementList != null) {
                logger.info("报表分页模糊查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, tbStatementList.size()));
            } else {
                logger.error("报表分页模糊查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表分页模糊查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("模糊查询报表异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "模糊查询报表异常~"));
        }
    }

    //测试 上传云端数据
    public void aa() {
        try {
            String userName = getPara("userName");
            String studentNumber = getPara("studentNumber");
            String groupName = getPara("groupName");
            String animalHeat = getPara("animalHeat");
            String status = getPara("status");
            String dormitory = getPara("dormitory");
            String isAccommodation = getPara("isAccommodation");
            String date = getPara("date");
            String SWJCode = getPara("swjCode");
            System.out.println(
                    "userName" + userName +
                            "studentNumber" + studentNumber +
                            "groupName" + groupName +
                            "animalHeat" + animalHeat +
                            "status" + status +
                            "dormitory" + dormitory +
                            "isAccommodation" + isAccommodation +
                            "date" + date +
                            "SWJCode" + SWJCode
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前系统时间 年-月-日
     *
     * @return
     */
    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 获取 本地mac地址
     *
     * @return
     * @throws Exception
     */
    private static String getLocalMac() throws Exception {
        // TODO Auto-generated method stub
        //得到IP
        InetAddress ia = InetAddress.getLocalHost();
        //获取网卡，获取地址
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //字节转换为整数
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0" + str);
            } else {
                sb.append(str);
            }
        }
        logger.info("本机MAC地址:" + sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

    /**
     * 毫秒转换成 时间
     *
     * @param millis
     * @return
     */
    public String date(long millis) {
        Date date2 = new Date();
        date2.setTime(millis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fmDate = simpleDateFormat.format(date2);
        return fmDate;
    }

    @Test
    public void test() {
        Date date2 = new Date();
        date2.setTime(1587539554296L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(date2));

    }


    public void test1() throws IOException {
        int statusCode = 0;
        List<TbPersonnel> perAll = srv.findPerAll();
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        if (perAll != null) {
            for (TbPersonnel tbPersonnel : perAll) {
                StringBuffer params = new StringBuffer();
                params.append("userName=" + URLEncoder.encode(tbPersonnel.getUserName(), "UTF-8"));
                params.append("&");
                params.append("studentNumber=" + URLEncoder.encode(tbPersonnel.getStudentNumber().toString(), "UTF-8"));
                params.append("&");
                params.append("groupName=" + URLEncoder.encode(tbPersonnel.getGroupName(), "UTF-8"));
                params.append("&");
                params.append("dormitory=" + URLEncoder.encode(tbPersonnel.getDormitory(), "UTF-8"));
                params.append("&");
                params.append("isAccommodation=" + URLEncoder.encode(tbPersonnel.getIsAccommodation(), "UTF-8"));
                params.append("&");
                params.append("swjCode=" + URLEncoder.encode("hcsyg101", "UTF-8"));
                // 创建Post请求
                HttpPost httpPost = new HttpPost("http://192.168.0.34:8080/per/addPersonal" + "?" + params);
                // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                // 响应模型
                CloseableHttpResponse response = null;
                try {
                    // 由客户端执行(发送)Post请求
                    response = httpClient.execute(httpPost);
                    // 从响应模型中获取响应实体
                    HttpEntity responseEntity = response.getEntity();

                    statusCode = response.getStatusLine().getStatusCode();
                    logger.info("响应状态为:" + response.getStatusLine());
                    if (responseEntity != null) {
                        logger.info("响应内容长度为:" + responseEntity.getContentLength());
                        logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                    }
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        // 释放资源
                        if (httpClient != null) {
//                                httpClient.close();
                        }
                        if (response != null) {
//                                response.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (statusCode == 200) {
            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "成功"));
        } else {
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "失败"));
        }
    }
}
