package com.xiaosong.common.licence;

import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.util.Misc;
import com.xiaosong.util.RetUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
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
public class LicenseController extends Controller {

    private static String resriviceKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDznJEH7XnzxUclxsLctGEqQN+OxAenKCdtelt6bw0/2BpZL+27eSixFyU+z6nqDiD+yoaUtdtDYZYuE4WbokrZy9dvev+OsDk6eL12VKhIPUtVgXpFK0ypcSrQZol6FOyQrXBZlygHUTcyS0ummGYkDEGbPmOogEZBwBhV3qpw9CqtqlDh/JkzTTUMV3qQaviviCUnvMWae2EKn4JTk0/C/JJdQrtu8MzTVubd6JNeP3DMCvQPDzlaM8AG126/DhIEnAqh9lwrGCQ4hVtT7m/bAajGJMflY7bQ8a9JCFqfnXwS3r7W1Lc+6SaPHJFIaFOfJiGemrTZ5gxgGBgUr4a9AgMBAAECggEAIY/vdz0jcQ871xuEGy4KuOyJID0npMLmc7HIypxkIeie8Kymvw5ZKdS7f+TSTvm1WAUE95X6aFUYgK6V2LRKRalMvAIhFUJ4D/M0fwn4yUMQju4wrzjg7fM2Z9HInPYnBWOvt9gYXrG0vgwblw8l+09o9n9o8X0CsOpLMAFmT/s+2VyS+9d4Uelqi7736JAGvE/johR/1y9KOI4n0Nf/HK9kNPW0uus70RfIhKdNu1oFb147fDm2Q6tJu0fmFq176o2h7L5aFKo+FFj1vIdxGElQ+nB59qYjbLrjzsUaoAwIaQxtBVKuIEmvPGrm/FYZjMtbOda/dzOw9SIUTrQ4QQKBgQD8ZEa5OZzagFuLoWS0b7YjFIfA0PjNWTOgiE4NrFofGwz4GdZ/cDbcUxSaPbSGOfkUUkLsoieZf/UGzPulBEXvI92SnuU4D0z8RnHkKjQ9uFUoRljo2X9z+wAXNNJZpigbUcEwQYjSdW0+ndkYW5gmJO8dU4Tay7sAs+woOTIwjQKBgQD3GCf0wehCq/oDUh/zQDVg+sn0oO70jLlhMLxWFq9IK3OeHAO0kB85YzVaXVoP2FLtfZ5TZhDrxXJQlkWQGYzGwfciECiCTAqW0Vr/eIP3kNBccOXqlSVi0lzA44C7OLlTy3DMtkBQGriRGBM0OADOcNxtl1PZ8X+szt/wLOea8QKBgGS77RJOcVEwO9l+AACYOZzEu+30OelTuexPmr+QU6PomwOG5HqWof7L1gQ9roiIKOa4cmeZzK5SSMx/Eczp6LOqn+u+KYR3buWdhCVpxtH/eVWKxj+hV2JGAuJAPdh0HbvRpIaLEX3WSlTckWJMZGyM/za4N+VPvQJpzU68jVCdAoGBAMfNrrGKAse2zMnWpKNMLl2nLZs0b0UsbdKbWA+89CvFhw441P4y9Cyxfjj994+MezIzO81wYQpldJSZjtKA9obZ3X6b1kXWO1HNJGokB4IuhW3alO/lfn+0XvBShovdyG5ruWCvlr2vfcNF1nGJP42vUW8EyPcZAwa3mflaNkXhAoGAHPqDSN8UOIiXyCLwVSZW9RLZ7VFJNroMci8yTL7G+p7GJsR6Opqnd+MQHCXL/XCZlenVcBryKdwywlyvbuKKBM2cC8Y2Djg4l2L2x+ueLCQe5oAhUjSv8g+PXWY8Z20eKHYh5y8X5JD1ys7sVV7BoQde005wTMRZNyduZJEMe3U=";

    private static String license = "wb239LpUbOpM+hCTy/CdiQH85NJsOOkW4XPUi7gnEtc64c09MpetlJIYpB41lnTnhX4jsev/MjBc92GyUrbJzoaAxpkJ7CiYS07fPN+NSWJEjQzYLRj8+rgKc5P8ZDt+saBZpf60TNtEYxogg3PdSnIqcBG6iskOzWbzeoEZ/X9EWsESOCip1aalJIJwwGMfng+TSBaJrYrFDkXXhJ67fKab03fZjBKxmdF3o4dkwzI8+s3RZsETAWm0O9vjhtz11aJ1qcSYQfGegnpwkj02C16eriwKVefVq8cMH+9dPsz1YieOMC+Wxh05wth9FxQzfDel58pFgSk3OO4u1v28Cg==";

    private static Logger logger = Logger.getLogger(LicenseController.class);

    /**
     * 对前端传入的参数 进行校验,
     * 没有就存入内存中 然后放行
     * 有就放行
     */
    public void index() throws Exception {

        //读取文件
        String lic = fileRead();
        String[] split = lic.split("\\|");
        String license = null; //license
        license = split[1];

        //前端 传入参数
        String code = getPara("license");
        String privateKey = getPara("key");
        byte[] res = decrypt(loadPrivateKeyByStr(privateKey), Base64.decodeBase64(code));
        String str = new String(res);
        String[] str1 = str.split("\\|");
        System.out.println(str);
        System.out.println(Misc.compareDate2(str1[3],getDate()));
        System.out.println(license);
        System.out.println(str1[3]);
        if (license.equals(str1[1]) && Misc.compareDate2(str1[3],getDate())) {
            //linux 路径
//            OutputStream out = new FileOutputStream(PathKit.getWebRootPath()+"/WEB-INF/classes/license.txt");
            //winds 路径
            OutputStream out = new FileOutputStream("license.txt");
            String s = str+"|T";
            InputStream is = new ByteArrayInputStream(s.getBytes());
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = is.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            is.close();
            out.close();
            renderJson(RetUtil.ok());

        } else {
            renderJson(RetUtil.fail());
        }

    }

    /**
     * 读取  license  文件并切割长度
     */
    public void cache() {
        try {
            String lic = fileRead();
            String[] split = lic.split("\\|");
            if(split.length>=5){
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL));
            }else{
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
//        File file = new File(PathKit.getWebRootPath()+"/WEB-INF/classes/license.txt");//定义一个file对象，用来初始化FileReader
        //winds 路劲
        File file = new File("license.txt");//定义一个file对象，用来初始化FileReader
        logger.info("license的路劲:"+"license.txt");
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

    public static void main(String[] args) throws Exception {
        LicenseController l= new LicenseController();
        byte[] decrypt = l.decrypt(loadPrivateKeyByStr(resriviceKey), Base64.decodeBase64(license));
        String str = new String(decrypt);
        System.out.println(str);
    }

}
