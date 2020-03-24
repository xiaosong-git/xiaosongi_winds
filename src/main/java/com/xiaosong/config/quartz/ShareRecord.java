package com.xiaosong.config.quartz;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.common.floor.FloorService;
import com.xiaosong.common.server.ServerService;
import com.xiaosong.config.QRCodeModel.ShareRoomDataResponse;
import com.xiaosong.config.QRCodeModel.ShareRoomResponse;
import com.xiaosong.constant.Constants;
import com.xiaosong.model.TbServerinfo;
import com.xiaosong.model.TbShareroom;
import com.xiaosong.util.OkHttpUtil;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * 定时获取共享数据
 */
public class ShareRecord implements Job{
    private FloorService srvFloor = FloorService.me; //大楼业务层
    private ServerService srvServer = ServerService.me; //服务器业务层
    private static Logger logger = Logger.getLogger(ShareRecord.class);
    OkHttpUtil okHttpUtil = new OkHttpUtil();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        getShareInfo();
    }

    private void getShareInfo() {
        // TODO Auto-generated method stub
        //共享数据需要传参，大楼编码，上位机编码
        String orgCode = srvFloor.findOrgId();
        String pospCode = srvFloor.findPospCode();
        // 拉取的数据地址
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(Constants.baseURl);
        TbServerinfo tbServerinfo = srvServer.findSer();
        stringBuilder.append("http://"+tbServerinfo.getServerUrl()+":"+tbServerinfo.getServerPort()+"/service/");

        stringBuilder.append(Constants.shareRoom);
        String url = stringBuilder.toString();
        Map<String,String> params = new HashMap<>();
        params.put("orgCode", orgCode);
        params.put("pospCode", pospCode);

        String responseContent = "";
        try {
            //API端返回数据
            responseContent = okHttpUtil.post(url, params);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(responseContent);
        if (responseContent.isEmpty()) {
            logger.error("访问数据获取错误");
            return;
        }
        // 返回数据转成json模式
        ShareRoomResponse shareRoomResponse = JSON.parseObject(responseContent,ShareRoomResponse.class);

        //System.out.println(shareRoomResponse.getData());
        ShareRoomDataResponse shareRoomData = shareRoomResponse.getData();

        if(null==shareRoomData.getRows()||shareRoomData.getRows().size() < 1) {
            logger.error("无共享数据内容");
        }else {
            for(TbShareroom shareRoom :shareRoomData.getRows()) {
                //拉取数据是否在本地已储存
//                TbShareroom exitShareRoom = shareRoomService.findByRecordId(shareRoom.getRecordId());
                TbShareroom exitShareRoom = TbShareroom.dao.findFirst("select * from tb_shareroom where recordId = ?",shareRoom.getRecordId());
                if(null == exitShareRoom) {

                    //共享会议室茶室等验证方式，二维码不做人脸下发
                    if(srvFloor.findShareCheckType().equals("QRCODE")) {
                        shareRoom.setIsSued("0");
                    }else {
                        shareRoom.setIsSued("1");
                    }
                    shareRoom.setDelFlag("1");

                }else {

                    if(null != exitShareRoom.getIsSued()) {
                        shareRoom.setIsSued(exitShareRoom.getIsSued());
                    }
                    shareRoom.setDelFlag(exitShareRoom.getDelFlag());

                }
                shareRoom.save();
                String response = confirmReceiveData(orgCode, pospCode, String.valueOf(shareRoom.getRecordId()));
                if(response.contains("success")) {
                    Db.update("update tb_shareroom set isFlag = 'T' where recordId = ?",shareRoom.getRecordId());
                }

            }

        }

        //System.out.println(shareRoomData.getRows().size());
    }

    // 向云端发送确认收取
    private String confirmReceiveData(String towerNumber,String pospCode,String idStr) {
        System.out.println("idStr:"+idStr);
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append(Constants.baseURl);
        TbServerinfo tbServerinfo = srvServer.findSer();
        stringBuilder.append("http://"+tbServerinfo.getServerUrl()+":"+tbServerinfo.getServerPort()+"/service/");

        stringBuilder.append(Constants.confirmShareRoom);
        String url = stringBuilder.toString();
        Map<String,String> params = new HashMap<>();
        params.put("idStr", idStr);
        params.put("orgCode", towerNumber);
        params.put("pospCode", pospCode);
        String response ="fail";
        try {
            response = okHttpUtil.post(url, params);
            logger.info("云端确认共享接口收取返回值："+response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("云端确认共享数据失败");
        }
        return response;
    }

}
