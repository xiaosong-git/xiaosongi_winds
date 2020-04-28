package com.xiaosong.config.quartz;

import com.xiaosong.model.TbRecordbackups;
import com.xiaosong.model.TbTemperatureRecord;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 定时备份数据库记录
 */
public class BackupsRecordDate implements Job {
    private static Logger logger = Logger.getLogger(BackupsRecordDate.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            backupsRecord();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 定时备份数据库记录
     */
    private void backupsRecord() throws ParseException {
        try {
            //前三个月的时间
            String date = date02(getDate());
            String sql = "select t.*,(select FROM_UNIXTIME(t.scan_timestamp/1000,'%Y-%m-%d %T')) as date from tb_temperature_record t where scan_timestamp < '" + date + "'";
            List<TbTemperatureRecord> tbTemperatureRecords = TbTemperatureRecord.dao.find(sql);
            if (tbTemperatureRecords != null) {
                boolean save = false;
                for (TbTemperatureRecord temperatureRecord : tbTemperatureRecords) {
                    TbRecordbackups tbRecordbackups = new TbRecordbackups();
                    tbRecordbackups.setChannelId(temperatureRecord.getChannelId());
                    tbRecordbackups.setFrameId(temperatureRecord.getFrameId());
                    tbRecordbackups.setTrackId(temperatureRecord.getTrackId());
                    tbRecordbackups.setImagePath(temperatureRecord.getImagePath());
                    tbRecordbackups.setScanTimestamp(temperatureRecord.getScanTimestamp());
                    tbRecordbackups.setName(temperatureRecord.getName());
                    tbRecordbackups.setTemperature(temperatureRecord.getTemperature());
                    tbRecordbackups.setScore(temperatureRecord.getScore());
                    tbRecordbackups.setTag(temperatureRecord.getTag());
                    save = tbRecordbackups.save();
                }
                if (save) {
                    logger.info("数据备份成功~");
                    boolean delete = false;
                    for (TbTemperatureRecord tbTemperatureRecord : tbTemperatureRecords) {
                        delete = tbTemperatureRecord.delete();
                        if (delete) {
                            logger.info("原数据已删除~");
                        } else {
                            logger.error("原数据删除失败~");
                        }
                    }
                } else {
                    logger.error("数据备份失败~");
                }
            } else {
                logger.error("暂无可备份数据~");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            logger.error("数据备份异常~");
        }
    }

    /**
     * 当前时间减90天
     * 三个月
     *
     * @param dateTime
     * @return
     * @throws ParseException
     */
    public String date02(String dateTime) throws ParseException {
        Date date = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(dateTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -90);
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
    public void test() throws ParseException {
        backupsRecord();
        System.out.println(date02(getDate()));
    }

    /**
     * 毫秒转换成 时间
     *
     * @param millis
     * @return
     */
    public String date(long millis) {
        Date date2 = new Date();
        date2.setTime(millis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fmDate = simpleDateFormat.format(date2);
        return fmDate;
    }

}
