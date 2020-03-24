package com.xiaosong.common.device;

import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.model.TbDevicerelated;

import java.util.List;

public class DeviceRelatedService {
    public static final DeviceRelatedService me = new DeviceRelatedService();

    /**
     * 查询所有的 设备使用记录
     * @return
     */
    public List<TbDevicerelated> findRecord() {
        return TbDevicerelated.dao.find("select * from tb_deviceRelated");
    }

    /**
     * 保存设备使用记录
     * @param td 设备使用参数
     */
    public boolean save(TbDevicerelated td) {
        return td.save();
    }

    /**
     * 删除设备使用记录
     * @param relatedId  设备编号
     * @return
     */
    public int delete(String relatedId) {
        return Db.delete("delete from tb_deviceRelated where relatedId = ?", relatedId);
    }

    /**
     * 修改设备使用记录
     * @param td  设备使用参数
     * @return
     */
    public boolean update(TbDevicerelated td) {
        return td.update();
    }

    /**
     * 根据 设备ip 查询 设备使用记录
     * @param faceIP ip地址
     * @return
     */
    public TbDevicerelated findByFaceIP(String faceIP) {
        return TbDevicerelated.dao.findFirst("select * from tb_deviceRelated where faceIP = ? ", faceIP);
    }
}
