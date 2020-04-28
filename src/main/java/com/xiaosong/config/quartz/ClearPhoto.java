package com.xiaosong.config.quartz;

import com.xiaosong.model.TbTemperatureRecord;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 定时清理 本地照片
 */
public class ClearPhoto implements Job {
    private static Logger logger = Logger.getLogger(BackupsRecordDate.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            clearPhoto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定时清理 本地照片
     */
    public void clearPhoto() throws Exception {
        //前两个月的时间
        String date = date02(getDate());
        String sql = "select t.*,(select FROM_UNIXTIME(t.scan_timestamp/1000,'%Y-%m-%d %T')) as date from tb_temperature_record t where scan_timestamp < '" + date + "'";
        List<TbTemperatureRecord> tbTemperatureRecords = TbTemperatureRecord.dao.find(sql);
        if (tbTemperatureRecords != null) {
            for (TbTemperatureRecord record : tbTemperatureRecords) {
                // /usr/img/0_1587361791092_754_fever_cap.jpg
                String imagePath = record.getImagePath();
                // /usr/img/0_1587361791092
                String substring = imagePath.substring(0, 24);

                String[] split = imagePath.split("_");
                String isCap = split[4].substring(0, 3);
                if (isCap.equals("bg")) {
                    substring = substring + "_" + record.getTrackId() + "_fever_bg.jpg";
                }
//                String cmd = "rm -f " + substring;
                String capCmd = "rm -f " + record.getImagePath();
                String bgCmd = "rm -f " + substring;
                // rm -f /usr/img/0_1587361791092_754_fever_cap.jpg
                Runtime.getRuntime().exec(capCmd);
                // rm -f /usr/img/0_1587361791092_754_fever_bg.jpg
                Runtime.getRuntime().exec(bgCmd);
            }
            logger.info("图片删除成功~");
        }else{
            logger.error("暂无可删除的照片~");
        }
    }


    /**
     * 当前时间减60天
     *
     * @param dateTime
     * @return
     * @throws ParseException
     */
    public String date02(String dateTime) throws ParseException {
        Date date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(dateTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -60);
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cal.getTime());
    }

    /**
     * 获取当前系统时间 年-月-日
     *
     * @return
     */
    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    @Test
    public void test() {
        String imagePath = "/usr/img/0_1587361791092_754_fever_bg.jpg";
        ///usr/img/0_1587361791092_754_fever_cap.jpg
        ///usr/img/0_1587361791092
        String substring = imagePath.substring(0, 24);

        String[] split = imagePath.split("_");
        String isCap = split[4].substring(0, 3);
        if (isCap.equals("cap")) {
            substring = substring + "_" + "754" + "_fever_cap.jpg";
        } else {
            substring = substring + "_" + "754" + "_fever_bg.jpg";
        }
        String cmd = "rm -f " + substring;
        System.out.println(cmd);
    }
}
