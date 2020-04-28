package com.xiaosong.common.licence;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.interceptor.LicenseInterceptor;
import com.xiaosong.interceptor.XiaoSongInterceptor;
import com.xiaosong.model.TbLicense;
import com.xiaosong.util.Misc;
import com.xiaosong.util.RetUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * licence 管理
 */

@Clear
@Before(LicenseInterceptor.class)
public class LicenseController extends Controller {
    private static LicenseService srv = LicenseService.me;

    private static String resriviceKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCCxQUVuE0dK/PFgnMfE3ebceyqT7HKR07uoW1eH9wVvqIkQ54DrSIxZwZmnxK6JmPdvkRa1hiZYimteBnJTv7Fejiuy+eeredZum60PSPpl0IFn5KuzI64Pyh7h81FyT8/9a/HUG0Kl5S+iZPK/FS8kqno4fHBR9CV4wOeZEvpASB7O49g2SbHgz0r3QNFNYjEcHJKZVF2AUINqSCZORCR1c2tZ94ZfKd+640cC/cVnfiqNgnr6Veg6R3R9tDm2nxjgDQtkRlAwHZTtOhfvd5aUGST6A6mOqEFDpePwbLgcdKAfcD3PyCWVX9/kQFa7sshqCWLztB/CPdTQJmCDDuDAgMBAAECggEAFd59CzTXMJm5Gr7GIdxPlF9pBNJvGTTzrlSwxZzuYXSQCSCFvHen0Ds6hs7zgs4IxXycUdScKIqSDcCYD2J2mO/nqtPO2Gqc1hSnjtzqtJXnJKmbDkCHXaiyTHZZjeIqAbhEvUBl++99oJ/BZ6DrSt38LEYc+AXajDZ61HJRf5XB6SRF/ihT8oLgVbTciWaNo8e4pnR576v/SUHlpJvU6FD2gC8ZJzTzfG9xjfPH/M3a4c53EhbouNVam4CwDQ+eVPUyu6KHL6g9yd5uTUqaU4OUNr0y/UUoyAGXo8LFw4sO6kEURoR2C2+HxKzHl5Dxr65VSK3SyaaT3eEcEIhxAQKBgQDlEDCPX6xe7t1lzkYnOcWIJM7sxm+7qLQIdRiI6PdcjR0jKlUYiC3QiezOq/13icJwdhT3YEuXzrl9KhRQP0p26MynNyIPdzf5cburbQs8TjuRVET/b/+a/5owNys+7Es3cQLPjnpZoc0ua4HUfp1KUmhJVXIkiYFdoalyQglcKwKBgQCSJcKEno0m5HKTJciDI6hFJawRtDyVVNMKwgU4PL3uOOfIYg4RqOWS6DhYGTp1cS/QVwpxFZEp9tsV/BUPGG89zfWFGFHSbN0CQJ0ciLnt9PWgLmmnEoldKjYAEtUEECkqEnhcfX45URXh/Ajovdjyx8ZIzUX7A4Msdm0QaiL6CQKBgFNWxJQlVu1jMqHrnpJ+7mNOIQr8V4KLKpU5JSgKupStssAskrFOOnz48XNwF9P6Bwgrrg0it94PlGT5flKD9G+2216rLHU8Bt12h7UjA0d5nyqM4l2dPf4CqPKJ3+4Qwnz1XPRu/gzGVYRiEOZi+o8j1FV6BQFOFgce3it34zfpAoGAIGkrWEnpN8TV+/gXHa4ctSkDnfGAtgVXwD0C91PRMzF/hZrpT9VLBGdnh3Ig2sa9OdDzydxkkw/hymBbMzvR/7zbRCb+jlICOWum7BsOUM1QY7OnTpv3xqeDzkRe5UXIO0JZWvmhsohe9/zy5NqlcEIJoT+U03QV9RhWW5uOIjECgYEAxqPQzCtsjJUlTHjTYPU7CzatXke3iNhKZK9hvRb5Mi6yaW7Dt2himNT4V+lpfkyITWW/eMcmoutNw4jKAI5QiHFOk6WZM3TCi402yB1Ik7LZoz5Hg8nHzuZZ5L9WAzUTPhRrmiUweejl1BpozTftZANhSOXBtZPz87kocBtvmy0=";

    private static String license = "WwDMto8H8p3oOwAPeyqybKpsommaufqd3cls31bGnE37bbnQDNb33iWrhhwY40CLWV7T8V4dfo1j+maxOiRURKddPOLYGrWyIQhhSgMfs/pmATG6JCGHVvLbXKzLNAyA31hT7IQZsDrrz9NZP5xt68nwjE1bNQF1S/iuCWV5sKg2a4M9UV3Ie9EdReRSefLi1YQyob3BepyqBj6MK/DE0qoszQ2/p2DZHQ16C3GH8u71SjbzeQWMSbO7VBDao+DosoO70hTOCR/niSJSqL7UxxEUXIYRQ1XkmY+PZ+M0jIea/z9K5HR2lZxoEKTM2Qwr+nLjRmJNpO92YzAnhh9cxg==";

