package com.xiaosong.common.campus.group;

import com.jfinal.core.Controller;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbGroup;
import com.xiaosong.model.TbPersonnel;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 分组管理
 */
public class GroupAdminController extends Controller {
    public GroupAdminService srv = GroupAdminService.me;
    private static Logger logger = Logger.getLogger(GroupAdminController.class);

    /**
     * 保存 组别
     */
    public void save() {
        try {
//            String groupName = getRequest().getParameter("groupName");
            String groupName = getPara("groupName");
            String date = getDate();
            TbGroup tbGroup = getModel(TbGroup.class);
            tbGroup.setGroupName(groupName);
            tbGroup.setCreationDate(date);
            boolean save = tbGroup.save();
            if (save) {
                logger.info("保存成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "保存成功"));
            } else {
                logger.error("保存失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "保存失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("保存异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "保存异常~"));
        }
    }

    /**
     * 删除分组信息
     */
    public void delete() {
        try {
            String id = getPara("id");
            List<TbPersonnel> exist = srv.exist(id);
            if (exist.size() > 0 && null != (exist)) {
                logger.error("删除分组信息失败,该分组存在人员信息~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除分组信息失败,该分组存在人员信息~"));
            }else{
                int delete = srv.delete(id);
                if (delete == 1) {
                    logger.info("删除分组信息成功");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "删除分组信息成功"));
                } else {
                    logger.error("删除分组信息失败");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除分组信息失败"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除分组信息异常");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除分组信息异常"));
        }

    }

    /**
     * 批量删除分组信息
     */
    public void batchDel() {
        //获取前台数据
        try {
            String groupId = getPara("id");
            String[] split = groupId.split(",");
            for (String id : split) {
                int i = srv.delete(id);
                if (i == 1) {
                    logger.info("批量删除分组信息成功~");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "批量删除分组信息成功~"));
                } else {
                    logger.error("批量删除分组信息失败~");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "批量删除分组信息失败~"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    /**
     * 修改 分组信息
     */
    public void update() {
        try {
            String id = getPara("id");
            String groupName = getPara("groupName");
            String updateDate = getDate();
            TbGroup tbGroup = getModel(TbGroup.class);
            tbGroup.setId(Integer.valueOf(id));
            tbGroup.setGroupName(groupName);
            tbGroup.setUpdateDate(updateDate);
            boolean update = tbGroup.update();
            if (update) {
                logger.info("修改分组信息成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "修改分组信息成功~"));
            } else {
                logger.info("修改分组信息失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "修改分组信息失败~"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }

    }

    /**
     * 查询 所有组别
     */
    public void index() {
        try {
            List<TbGroup> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = Integer.parseInt(getPara("pageSize"));
            int index = (page - 1) * number;
            List<TbGroup> tbGroupList = srv.findAll();
            for (int i = index; i < tbGroupList.size() && i < (index + number); i++) {
                list.add(tbGroupList.get(i));
            }
            if (tbGroupList != null) {
                logger.info("组别信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, tbGroupList.size()));
            } else {
                logger.error("组别信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "组别信息查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("组别信息查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "组别信息查询异常~"));
        }
    }

    /**
     * 获取当前系统时间 年-月-日  时:分:秒
     *
     * @return
     */
    private String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
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
     * 获取当前系统时间 时:分:秒
     *
     * @return
     */
    private String getTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }
}
