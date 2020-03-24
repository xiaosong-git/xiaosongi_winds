package com.xiaosong.config.quartz;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.xiaosong.common.device.DeviceService;
import com.xiaosong.config.SendAccessRecord;
import com.xiaosong.model.TbDevice;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时获取网络摄像头通行记录
 */
public class HCRecord implements Job{
    private DeviceService srvDevice = DeviceService.me;
    private Cache cache = Redis.use("xiaosong");
    private SendAccessRecord sendAccessRecord = new SendAccessRecord();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // TODO Auto-generated method stub
        //查找所有运行的人脸设备
        List<TbDevice> devices = srvDevice.findByDevName("人脸设备", "使用");
        for(TbDevice device: devices) {

            //型号是DS-2CD8627FWD（网络摄像头）的设备
            if(device.getDeviceType().equals("DS-2CD8627FWD")) {


                Map<String,String> map = new HashMap<>();
                String IPCDateRedis = cache.get("IPC"+device.getDeviceIp());
                if(null != IPCDateRedis) {
                    map = JSON.parseObject(IPCDateRedis,map.getClass());
                }

                map.put(getYesterdayByDate(), "false");
                sendAccessRecord.initAndLogin(device.getDeviceIp(),"admin","wgmhao123");

                for(String key : map.keySet()){
                    if(map.get(key).equals("false")) {
                        boolean result = sendAccessRecord.getIPCRecord(device.getDeviceIp(), key);
                        if(result) {
                            map.remove(key);
                        }
                    }
                }
                String strMap = JSON.toJSONString(map);
                cache.set("IPC"+device.getDeviceIp(),strMap);
            }
        }

    }
    public String getYesterdayByDate() {
        // 实例化当天的日期
        Date today = new Date();
        // 用当天的日期减去昨天的日期
        Date yesterdayDate = new Date(today.getTime() - 86400000L);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(yesterdayDate);
        return yesterday;
    }
}
