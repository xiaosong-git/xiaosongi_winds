package com.xiaosong.config;

import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.xiaosong.common.device.DeviceService;
import org.apache.log4j.Logger;

public class InitHCNetSDK {

    private static Logger logger = Logger.getLogger(InitHCNetSDK.class);

    public static void run(String deviceType) {
        // TODO Auto-generated method stub

        //查看大楼的设备选型
        logger.info(deviceType);
        //设备选型是否为海康设备
        systemload();
        HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
        boolean init = hCNetSDK.NET_DVR_Init();
        if(init){
            logger.info("初始化成功");
        }else{
            logger.error("初始化失败");
        }
       /* if (deviceType.contains("DS-K5671") || deviceType.contains("DS-2CD8627FWD")) {

            systemload();
            HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
            //是海康设备就初始化海康SDK
            while (!isInit && count < 5) {
                isInit = hCNetSDK.NET_DVR_Init();
                count++;
            }
            if (!isInit) {
                redisUtils.set("isInit", "false");
                logger.error("海康SDK初始化失败");
            } else {
                redisUtils.set("isInit", "true");
                logger.info("海康SDK初始化成功");
                //选型有海康门禁设备需要启动长连接做继电器开门
                List<TbDevice> devices = devicesService.findByDevName("人脸设备", "使用");
                //查找所有运行的海康设备做长连接
                for (int i = 0; i < devices.size(); i++) {
                    if (devices.get(i).getDeviceType().contains("DS-K5671") || devices.get(i).getDeviceType().contains("DS-2CD8627FWD")) {
                        logger.info("长连接" + devices.get(i).getDeviceIp());
                        hcNetSDKService.sendAccessRecord(devices.get(i).getDeviceIp(), "admin", "wgmhao123");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }*/
    }

    /**
     * 海康SKD加载方式
     */
    public static void systemload() {
        HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

        System.load("/usr/tomcat/apache-tomcat-8.5.43/webapps1/WEB-INF/classes/lib/libHCCore.so");
        System.load("/usr/tomcat/apache-tomcat-8.5.43/webapps1/WEB-INF/classes/lib/libhpr.so");
        System.load("/usr/tomcat/apache-tomcat-8.5.43/webapps1/WEB-INF/classes/lib/libhcnetsdk.so");

        String strPathCom2 = "/usr/tomcat/apache-tomcat-8.5.43/webapps1/WEB-INF/classes/lib/";
        HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath2 = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
        System.arraycopy(strPathCom2.getBytes(), 0, struComPath2.sPath, 0, strPathCom2.length());
        struComPath2.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath2.getPointer());

        HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto = new HCNetSDK.BYTE_ARRAY(256);
        String strPathCrypto = "/usr/tomcat/apache-tomcat-8.5.43/webapps1/WEB-INF/classes/lib/libssl.so";
        System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
        ptrByteArrayCrypto.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto.getPointer());

        HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto2 = new HCNetSDK.BYTE_ARRAY(256);
        String strPathCrypto2 = "/usr/tomcat/apache-tomcat-8.5.43/webapps1/WEB-INF/classes/lib/libcrypto.so.1.0.0";
        System.arraycopy(strPathCrypto2.getBytes(), 0, ptrByteArrayCrypto2.byValue, 0, strPathCrypto2.length());
        ptrByteArrayCrypto2.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto2.getPointer());

        HCNetSDK.BYTE_ARRAY ptrByteArrayCrypto3 = new HCNetSDK.BYTE_ARRAY(256);
        String strPathCrypto3 = "/usr/tomcat/apache-tomcat-8.5.43/webapps1/WEB-INF/classes/lib/libcrypto.so";
        System.arraycopy(strPathCrypto3.getBytes(), 0, ptrByteArrayCrypto3.byValue, 0, strPathCrypto3.length());
        ptrByteArrayCrypto3.write();
        hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArrayCrypto3.getPointer());
    }
}
