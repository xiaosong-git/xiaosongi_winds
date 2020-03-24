package com.xiaosong.config.quartz;

import com.xiaosong.common.floor.FloorService;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
            String postURL = "http://192.168.4.6:8088/visitor/foreign/newFindOrgCode";
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
}
