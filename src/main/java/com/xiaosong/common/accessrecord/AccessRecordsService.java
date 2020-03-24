package com.xiaosong.common.accessrecord;

import com.xiaosong.model.TbAccessrecord;

import java.util.List;

public class AccessRecordsService {
    public static AccessRecordsService me = new AccessRecordsService();

    /**
     * 查询所有通行记录
     * @return
     */
    public List<TbAccessrecord> findAll() {
        return TbAccessrecord.dao.find("select * from tb_accessrecord");
    }
}
