package com.xiaosong.common.campus.personnel;

import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbGroup;
import com.xiaosong.model.TbPersonnel;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 人员管理
 */
public class PersonnelAdminController extends Controller {
    public PersonnelAdminService srv = PersonnelAdminService.me;
    private static Logger logger = Logger.getLogger(PersonnelAdminController.class);

    /**
     * 模糊查询
     */
    public void dim() {
        try {
            String userName = getPara("userName");
            List<TbPersonnel> tbPersonnel = srv.findByUserName(userName);
            if (userName == null) {
                List<TbPersonnel> list = new ArrayList<>();
                int page = Integer.parseInt(getPara("currentPage"));
                int number = Integer.parseInt(getPara("pageSize"));
                int index = (page - 1) * number;
                List<TbPersonnel> tbPersonnelList = srv.findAll();
                for (int i = index; i < tbPersonnelList.size() && i < (index + number); i++) {
                    list.add(tbPersonnelList.get(i));
                }
                if (tbPersonnelList != null) {
                    logger.info("人员信息模糊查询成功~");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, tbPersonnelList.size()));
                } else {
                    logger.error("人员信息模糊查询失败~");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "人员信息模糊查询失败"));
                }
            } else {
                if (tbPersonnel != null) {
                    List<TbPersonnel> list = new ArrayList<>();
                    int page = Integer.parseInt(getPara("currentPage"));
                    int number = Integer.parseInt(getPara("pageSize"));
                    int index = (page - 1) * number;
                    for (int i = index; i < tbPersonnel.size() && i < (index + number); i++) {
                        list.add(tbPersonnel.get(i));
                    }
                    logger.info(list);
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list));
                } else {
                    logger.error("模糊查询人员失败~");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "模糊查询人员失败~"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("模糊查询人员异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "模糊查询人员异常~"));
        }
    }

    /**
     * 获取excel
     */
    public void excel() {
        try {
            UploadFile photo = getFile();
            System.out.println(photo.getUploadPath());
            if (photo != null) {
                logger.info("excel上传成功,上传的excel名" + photo.getFileName());
                renderJson(RetUtil.ok(photo.getFileName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("excel上传失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "excel上传失败~"));
        }
    }

    /**
     * 批量导入人员信息
     */
    public void batchImport() throws FileNotFoundException {
        try {
            String groupName = getPara("groupName");
            String excelName = getPara("excelName");
//            UploadFile uploadFile = getFile();
//            获取原始的文件名
//            String fileName = uploadFile.getFileName();
//            String hz = fileName.substring(fileName.lastIndexOf(".")+1);//文件后缀
//            File file = uploadFile.getFile();
            File file = new File(PathKit.getWebRootPath() + "/file/" + excelName);
            String fileName = file.getName();
            String hz = fileName.substring(fileName.lastIndexOf(".") + 1);//文件后缀

            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
            String str = "";
            if ("xls".equals(hz)) {
                str = srv.importExcel(groupName, fis);
            } else {
                return;
            }
            if ("ok".equals(str)) {
                logger.info("批量导入人员成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "批量导入人员成功~"));
            } else {
                logger.error("批量导入人员失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "批量导入人员失败~"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error("批量导入人员异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "批量导入人员异常~"));
        }

    }

    /**
     * 保存人员信息
     */
    public void save() {
        try {
            String groupName = getPara("groupName");  //组别名称
            String userName = getPara("userName");   //人员姓名
            String dormitory = getPara("dormitory");   //宿舍编号
            String studentNumber = getPara("studentNumber");  //人员学号
            TbPersonnel tbPersonnel = getModel(TbPersonnel.class);
            tbPersonnel.setGroupName(groupName);
            tbPersonnel.setUserName(userName);
            tbPersonnel.setDormitory(dormitory);
            tbPersonnel.setStudentNumber(Integer.valueOf(studentNumber));
            tbPersonnel.setCreationDate(getDate());
            boolean save = tbPersonnel.save();
            if (save) {
                logger.info("人员信息添加成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "人员信息添加成功"));
            } else {
                logger.error("人员信息添加失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "人员信息添加失败"));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("人员信息添加异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "人员信息添加异常~"));
        }
    }

    /**
     * 删除人员信息
     */
    public void delete() {
        try {
            String id = getPara("id");
            int delete = srv.delete(id);
            if (delete == 1) {
                logger.info("删除人员信息成功");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "删除人员信息成功"));
            } else {
                logger.error("删除人员信息失败");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除人员信息失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除人员信息异常");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除人员信息异常"));
        }
    }

    /**
     * 批量删除人员信息
     */
    public void batchDel() {
        //获取前台数据
        try {
            String personnelId = getPara("id");
            String[] split = personnelId.split(",");
            for (String id : split) {
                int i = srv.delete(id);
                if (i == 1) {
                    logger.info("批量删除人员信息成功~");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "批量删除人员信息成功~"));
                } else {
                    logger.error("批量删除人员信息失败~");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "批量删除人员信息失败~"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    /**
     * 修改人员信息
     */
    public void update() {
        try {
            String id = getPara("id");              //id
            String groupName = getPara("groupName");  //组别名称
            String userName = getPara("userName");   //人员姓名
            String dormitory = getPara("dormitory");   //宿舍编号
            String studentNumber = getPara("studentNumber");  //人员学号
            TbPersonnel tbPersonnel = getModel(TbPersonnel.class);
            tbPersonnel.setId(Integer.valueOf(id));
            tbPersonnel.setUserName(userName);
            tbPersonnel.setDormitory(dormitory);
            tbPersonnel.setGroupName(groupName);
            tbPersonnel.setStudentNumber(Integer.valueOf(studentNumber));
            tbPersonnel.setUpdateDate(getDate());
            boolean update = tbPersonnel.update();
            if (update) {
                logger.info("修改人员信息成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "修改人员信息成功~"));
            } else {
                logger.info("修改人员信息失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "修改人员信息失败~"));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }

    }

    /**
     * 查询 所有 人员所属组别
     */
    public void findGroupName() {
        try {
            List<TbGroup> tbGroups = srv.findByGroupName();
            if (tbGroups != null) {
                logger.info("人员所属组别查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, tbGroups));
            } else {
                logger.error("人员所属组别查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "人员所属组别查询失败~"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("人员所属组别查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "人员所属组别查询异常~"));
        }
    }

    /**
     * 查询所有 人员信息
     */
    public void index() {
        try {
            List<TbPersonnel> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = Integer.parseInt(getPara("pageSize"));
            int index = (page - 1) * number;
            List<TbPersonnel> tbPersonnelList = srv.findAll();
            for (int i = index; i < tbPersonnelList.size() && i < (index + number); i++) {
                list.add(tbPersonnelList.get(i));
            }
            if (tbPersonnelList != null) {
                logger.info("人员信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, tbPersonnelList.size()));
            } else {
                logger.error("人员信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "人员信息查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("人员信息查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "人员信息查询异常~"));
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
}
