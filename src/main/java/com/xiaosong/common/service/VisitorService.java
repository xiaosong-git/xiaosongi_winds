package com.xiaosong.common.service;

import com.xiaosong.model.TbVisitor;

import java.util.List;

public class VisitorService {
    public static VisitorService me = new VisitorService();

    /**
     *  根据 下发标识 查询 访客
     * @param isSued  下发成功标识
     * @return
     */
    public List<TbVisitor> findByIssued(String isSued) {
        return TbVisitor.dao.find("select * from tb_visitor where isSued = ?" ,isSued);
    }

    /**
     * 保存访客数据
     * @param tbVisitor 访客参数
     */
    public void save(TbVisitor tbVisitor) {
        tbVisitor.save();
    }

    /**
     * 根据 访客id 查询访客数据
     * @param visitorUUID 访客id
     * @return
     */
    public TbVisitor findByUUID(String visitorUUID) {
        return TbVisitor.dao.findFirst("select * from tb_visitor where id = ?", visitorUUID);
    }

    /**
     * 根据下发标识 查询 访客数据
     * @return
     */
    public List<TbVisitor> findByGoneDay() {
        return TbVisitor.dao.find("select * from tb_visitor where NOW() > endDateTime and isSued = '0' and delflag = '1'");
    }

    /**
     * 根据 访客姓名 和访客 身份证 查询 访客数据
     * @param visitorName    访客姓名
     * @param visitorIdCard  访客身份证
     * @return
     */
    public List<TbVisitor> findByVisitor(String visitorName, String visitorIdCard) {
        return TbVisitor.dao.find("select * from tb_visitor where visitorName = ? and visitorIdCard = ? and delFlag = '1'",visitorName,visitorIdCard);
    }

    /**
     * 根据 访客身份证号码 查询访客信息
     * @param idCard  访客身份证号码
     * @return
     */
    public TbVisitor findVisitorId(String idCard) {
        return TbVisitor.dao.findFirst("select * from tb_visitor where visitorIdCard = ?",idCard);
    }

    /**
     * 根据条件查询 访客数据
     * @param soleCode 访客码
     * @param startDateTime 访问开始时间
     * @param endDateTime 访问结束时间
     * @return
     */
    public List<TbVisitor> findByVisitId(String soleCode, String startDateTime, String endDateTime) {
        return TbVisitor.dao.find("select * from tb_visitor where soleCode = ? and startDateTime = ? and endDateTime = ? ", soleCode, startDateTime, endDateTime);
    }
}
