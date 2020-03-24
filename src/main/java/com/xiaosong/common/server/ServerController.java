package com.xiaosong.common.server;


import com.jfinal.core.Controller;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.*;
import com.xiaosong.util.*;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 服务器配置
 */
public class ServerController extends Controller {
    public ServerService srv = ServerService.me;
    private static Logger logger = Logger.getLogger(ServerController.class);

    /**
     * 添加服务器
     */
    public void save() {
        try {
            String serverUrl = getPara("serverUrl1");       //连接服务器的地址
            String serverPort = getPara("serverPort1");     //连接服务器的端口
            String value1 = getPara("value1");     //上位机机械码
            String serverUrl2 = getPara("serverUrl2");       //连接服务器的地址1
            String serverPort2 = getPara("serverPort2");     //连接服务器的端口1
            String value2 = getPara("value2");     //上位机机械码1

            List<TbServerinfo> serverinfos = srv.findServer();
            if (serverinfos == null || serverinfos.size() == 0) {
                String netStatus = null;
                String netStatus2 = null;

                if (value1.equals("true")) {

                    if(serverPort.isEmpty()){
                        logger.error("端口为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }else if(serverUrl.isEmpty()){
                        logger.error("地址为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }
                } else {
                    serverPort = "";
                    serverUrl = "";
                }

                if (value2.equals("true")) {
                    if(serverPort2.isEmpty()){
                        logger.error("端口为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }else if(serverUrl2.isEmpty()){
                        logger.error("地址为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }
                } else {
                    serverPort2 = "";
                    serverUrl2 = "";
                }
                boolean bool = srv.ping(serverUrl);
                if (bool) {
                    netStatus = "连接服务器成功";
                } else {
                    netStatus = "连接服务器异常";
                }
                boolean bool2 = srv.ping(serverUrl2);
                if (bool2) {
                    netStatus2 = "连接服务器成功";
                } else {
                    netStatus2 = "连接服务器异常";
                }

                TbServerinfo tbServerinfo = getModel(TbServerinfo.class);
//            tbServerinfo.setId(Integer.valueOf(id));
                tbServerinfo.setServerUrl(serverUrl);
                tbServerinfo.setServerPort(serverPort);
                tbServerinfo.setNetStatus(netStatus);
                tbServerinfo.setValue1(value1);
                tbServerinfo.setServerUrl2(serverUrl2);
                tbServerinfo.setNetStatus2(netStatus2);
                tbServerinfo.setServerPort2(serverPort2);
                tbServerinfo.setValue2(value2);
                //保存服务器
                boolean save = srv.save(tbServerinfo);
                if (save) {
                    logger.info("服务器添加成功");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "服务器添加成功"));
                } else {
                    logger.info("服务器添加失败");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器添加失败"));
                    return;
                }
            } else {
                String netStatus = null;
                String netStatus2 = null;

                if (value1.equals("true")) {

                    if(serverPort.isEmpty()){
                        logger.error("端口为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }else if(serverUrl.isEmpty()){
                        logger.error("地址为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }
                } else {
                    serverPort = "";
                    serverUrl = "";
                }

                if (value2.equals("true")) {
                    if(serverPort2.isEmpty()){
                        logger.error("端口为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }else if(serverUrl2.isEmpty()){
                        logger.error("地址为空");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
                        return;
                    }
                } else {

                    serverPort2 = "";
                    serverUrl2 = "";
                }
                boolean bool = srv.ping(serverUrl2);
                if (bool) {
                    netStatus = "连接服务器成功";
                } else {
                    netStatus = "连接服务器异常";
                }

                boolean bool2 = srv.ping(serverUrl2);
                if (bool2) {
                    netStatus2 = "连接服务器成功";
                } else {
                    netStatus2 = "连接服务器异常";
                }

                TbServerinfo tbServerinfo = getModel(TbServerinfo.class);
//            tbServerinfo.setId(Integer.valueOf(id));
                tbServerinfo.setServerUrl(serverUrl);
                tbServerinfo.setServerPort(serverPort);
                tbServerinfo.setNetStatus(netStatus);
                tbServerinfo.setNetStatus2(netStatus2);
                tbServerinfo.setValue1(value1);
                tbServerinfo.setValue2(value2);
                tbServerinfo.setServerUrl2(serverUrl2);
                tbServerinfo.setServerPort2(serverPort2);
                //保存服务器
                int save = srv.update(tbServerinfo);
                if (save==1) {
                    logger.info("服务器修改成功");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "服务器修改成功"));
                } else {
                    logger.info("服务器修改失败");
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器修改失败"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("服务器异常!");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器异常!"));
        }

    }

    /**
     * 删除服务器
     */
    public void delete() {
        try {
            String id = getPara("id");
            int i = srv.deleteServer(id);
            if (i == 1) {
                logger.info("删除服务器成功.");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "删除服务器成功."));
            } else {
                logger.error("删除服务器失败!");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除服务器失败!"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("服务器异常!");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器异常!"));
        }
    }

    /**
     * 查询所有的服务器
     */
    public void index() {
        try {
            List<TbServerinfo> server = srv.findServer();
            if(server!=null&&server.size()!=0){
                for (TbServerinfo tbServerinfo : server) {
                    if (tbServerinfo != null) {
                        logger.info("服务器信息查询成功~");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, tbServerinfo, server.size()));
                    } else {
                        logger.error("服务器信息查询失败~");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器信息查询失败"));
                    }
                }

            }else{
                logger.error("服务器信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器信息查询失败"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("服务器异常!");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器异常!"));
        }
    }

    /**
     * 测试是否连接成功
     */
    public void ping() throws Exception {
        try {
            String ip = getPara("serverUrl");
            boolean bool = srv.ping(ip);
            if (bool) {
                logger.info("连接成功~");
                renderJson(RetUtil.ok(bool));
            } else {
                logger.error("连接失败~");
                renderJson(RetUtil.fail(bool));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("服务器异常!");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器异常!"));
        }
    }

    /**
     * 条件查询
     */
    public void findByName() {

        try {
            String serverName = getPara("serverName");
            List<TbServerinfo> serverinfos = new ArrayList<>();
            if (serverName != null) {
                logger.info("查询成功~");
                serverinfos = srv.findByName(serverName);
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, serverinfos));
            } else {
                logger.info("查询成功~");
                serverinfos = srv.findServer();
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_ERROR, serverinfos));
            }
        } catch (Exception e) {


            e.printStackTrace();
            logger.error("服务器异常!");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "服务器异常!"));
        }
    }

}
