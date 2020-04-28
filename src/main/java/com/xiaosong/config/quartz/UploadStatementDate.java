package com.xiaosong.config.quartz;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.xiaosong.common.campus.statement.StatementController;
import com.xiaosong.common.campus.statement.StatementService;
import com.xiaosong.model.TbPersonnel;
import com.xiaosong.model.TbTemperatureRecord;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 发送数据到云端
 */
public class UploadStatementDate implements Job {
    public StatementService srv = StatementService.me;
    private static Logger logger = Logger.getLogger(UploadStatementDate.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            //发送数据到云端
            uploadDate();
            //根据标识查询 下次下发
            againSend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传报表信息到云端
     */
    public void uploadDate() {
        try {
            //读取 资源文件 中的 云端路劲
            Prop use = PropKit.use("uploadPath.properties");
            String path = use.get("path");
            String path1 = use.get("path1");
            String swjCode = use.get("swjCode");
            //上传状态码
            int statusCode = 0;
            List<TbTemperatureRecord> tbStatementList = srv.findCondition();
            // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            // 参数
            try {
                List<TbTemperatureRecord> list = new ArrayList<>();
                // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)

                for (int i = 0; i < tbStatementList.size(); i++) {
                    list.add(tbStatementList.get(i));
                }
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).get("name1").toString().equals("陌生人")) {
                        StringBuffer params = new StringBuffer();

                        params.append("userName=" + URLEncoder.encode(list.get(i).get("name1").toString(), "UTF-8"));
                        params.append("&");
                        params.append("animalHeat=" + URLEncoder.encode(list.get(i).get("temperature").toString(), "UTF-8"));
                        params.append("&");
                        params.append("status=" + URLEncoder.encode(list.get(i).get("status1").toString(), "UTF-8"));
                        params.append("&");
                        params.append("date=" + URLEncoder.encode(list.get(i).get("date").toString(), "UTF-8"));
                        params.append("&");
                        params.append("swjCode=" + URLEncoder.encode(swjCode, "UTF-8"));

                        // 创建Post请求
                        HttpPost httpPost = new HttpPost(path + "?" + params);

                        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                        // 响应模型
                        CloseableHttpResponse response = null;
                        try {
                            // 由客户端执行(发送)Post请求
                            response = httpClient.execute(httpPost);
                            // 从响应模型中获取响应实体
                            HttpEntity responseEntity = response.getEntity();

                            statusCode = response.getStatusLine().getStatusCode();
                            logger.info("响应状态为:" + response.getStatusLine());
                            if (responseEntity != null) {
                                logger.info("响应内容长度为:" + responseEntity.getContentLength());
                                logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                // 释放资源
                                if (httpClient != null) {
//                                httpClient.close();
                                }
                                if (response != null) {
//                                response.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        TbPersonnel tbPersonnel = srv.findByPerName(list.get(i).get("name1").toString());
                        if (tbPersonnel == null) {
                            StringBuffer params = new StringBuffer();

                            params.append("userName=" + URLEncoder.encode(list.get(i).get("name1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("animalHeat=" + URLEncoder.encode(list.get(i).get("temperature").toString(), "UTF-8"));
                            params.append("&");
                            params.append("status=" + URLEncoder.encode(list.get(i).get("status1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("date=" + URLEncoder.encode(list.get(i).get("date").toString(), "UTF-8"));
                            params.append("&");
                            params.append("swjCode=" + URLEncoder.encode(swjCode, "UTF-8"));

                            // 创建Post请求
                            HttpPost httpPost = new HttpPost(path + "?" + params);

                            // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                            httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                            // 响应模型
                            CloseableHttpResponse response = null;
                            try {
                                // 由客户端执行(发送)Post请求
                                response = httpClient.execute(httpPost);
                                // 从响应模型中获取响应实体
                                HttpEntity responseEntity = response.getEntity();

                                statusCode = response.getStatusLine().getStatusCode();
                                logger.info("响应状态为:" + response.getStatusLine());
                                if (responseEntity != null) {
                                    logger.info("响应内容长度为:" + responseEntity.getContentLength());
                                    logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                                }
                            } catch (ClientProtocolException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    // 释放资源
                                    if (httpClient != null) {
//                                httpClient.close();
                                    }
                                    if (response != null) {
//                                response.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            StringBuffer params = new StringBuffer();

                            params.append("userName=" + URLEncoder.encode(list.get(i).get("name1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("studentNumber=" + URLEncoder.encode(list.get(i).get("studentNumber").toString(), "UTF-8"));
                            params.append("&");
                            params.append("groupName=" + URLEncoder.encode(list.get(i).get("groupName").toString(), "UTF-8"));
                            params.append("&");
                            params.append("animalHeat=" + URLEncoder.encode(list.get(i).get("temperature").toString(), "UTF-8"));
                            params.append("&");
                            params.append("status=" + URLEncoder.encode(list.get(i).get("status1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("dormitory=" + URLEncoder.encode(list.get(i).get("dormitory").toString(), "UTF-8"));
                            params.append("&");
                            params.append("isAccommodation=" + URLEncoder.encode(list.get(i).get("isAccommodation").toString(), "UTF-8"));
                            params.append("&");
                            params.append("date=" + URLEncoder.encode(list.get(i).get("date").toString(), "UTF-8"));
                            params.append("&");
                            params.append("swjCode=" + URLEncoder.encode(swjCode, "UTF-8"));

                            // 创建Post请求
                            HttpPost httpPost = new HttpPost(path + "?" + params);

                            // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                            httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                            // 响应模型
                            CloseableHttpResponse response = null;
                            try {
                                // 由客户端执行(发送)Post请求
                                response = httpClient.execute(httpPost);
                                // 从响应模型中获取响应实体
                                HttpEntity responseEntity = response.getEntity();

                                statusCode = response.getStatusLine().getStatusCode();
                                logger.info("响应状态为:" + response.getStatusLine());
                                if (responseEntity != null) {
                                    logger.info("响应内容长度为:" + responseEntity.getContentLength());
                                    logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                                }
                            } catch (ClientProtocolException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    // 释放资源
                                    if (httpClient != null) {
//                                httpClient.close();
                                    }
                                    if (response != null) {
//                                response.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            if (statusCode == 200) {
                logger.info("上传成功~");
                List<TbPersonnel> perAll = srv.findPerAll();
                if (perAll != null) {
                    int code = 0;
                    for (TbPersonnel tbPersonnel : perAll) {
                        StringBuffer params = new StringBuffer();
                        params.append("userName=" + URLEncoder.encode(tbPersonnel.getUserName(), "UTF-8"));
                        params.append("&");
                        params.append("studentNumber=" + URLEncoder.encode(tbPersonnel.getStudentNumber().toString(), "UTF-8"));
                        params.append("&");
                        params.append("groupName=" + URLEncoder.encode(tbPersonnel.getGroupName(), "UTF-8"));
                        params.append("&");
                        params.append("dormitory=" + URLEncoder.encode(tbPersonnel.getDormitory(), "UTF-8"));
                        params.append("&");
                        params.append("isAccommodation=" + URLEncoder.encode(tbPersonnel.getIsAccommodation(), "UTF-8"));
                        params.append("&");
                        params.append("swjCode=" + URLEncoder.encode(swjCode, "UTF-8"));

                        // 创建Post请求
                        HttpPost httpPost = new HttpPost(path1 + "?" + params);

                        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                        // 响应模型
                        CloseableHttpResponse response = null;
                        try {
                            // 由客户端执行(发送)Post请求
                            response = httpClient.execute(httpPost);
                            // 从响应模型中获取响应实体
                            HttpEntity responseEntity = response.getEntity();

                            code = response.getStatusLine().getStatusCode();
                            logger.info("响应状态为:" + response.getStatusLine());
                            if (responseEntity != null) {
                                logger.info("响应内容长度为:" + responseEntity.getContentLength());
                                logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                // 释放资源
                                if (httpClient != null) {
//                                httpClient.close();
                                }
                                if (response != null) {
//                                response.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (code == 200) {
                        logger.info("校园花名册上传成功~");
                    } else {
                        logger.info("校园花名册上传~");
                    }
                }
                //上传成功后标识
                int b = srv.updateDate();
                if (b == 1) {
                    logger.info("标识成功~");
                } else {
                    logger.info("标识失败~");
                }
            } else {
                logger.error("上传失败,暂无用户通行...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("上传异常~");
        }

    }

    /**
     * 根据没下发成功标识查询 再次下发
     */
    public void againSend() {
        try {
            //读取 资源文件 中的 云端路劲
            Prop use = PropKit.use("uploadPath.properties");
            String path = use.get("path");
            String swjCode = use.get("swjCode");
            //上传状态码
            int statusCode = 0;
            //查询报表中的 未标识的 数据
            List<TbTemperatureRecord> tbStatementList = srv.findAll();
            // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            // 参数
            try {
                List<TbTemperatureRecord> list = new ArrayList<>();
                // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)

                for (int iii = 0; iii < tbStatementList.size(); iii++) {
                    list.add(tbStatementList.get(iii));
                }
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).get("name1").toString().equals("陌生人")) {
                        StringBuffer params = new StringBuffer();

                        params.append("userName=" + URLEncoder.encode(list.get(i).get("name1").toString(), "UTF-8"));
                        params.append("&");
                        params.append("animalHeat=" + URLEncoder.encode(list.get(i).get("temperature").toString(), "UTF-8"));
                        params.append("&");
                        params.append("status=" + URLEncoder.encode(list.get(i).get("status1").toString(), "UTF-8"));
                        params.append("&");
                        params.append("date=" + URLEncoder.encode(list.get(i).get("date").toString(), "UTF-8"));
                        params.append("&");
                        params.append("swjCode=" + URLEncoder.encode(swjCode, "UTF-8"));

                        // 创建Post请求
                        HttpPost httpPost = new HttpPost(path + "?" + params);

                        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                        httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                        // 响应模型
                        CloseableHttpResponse response = null;
                        try {
                            // 由客户端执行(发送)Post请求
                            response = httpClient.execute(httpPost);
                            // 从响应模型中获取响应实体
                            HttpEntity responseEntity = response.getEntity();

                            statusCode = response.getStatusLine().getStatusCode();
                            logger.info("响应状态为:" + response.getStatusLine());
                            if (responseEntity != null) {
                                logger.info("响应内容长度为:" + responseEntity.getContentLength());
                                logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                // 释放资源
                                if (httpClient != null) {
                                    //                                httpClient.close();
                                }
                                if (response != null) {
                                    //                                response.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        TbPersonnel tbPersonnel = srv.findByPerName(list.get(i).get("name1").toString());
                        if (tbPersonnel == null) {
                            StringBuffer params = new StringBuffer();

                            params.append("userName=" + URLEncoder.encode(list.get(i).get("name1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("animalHeat=" + URLEncoder.encode(list.get(i).get("temperature").toString(), "UTF-8"));
                            params.append("&");
                            params.append("status=" + URLEncoder.encode(list.get(i).get("status1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("date=" + URLEncoder.encode(list.get(i).get("date").toString(), "UTF-8"));
                            params.append("&");
                            params.append("swjCode=" + URLEncoder.encode(swjCode, "UTF-8"));

                            // 创建Post请求
                            HttpPost httpPost = new HttpPost(path + "?" + params);

                            // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                            httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                            // 响应模型
                            CloseableHttpResponse response = null;
                            try {
                                // 由客户端执行(发送)Post请求
                                response = httpClient.execute(httpPost);
                                // 从响应模型中获取响应实体
                                HttpEntity responseEntity = response.getEntity();

                                statusCode = response.getStatusLine().getStatusCode();
                                logger.info("响应状态为:" + response.getStatusLine());
                                if (responseEntity != null) {
                                    logger.info("响应内容长度为:" + responseEntity.getContentLength());
                                    logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                                }
                            } catch (ClientProtocolException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    // 释放资源
                                    if (httpClient != null) {
                                        //                                httpClient.close();
                                    }
                                    if (response != null) {
                                        //                                response.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            StringBuffer params = new StringBuffer();

                            params.append("userName=" + URLEncoder.encode(list.get(i).get("name1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("studentNumber=" + URLEncoder.encode(list.get(i).get("studentNumber").toString(), "UTF-8"));
                            params.append("&");
                            params.append("groupName=" + URLEncoder.encode(list.get(i).get("groupName").toString(), "UTF-8"));
                            params.append("&");
                            params.append("animalHeat=" + URLEncoder.encode(list.get(i).get("temperature").toString(), "UTF-8"));
                            params.append("&");
                            params.append("status=" + URLEncoder.encode(list.get(i).get("status1").toString(), "UTF-8"));
                            params.append("&");
                            params.append("dormitory=" + URLEncoder.encode(list.get(i).get("dormitory").toString(), "UTF-8"));
                            params.append("&");
                            params.append("isAccommodation=" + URLEncoder.encode(list.get(i).get("isAccommodation").toString(), "UTF-8"));
                            params.append("&");
                            params.append("date=" + URLEncoder.encode(list.get(i).get("date").toString(), "UTF-8"));
                            params.append("&");
                            params.append("swjCode=" + URLEncoder.encode(swjCode, "UTF-8"));

                            // 创建Post请求
                            HttpPost httpPost = new HttpPost(path + "?" + params);

                            // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
                            httpPost.setHeader("Content-Type", "application/json;charset=utf8");

                            // 响应模型
                            CloseableHttpResponse response = null;
                            try {
                                // 由客户端执行(发送)Post请求
                                response = httpClient.execute(httpPost);
                                // 从响应模型中获取响应实体
                                HttpEntity responseEntity = response.getEntity();

                                statusCode = response.getStatusLine().getStatusCode();
                                logger.info("响应状态为:" + response.getStatusLine());
                                if (responseEntity != null) {
                                    logger.info("响应内容长度为:" + responseEntity.getContentLength());
                                    logger.info("响应内容为:" + EntityUtils.toString(responseEntity));
                                }
                            } catch (ClientProtocolException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    // 释放资源
                                    if (httpClient != null) {
                                        //                                httpClient.close();
                                    }
                                    if (response != null) {
                                        //                                response.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            if (statusCode == 200) {
                //上传成功后标识
                int b = srv.update();
                if (b == 1) {
                    logger.info("标识成功~");
                } else {
                    logger.info("标识失败~");
                }
            } else {
                logger.error("再次上传失败,暂无用户通行...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("再次上传异常");
        }


    }

    @Test
    public void test() {
        System.out.println(System.currentTimeMillis());
    }
}