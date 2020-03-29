package com.xiaosong.config.quartz;

import com.xiaosong.common.floor.FloorService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * 定时发送上位机数据
 */
public class GetSWJData implements Job {
    private static Logger logger = Logger.getLogger(GetSWJData.class);
    private static FloorService srvFloor = FloorService.me;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            findData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //查询数据
    public void findData() {
        String pospCode = srvFloor.findPospCode();
        String orgCode = srvFloor.findByOrgCode();
        try {
            String  privateKey= "Kj9MmF6cVp1+ttjcFuXfWXarFHCmHZ9qeyu9lpt9HM4Pi02iBP+BqAvEx+5PXAkkgj4r6FQiaQYVWgw5SvmNRRKzfugGfV5IBhzuzezOypjNLPxes6T2uRRJvovxUoIgKHKKHrsf+aT5ErfQeTdkkrPkjpRB17G8c4yttp3iMFV6OVJ3s2slna39eBv9fK+vQWlfoBaKfbadgWQE9kusRKNqHNzWTvvnX+hucAWDN6+sJHcjz9As+mgpu4ZIKO4VokzJC8qLvQF88zkVoJAdqvRzgcjWpubP0FeQ+4F0+R31b9jH2IpPN2BP3b5pG6RFnOGNH1MmsjBdT5SLUVD4lw==";
            String postURL = "http://121.36.45.232:8082/visitor/foreign/newFindOrgCode";
            PostMethod postMethod = null;
            postMethod = new PostMethod(postURL);
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            //参数设置，需要注意的就是里边不能传NULL，要传空字符串
            NameValuePair[] data = {
                    new NameValuePair("pospCode", pospCode),
                    new NameValuePair("orgCode", orgCode),
                    new NameValuePair("pageNum", "1"),
                    new NameValuePair("pageSize", "10"),
                    new NameValuePair("mac", privateKey),

            };

            postMethod.setRequestBody(data);

            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            int response = httpClient.executeMethod(postMethod); // 执行POST方法
            String result = postMethod.getResponseBodyAsString();

            logger.info(result);
            logger.info("状态码："+response);
        } catch (Exception e) {
            logger.error("请求异常" + e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void main(String[] args) {
        String pospCode = "00000001";
        String orgCode = "hlxz";
        try {
            //私钥
            String  privateKey= "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDznJEH7XnzxUclxsLctGEqQN+OxAenKCdtelt6bw0/2BpZL+27eSixFyU+z6nqDiD+yoaUtdtDYZYuE4WbokrZy9dvev+OsDk6eL12VKhIPUtVgXpFK0ypcSrQZol6FOyQrXBZlygHUTcyS0ummGYkDEGbPmOogEZBwBhV3qpw9CqtqlDh/JkzTTUMV3qQaviviCUnvMWae2EKn4JTk0/C/JJdQrtu8MzTVubd6JNeP3DMCvQPDzlaM8AG126/DhIEnAqh9lwrGCQ4hVtT7m/bAajGJMflY7bQ8a9JCFqfnXwS3r7W1Lc+6SaPHJFIaFOfJiGemrTZ5gxgGBgUr4a9AgMBAAECggEAIY/vdz0jcQ871xuEGy4KuOyJID0npMLmc7HIypxkIeie8Kymvw5ZKdS7f+TSTvm1WAUE95X6aFUYgK6V2LRKRalMvAIhFUJ4D/M0fwn4yUMQju4wrzjg7fM2Z9HInPYnBWOvt9gYXrG0vgwblw8l+09o9n9o8X0CsOpLMAFmT/s+2VyS+9d4Uelqi7736JAGvE/johR/1y9KOI4n0Nf/HK9kNPW0uus70RfIhKdNu1oFb147fDm2Q6tJu0fmFq176o2h7L5aFKo+FFj1vIdxGElQ+nB59qYjbLrjzsUaoAwIaQxtBVKuIEmvPGrm/FYZjMtbOda/dzOw9SIUTrQ4QQKBgQD8ZEa5OZzagFuLoWS0b7YjFIfA0PjNWTOgiE4NrFofGwz4GdZ/cDbcUxSaPbSGOfkUUkLsoieZf/UGzPulBEXvI92SnuU4D0z8RnHkKjQ9uFUoRljo2X9z+wAXNNJZpigbUcEwQYjSdW0+ndkYW5gmJO8dU4Tay7sAs+woOTIwjQKBgQD3GCf0wehCq/oDUh/zQDVg+sn0oO70jLlhMLxWFq9IK3OeHAO0kB85YzVaXVoP2FLtfZ5TZhDrxXJQlkWQGYzGwfciECiCTAqW0Vr/eIP3kNBccOXqlSVi0lzA44C7OLlTy3DMtkBQGriRGBM0OADOcNxtl1PZ8X+szt/wLOea8QKBgGS77RJOcVEwO9l+AACYOZzEu+30OelTuexPmr+QU6PomwOG5HqWof7L1gQ9roiIKOa4cmeZzK5SSMx/Eczp6LOqn+u+KYR3buWdhCVpxtH/eVWKxj+hV2JGAuJAPdh0HbvRpIaLEX3WSlTckWJMZGyM/za4N+VPvQJpzU68jVCdAoGBAMfNrrGKAse2zMnWpKNMLl2nLZs0b0UsbdKbWA+89CvFhw441P4y9Cyxfjj994+MezIzO81wYQpldJSZjtKA9obZ3X6b1kXWO1HNJGokB4IuhW3alO/lfn+0XvBShovdyG5ruWCvlr2vfcNF1nGJP42vUW8EyPcZAwa3mflaNkXhAoGAHPqDSN8UOIiXyCLwVSZW9RLZ7VFJNroMci8yTL7G+p7GJsR6Opqnd+MQHCXL/XCZlenVcBryKdwywlyvbuKKBM2cC8Y2Djg4l2L2x+ueLCQe5oAhUjSv8g+PXWY8Z20eKHYh5y8X5JD1ys7sVV7BoQde005wTMRZNyduZJEMe3U=";
            String postURL = "http://121.36.45.232:8082/visitor/companyUser/newFindApplyAllSucOrg";
            //需要加密的mac
            String plainText="42-74-E0-38-2F-F7";
            //加密
            byte[] encrypt = encrypt(loadPrivateKeyByStr(privateKey), plainText.getBytes());
            String mac = Base64.encodeBase64String(encrypt);
            System.out.println("加密后的mac:"+mac);
            PostMethod postMethod = null;
            postMethod = new PostMethod(postURL);
            postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            //参数设置，需要注意的就是里边不能传NULL，要传空字符串
            NameValuePair[] data = {
                    new NameValuePair("pospCode", pospCode),
                    new NameValuePair("orgCode", orgCode),
                    new NameValuePair("pageNum", "1"),
                    new NameValuePair("pageSize", "10"),
                    new NameValuePair("mac", mac),

            };

            postMethod.setRequestBody(data);

            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            int response = httpClient.executeMethod(postMethod); // 执行POST方法
            String result = postMethod.getResponseBodyAsString();
            System.out.println("response"+response);
            System.out.println("result"+result);

            logger.info(result);
            logger.info("状态码："+response);
        } catch (Exception e) {
            logger.error("请求异常" + e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

//    public static void main(String[] args) throws Exception {
//        String key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDznJEH7XnzxUclxsLctGEqQN+OxAenKCdtelt6bw0/2BpZL+27eSixFyU+z6nqDiD+yoaUtdtDYZYuE4WbokrZy9dvev+OsDk6eL12VKhIPUtVgXpFK0ypcSrQZol6FOyQrXBZlygHUTcyS0ummGYkDEGbPmOogEZBwBhV3qpw9CqtqlDh/JkzTTUMV3qQaviviCUnvMWae2EKn4JTk0/C/JJdQrtu8MzTVubd6JNeP3DMCvQPDzlaM8AG126/DhIEnAqh9lwrGCQ4hVtT7m/bAajGJMflY7bQ8a9JCFqfnXwS3r7W1Lc+6SaPHJFIaFOfJiGemrTZ5gxgGBgUr4a9AgMBAAECggEAIY/vdz0jcQ871xuEGy4KuOyJID0npMLmc7HIypxkIeie8Kymvw5ZKdS7f+TSTvm1WAUE95X6aFUYgK6V2LRKRalMvAIhFUJ4D/M0fwn4yUMQju4wrzjg7fM2Z9HInPYnBWOvt9gYXrG0vgwblw8l+09o9n9o8X0CsOpLMAFmT/s+2VyS+9d4Uelqi7736JAGvE/johR/1y9KOI4n0Nf/HK9kNPW0uus70RfIhKdNu1oFb147fDm2Q6tJu0fmFq176o2h7L5aFKo+FFj1vIdxGElQ+nB59qYjbLrjzsUaoAwIaQxtBVKuIEmvPGrm/FYZjMtbOda/dzOw9SIUTrQ4QQKBgQD8ZEa5OZzagFuLoWS0b7YjFIfA0PjNWTOgiE4NrFofGwz4GdZ/cDbcUxSaPbSGOfkUUkLsoieZf/UGzPulBEXvI92SnuU4D0z8RnHkKjQ9uFUoRljo2X9z+wAXNNJZpigbUcEwQYjSdW0+ndkYW5gmJO8dU4Tay7sAs+woOTIwjQKBgQD3GCf0wehCq/oDUh/zQDVg+sn0oO70jLlhMLxWFq9IK3OeHAO0kB85YzVaXVoP2FLtfZ5TZhDrxXJQlkWQGYzGwfciECiCTAqW0Vr/eIP3kNBccOXqlSVi0lzA44C7OLlTy3DMtkBQGriRGBM0OADOcNxtl1PZ8X+szt/wLOea8QKBgGS77RJOcVEwO9l+AACYOZzEu+30OelTuexPmr+QU6PomwOG5HqWof7L1gQ9roiIKOa4cmeZzK5SSMx/Eczp6LOqn+u+KYR3buWdhCVpxtH/eVWKxj+hV2JGAuJAPdh0HbvRpIaLEX3WSlTckWJMZGyM/za4N+VPvQJpzU68jVCdAoGBAMfNrrGKAse2zMnWpKNMLl2nLZs0b0UsbdKbWA+89CvFhw441P4y9Cyxfjj994+MezIzO81wYQpldJSZjtKA9obZ3X6b1kXWO1HNJGokB4IuhW3alO/lfn+0XvBShovdyG5ruWCvlr2vfcNF1nGJP42vUW8EyPcZAwa3mflaNkXhAoGAHPqDSN8UOIiXyCLwVSZW9RLZ7VFJNroMci8yTL7G+p7GJsR6Opqnd+MQHCXL/XCZlenVcBryKdwywlyvbuKKBM2cC8Y2Djg4l2L2x+ueLCQe5oAhUjSv8g+PXWY8Z20eKHYh5y8X5JD1ys7sVV7BoQde005wTMRZNyduZJEMe3U=";
//        String plainText="42-74-E0-38-2F-F7";
//        byte[] encrypt = encrypt(loadPrivateKeyByStr(key), plainText.getBytes());
//        String string = Base64.encodeBase64String(encrypt);
//        System.out.println(string);
//    }

    /**
     * 私钥加密过程
     *
     * @param privateKey
     *            私钥
     * @param plainTextData
     *            明文数据
     * @return
     * @throws Exception
     *             加密过程中的异常信息
     */
    public static byte[] encrypt(RSAPrivateKey privateKey, byte[] plainTextData)
            throws Exception {
        if (privateKey == null) {
            throw new Exception("加密私钥为空, 请设置");
        }
        Cipher cipher = null;
        try {
            // 使用默认RSA
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("加密私钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("明文数据已损坏");
        }
    }

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

}
