package com.xiaosong.common.floor;

import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.model.TbBuilding;
import com.xiaosong.model.TbVisitor;

import java.util.List;

public class FloorService {
    public static final FloorService me = new FloorService();

    /**
     * 根据大楼唯一编号删除 大楼配置信息
     *
     * @param orgCode 大楼编号
     */
    public int deleteFloor(String orgCode) {
        return Db.delete("delete from tb_building where orgCode = ?", orgCode);
    }

    /**
     * 查询所有的大楼信息数据
     */
    public TbBuilding findFloor() {
        return TbBuilding.dao.findFirst("select * from tb_building");
    }

    /**
     * 修改大楼配置
     *
     * @param tb 大楼参数
     * @return
     */
    public int update(TbBuilding tb, String code) {
        return Db.update("UPDATE tb_building tb SET tb.orgCode= ? , tb.orgName= ? , tb.pospCode=? , tb.netType=? , tb.faceComparesCope=? , tb.key=? WHERE tb.orgCode=?",
                tb.getOrgCode(),
                tb.getOrgName(),
                tb.getPospCode(),
                tb.getNetType(),
                tb.getFaceComparesCope(),
                tb.getKey(),
                code
        );
    }

    /**
     * 查询大楼的唯一编号
     *
     * @return
     */
    public String findByOrgCode() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select orgCode from tb_building");
        if(tbBuilding!=null){
            return tbBuilding.getOrgCode();
        }else{
            return null;
        }
    }

    /**
     * 查询大楼的设备配置方式（1-二维码，2-人像识别，3-二维码+人像识别，4-二维码或人像识别）
     *
     * @return
     */
    public String findDeviceType() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select accessType from tb_building");
        if (tbBuilding != null) {
            return tbBuilding.getAccessType();
        } else {
            return null;
        }

    }

    /**
     * 查询上位机编码
     * @return
     */
    public String findPospCode() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select accessType from tb_building");
        if (tbBuilding != null) {
            return tbBuilding.getPospCode();
        } else {
            return null;
        }
    }

    /**
     *  查询大楼中 访客的通行方式
     * @return
     */
    public String findVisitorCheckType() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select visitorCheckType from tb_building");
        if (tbBuilding != null) {
            return tbBuilding.getPospCode();
        } else {
            return null;
        }
    }

    /**
     * 修改访客
     * @param visitor 访客数据
     */
    public void updateFloor(TbVisitor visitor) {
        visitor.update();
    }

    /**
     * 查询 大楼 唯一编号
     * @return
     */
    public String findOrgId() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select orgCode from tb_building");
        if(tbBuilding==null){
            return null;
        }else{
            return tbBuilding.getOrgCode();
        }
    }

    /**
     * 查询 大楼的key 值
     * @return
     */
    public String findKey() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select t.key from tb_building t");
        if(tbBuilding==null){
            return null;
        }else{
            return tbBuilding.getKey();
        }
    }

    /**
     * 查询大楼中的 联网方式
     * @return
     */
    public String findNetType() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select t.netType from tb_building t");
        if(tbBuilding==null){
            return null;
        }else{
            return tbBuilding.getNetType();
        }
    }

    /**
     * 查询大楼中 共享方式的通行方式
     * @return
     */
    public String findShareCheckType() {
        TbBuilding tbBuilding = TbBuilding.dao.findFirst("select shareCheckType from tb_building");

        if(tbBuilding==null){
            return null;
        }else{
            return tbBuilding.getShareCheckType();
        }
    }

}
