package com.xiaosong.common.campus.group;

import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.model.TbGroup;
import com.xiaosong.model.TbPersonnel;

import java.util.List;

public class GroupAdminService {
    public static final GroupAdminService me = new GroupAdminService();

    /**
     * 查询 所有组别
     * @return
     */
    public List<TbGroup> findAll() {
        return TbGroup.dao.findAll();
//        return TbGroup.dao.find("select * from tb_group");
    }

    /**
     * 删除分组信息
     * @param id
     */
    public int delete(String id) {
        return Db.delete("delete from tb_group WHERE id = ?", id);
    }

    /**
     * 查询该分组是否有人员
     * @param id
     */
    public List<TbPersonnel> exist(String id) {
        TbGroup tbGroup = TbGroup.dao.findFirst("select groupName from tb_group where id = ?", id);
        String groupName = tbGroup.getGroupName();
        return TbPersonnel.dao.find("select *  from tb_personnel WHERE groupName = ?", groupName);


    }
}
