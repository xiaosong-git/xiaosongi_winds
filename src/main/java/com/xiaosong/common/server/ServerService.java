package com.xiaosong.common.server;

import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.model.*;

import java.net.InetAddress;
import java.util.List;

public class ServerService {

    public static final ServerService me = new ServerService();

    /**
     * 查询服务器的所有id字段
     */
    public List<TbServerinfo> findById() {
        List<TbServerinfo> orgCode = TbServerinfo.dao.find("select id from tb_serverinfo");
        if (orgCode.size() <= 0 || orgCode == null) {
            return null;
        }
        return orgCode;
    }

    /**
     * 保存 服务器
     *
     * @param tbServerinfo 服务器保存 参数
     * @return
     */
    public boolean save(TbServerinfo tbServerinfo) {
        return tbServerinfo.save();

    }

    /**
     * 修改服务器
     *
     * @param tbServerinfo 服务器修改 参数
     * @return
     */
    public int update(TbServerinfo tbServerinfo) {
        TbServerinfo ts = TbServerinfo.dao.findFirst("select id from tb_serverinfo");
        return Db.update("update tb_serverinfo set value1 = ? , serverUrl = ? , serverPort = ? , privateKey = null , codeKey = null , netStatus = ? , mac = null , value2 = ? , serverUrl2 = ? , serverPort2 = ? where id = ?",
                tbServerinfo.getValue1(), tbServerinfo.getServerUrl(), tbServerinfo.getServerPort(), tbServerinfo.getNetStatus(), tbServerinfo.getValue2(), tbServerinfo.getServerUrl2(), tbServerinfo.getServerPort2(), ts.getId());
    }

    /**
     * 根据id 删除服务器
     *
     * @param id id
     */
    public int deleteServer(String id) {
        return Db.delete("delete from tb_serverinfo where id = ?", id);
    }

    /**
     * 查询所有的服务器
     *
     * @return
     */
    public List<TbServerinfo> findServer() {
        return TbServerinfo.dao.find("select * from tb_serverinfo");
    }

    /**
     * 根据ip 判断是否可以ping 成功
     *
     * @param ipAddress ip地址
     * @return
     * @throws Exception
     */
    public boolean ping(String ipAddress) throws Exception {
        if (ipAddress == null || "".equals(ipAddress)) {
            return false;
        }
        int timeOut = 1000; // 超时应该在1秒以上
        boolean status = InetAddress.getByName(ipAddress).isReachable(timeOut);

        // 当返回值是true时，说明host是可用的，false则不可。
        return status;
    }

    /*
     *  条件查询
     */
    public List<TbServerinfo> findByName(String serverName) {
        return TbServerinfo.dao.find("select * from tb_serverinfo where serverName like '%" + serverName + "%'");
    }

    /**
     * 查询一条服务器
     *
     * @return
     */
    public TbServerinfo findSer() {
        return TbServerinfo.dao.findFirst("select * from tb_serverinfo");
    }
}
