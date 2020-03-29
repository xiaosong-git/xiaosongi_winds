package com.xiaosong.common.campus.statement;

import com.jfinal.core.Controller;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbPersonnel;
import com.xiaosong.model.TbStatement;
import com.xiaosong.util.RetUtil;
import com.xiaosong.util.XLSFileKit;
import org.apache.log4j.Logger;

import java.io.File;
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
            String groupName = getPara("groupName");

            // 导出`Excel`名称
            String fileName = groupName+"_"+getDate() + ".xls";

            // excel`保存路径
            String filePath = getRequest().getRealPath("/") + "/file/export/";
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String relativePath = "/file/export/" + fileName;
            filePath += fileName;
            XLSFileKit xlsFileKit = new XLSFileKit(filePath);
            List<List<Object>> content = new ArrayList<List<Object>>();
            List<String> title = new ArrayList<String>();

            List<TbStatement> tbStatements = srv.findByGroupName(groupName);
            // 添加`title`,对应的从数据库检索出`datas`的`title`
            title.add("序号");
            title.add("姓名");
            title.add("学号");
            title.add("组别");
            title.add("体温");
            title.add("体温是否正常");
            title.add("时间");
            int i = 0;
            OK:
            while (true) {
                if (tbStatements.size() < (i + 1)) {
                    break OK;
                }
                // 判断单元格是否为空，不为空添加数据
                int index = i + 1;
                List<Object> row = new ArrayList<Object>();
                row.add(index + "");
    //            row.add(null == tbStatements.get(i).getId() ? "" : tbStatements.get(i).getId());
                row.add(null == tbStatements.get(i).getUserName() ? "" : tbStatements.get(i).getUserName());
                row.add(null == tbStatements.get(i).getStudentNumber() ? "" : tbStatements.get(i).getStudentNumber());
                row.add(null == tbStatements.get(i).getGroupName() ? "" : tbStatements.get(i).getGroupName());
                row.add(null == tbStatements.get(i).getAnimalHeat() ? "" : tbStatements.get(i).getAnimalHeat());
                row.add(null == tbStatements.get(i).getStatus() ? "" : tbStatements.get(i).getStatus());
                row.add(null == tbStatements.get(i).getDate() ? "" : tbStatements.get(i).getDate());
                content.add(row);
                i++;
            }
            xlsFileKit.addSheet(content, groupName, title);
            boolean save = xlsFileKit.save();
            if(save){
                logger.info("报表导出成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "报表导出成功~"));
                File file1 = new File(getRequest().getRealPath("/") + "/file/export/" + groupName + "_" + getDate() + ".xls");
                renderFile(file1);
            }else{
                logger.error("报表导出失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表导出失败~"));
            }
//        renderJson(new Record().set("relativePath", relativePath));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("报表导入异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表导入异常~"));
        }
    }

    /**
     * 查询所有 的 报表信息
     */
    public void findAll(){
        try {
            List<TbStatement> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = Integer.parseInt(getPara("pageSize"));
            int index = (page - 1) * number;
            List<TbStatement> tbStatementList = srv.findAll();
            for (int i = index; i < tbStatementList.size() && i < (index + number); i++) {
                list.add(tbStatementList.get(i));
            }
            if (tbStatementList != null) {
                logger.info("报表信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, tbStatementList.size()));
            } else {
                logger.error("报表信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表信息查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("报表信息查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表信息查询异常~"));
        }
    }

    public void dim(){
        try {
            String userName = getPara("userName");
            TbStatement tbPersonnel = srv.findByUserName(userName);
            if (userName == null) {
                List<TbStatement> list = new ArrayList<>();
                int page = Integer.parseInt(getPara("currentPage"));
                int number = Integer.parseInt(getPara("pageSize"));
                int index = (page - 1) * number;
                List<TbStatement> tbPersonnelList = srv.findAll();
                for (int i = index; i < tbPersonnelList.size() && i < (index + number); i++) {
                    list.add(tbPersonnelList.get(i));
                }
                if (tbPersonnelList != null) {
                    logger.info("报表信息模糊查询成功~");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, tbPersonnelList.size()));
                } else {
                    logger.error("报表信息模糊查询失败~");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "报表信息模糊查询失败~"));
                }
            } else {
                if (tbPersonnel != null) {
                    logger.info(tbPersonnel);
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, tbPersonnel));
                } else {
                    logger.error("模糊查询报表失败~");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "模糊查询报表失败~"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("模糊查询报表异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "模糊查询报表异常~"));
        }
    }
    /**
     * 获取当前系统时间 年-月-日
     * @return
     */
    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }
}

