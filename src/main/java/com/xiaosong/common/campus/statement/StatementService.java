package com.xiaosong.common.campus.statement;

import com.xiaosong.model.TbPersonnel;
import com.xiaosong.model.TbStatement;

import java.util.List;

public class StatementService {
    public static final StatementService me = new StatementService();

    /**
     *  根据 组名查询 人员信息
     */
    public List<TbStatement> findByGroupName(String groupName) {
        return TbStatement.dao.find("select * from tb_statement where groupName = ?", groupName);
    }

    /**
     * 查询所有 的 报表信息
     * @return
     */
    public List<TbStatement> findAll() {
        return TbStatement.dao.find("select * from tb_statement");
    }

    public TbStatement findByUserName(String userName) {
        return TbStatement.dao.findFirst("select *  from tb_statement WHERE userName = ?", userName);
    }
}