    private static Logger logger = Logger.getLogger(LicenseController.class);

    private static Cache cache = Redis.use("xiaosong");

    /**
     * 对前端传入的参数 进行校验,
     * 没有就存入内存中 然后放行
     * 有就放行
     */
    public void index() throws Exception {

        try {
            //前端 传入参数
            String code = getPara("license");
            String privateKey = getPara("key");
            byte[] res = decrypt(loadPrivateKeyByStr(privateKey), Base64.decodeBase64(code));
            String str = new String(res);
            String[] str1 = str.split("\\|");
            //上位机编码
            String SwjCode = str1[0];
            //mac地址
            String mac = str1[1];
            //结束时间
            String beginDateDate = str1[2];
            //开始时间时间
            String endDate = str1[3];
            TbLicense tbLicense1 = srv.findMac(mac);

            //判断 数据库 中是否 有数据 没有就存
            if (tbLicense1 == null) {
                TbLicense tbLicense = getModel(TbLicense.class);
                tbLicense.setMac(mac);
                tbLicense.setSWJCode(SwjCode);
                tbLicense.setBeginDate(beginDateDate);
                tbLicense.setEndDate(endDate);
                boolean save = tbLicense.save();
                if (save) {
                    logger.info("license 保存成功~");
                    //存入缓存
                    cache.set("SwjCode", SwjCode);
                    cache.set("mac", mac);
                    cache.set("beginDateDate", beginDateDate);
                    cache.set("endDate", endDate);
                    logger.info("license 已存入缓存~");
                    renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "license已存入缓存~"));
                }
            } else {
                String cacheMac = cache.get("mac");
                String cacheEndDate = cache.get("endDate");
                //从缓存中获取mac 并效验
                if (cacheMac == null || cacheEndDate == null) {
                    renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "缓存中的mac或时间不存在~"));
                } else {
                    if (cacheMac.equals(mac) && Misc.compareDate2(cacheEndDate, getDate())) {
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "license匹对成功~"));
                    } else {
                        //缓存中 mac 不匹配 就从 数据库中获取
                        if (mac.equals(tbLicense1.getMac()) && Misc.compareDate2(tbLicense1.getEndDate(), getDate())) {
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "license匹对成功~"));
                        } else {
                            logger.error("mac不存在或时间已过期~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "mac不存在或时间已过期~"));
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("license异常错误~");
        }
    }

    /**
     * 读取  license  文件并切割长度
     */

    public void cache() {
        try {
            String lic = fileRead();
            String[] split = lic.split("\\|");
            if (split.length >= 5) {
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL));
            } else {
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }

    /**
     * 私钥解密过程
     *
     * @param privateKey 私钥
     * @param cipherData 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData)
            throws Exception {
        if (privateKey == null) {
            throw new Exception("解密私钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            // 使用默认RSA
            cipher = Cipher.getInstance("RSA");
            // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(cipherData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("解密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * @param privateKeyStr 私钥
     * @return
     * @throws Exception
     */
    public static RSAPrivateKey loadPrivateKeyByStr(String privateKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.decodeBase64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new Exception("私钥非法");
        } catch (NullPointerException e) {
            throw new Exception("私钥数据为空");
        }
    }

    /**
     * 读取文件
     *
     * @throws Exception
     */

    public static String fileRead() throws Exception {
        //linux 路径
//        File file = new File(PathKit.getWebRootPath() + "/WEB-INF/classes/license.txt");//定义一个file对象，用来初始化FileReader
        //winds 路劲
        File file = new File("license.txt");//定义一个file对象，用来初始化FileReader
        logger.info("license的路劲:" + "license.txt");
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();

        return sb.toString();
    }

    /**
     * 获取当前时间 年月日
     *
     * @return
     */
    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 获取 本地mac地址
     *
     * @return
     * @throws Exception
     */
    private static String getLocalMac() throws Exception {
        // TODO Auto-generated method stub
        //得到IP
        InetAddress ia = InetAddress.getLocalHost();
        //获取网卡，获取地址
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        System.out.println("mac数组长度：" + mac.length);
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //字节转换为整数
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            System.out.println("每8位:" + str);
            if (str.length() == 1) {
                sb.append("0" + str);
            } else {
                sb.append(str);
            }
        }
        logger.info("本机MAC地址:" + sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

    //    //测试解密 结果
    @Test
    public void test() throws Exception {
        LicenseController l = new LicenseController();
        byte[] decrypt = l.decrypt(loadPrivateKeyByStr(resriviceKey), Base64.decodeBase64(license));
        String str = new String(decrypt);
        System.out.println(str);
    }

}
