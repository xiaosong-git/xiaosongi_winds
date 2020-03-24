package com.xiaosong.util;

import com.jfinal.kit.PathKit;
import com.sun.jna.Platform;
import org.apache.log4j.Logger;

import java.io.*;

public class Utils {
    private static Logger log = Logger.getLogger(Utils.class);

    public Utils() {

    }

    // 获取操作平台信息
    public static String getOsPrefix() {
        String arch = System.getProperty("os.arch").toLowerCase();
        final String name = System.getProperty("os.name");
        String osPrefix;
        switch (Platform.getOSType()) {
            case Platform.WINDOWS: {
                if ("i386".equals(arch))
                    arch = "x86";
                osPrefix = "win32-" + arch;
            }
            break;
            case Platform.LINUX: {
                if ("x86".equals(arch)) {
                    arch = "i386";
                } else if ("x86_64".equals(arch)) {
                    arch = "amd64";
                }
                osPrefix = "linux-" + arch;
            }
            break;
            default: {
                osPrefix = name.toLowerCase();
                if ("x86".equals(arch)) {
                    arch = "i386";
                }
                if ("x86_64".equals(arch)) {
                    arch = "amd64";
                }
                int space = osPrefix.indexOf(" ");
                if (space != -1) {
                    osPrefix = osPrefix.substring(0, space);
                }
                osPrefix += "-" + arch;
            }
            break;

        }

        return osPrefix;
    }

    public static String getOsName() {
        String osName = "";
        String osPrefix = getOsPrefix();
        if (osPrefix.toLowerCase().startsWith("win32-x86")
                || osPrefix.toLowerCase().startsWith("win32-amd64")) {
            osName = "win";
        } else if (osPrefix.toLowerCase().startsWith("linux-i386")
                || osPrefix.toLowerCase().startsWith("linux-amd64")) {
            osName = "linux";
        }

        return osName;
    }

    // 获取加载库
    public static String getLoadLibrary(String library) {
        if (isChecking()) {
            return null;
        }

        String loadLibrary = "";
        String osPrefix = getOsPrefix();
        if (osPrefix.toLowerCase().startsWith("win32-x86")) {
//            loadLibrary = (Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "win32/").substring(1);
            loadLibrary = (PathKit.getWebRootPath()+"/WEB-INF/classes/lib/win64/");
        } else if (osPrefix.toLowerCase().startsWith("win32-amd64")) {
//            loadLibrary = (Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "win64/").substring(1);
            loadLibrary = (PathKit.getWebRootPath()+"/WEB-INF/classes/lib/win64/");
        } else if (osPrefix.toLowerCase().startsWith("linux-i386")) {
            loadLibrary = "";
        } else if (osPrefix.toLowerCase().startsWith("linux-amd64")) {
            loadLibrary = "";
        }

        log.info("[Load %s Path : %s]" + library + loadLibrary + library);
        return loadLibrary + library;
    }

    private static boolean checking = false;

    public static void setChecking() {
        checking = true;
    }

    public static void clearChecking() {
        checking = false;
    }

    public static boolean isChecking() {
        return checking;
    }

    public static void main(String[] args) throws IOException {
//        System.out.println(Utils.class.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1));
//        System.out.println(ClassLoader.getSystemResources("win64/").nextElement().getPath());
//        String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "win64/";
//        System.out.println(path);
//        System.out.println(Utils.class.getResource("/win64").toString().substring(6));
        System.out.println((Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "win64/").substring(1));
        System.out.println(System.getProperty("java.library.path"));
        String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println(java.net.URLDecoder.decode(path, "UTF-8"));
        File file = new File("src/main/resources/win64/dhnetsdk.dll");
        try {
            InputStream in = new FileInputStream(file);
            System.out.println(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
