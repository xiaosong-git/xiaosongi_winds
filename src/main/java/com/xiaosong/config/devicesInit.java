package com.xiaosong.config;

import com.dhnetsdk.lib.NetSDKLib;
import com.dhnetsdk.lib.NetSDKLib.*;
import com.dhnetsdk.module.LoginModule;
import com.sun.jna.Pointer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.swing.*;

public class devicesInit {
    private static Logger log = Logger.getLogger(devicesInit.class);
    // 网络连接恢复
    private static HaveReConnect haveReConnect = new HaveReConnect();
    // 设备断线通知回调
    private static DisConnect disConnect = new DisConnect();
    private static boolean isInit=false;

    // 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
    private static class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            log.info("ReConnect Device[%s] Port[%d]\n" + "," + pchDVRIP + "," + nDVRPort);
            // 断线提示
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    log.error("人脸识别服务器断开");
                }
            });
        }
    }

    // 网络连接恢复，设备重连成功回调
    // 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
    private static class HaveReConnect implements fHaveReConnect {
        public void invoke(LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            log.info("ReConnect Device[%s] Port[%d]\n" + "," + pchDVRIP + "," + nDVRPort);

            // 重连提示
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    log.info("人脸识别服务器已连接");
                }
            });
        }
    }

    public static void initDH() {
        //初始化设备
//        NetSDKLib dhSdk = NetSDKLib.NETSDK_INSTANCE;
//        isInit=dhSdk.CLIENT_Init(disConnect,null);
        isInit = LoginModule.init(disConnect, haveReConnect);
        if (isInit) {
            log.info("初始化大华设备SDK成功!");
        } else {
            log.error("初始化大华设备SDK失败!");
        }
    }

    public static void initHC(){
        HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
        //海康设备就初始化海康SDK
        isInit = hCNetSDK.NET_DVR_Init();
        if (!isInit) {
            log.error("海康SDK初始化失败");
        } else {
            log.info("海康SDK初始化成功");
        }
    }

}
