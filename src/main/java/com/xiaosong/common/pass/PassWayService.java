package com.xiaosong.common.pass;


import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.model.TbBuilding;

public class PassWayService {
    public static final PassWayService me = new PassWayService();

    /**
     * 根据大楼编号修改通行方式
     * @param face     员工通行方式
     * @param qrcode   访客通行方式
     * @param fq     共享通行方式
     * @return
     */
    public int updateByPassWay(String face, String qrcode, String fq,String orgCode) {
       return Db.update("update tb_building set staffCheckType = ? ,visitorCheckType = ? , shareCheckType =? where orgCode = ?",
               face,qrcode,fq,orgCode);
    }

    /**
     * 查询大楼编号
     */
    public TbBuilding findOrgCode() {
        return TbBuilding.dao.findFirst("select orgCode from tb_building");
    }

    /**
     * 查询通行方式
     */
    public TbBuilding findByPassWay(String orgCode) {
        return TbBuilding.dao.findFirst("select tb.visitorCheckType,tb.staffCheckType,tb.shareCheckType from tb_building tb where tb.orgCode = ?", orgCode);
    }
}
