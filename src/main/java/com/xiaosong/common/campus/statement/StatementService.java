package com.xiaosong.common.campus.statement;

import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.model.TbPersonnel;
import com.xiaosong.model.TbTemperatureRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatementService {
    public static final StatementService me = new StatementService();

    /**
     * 根据 组名查询 人员信息
     */
    public List<TbPersonnel> findByGroupName(String groupName) {
        return TbPersonnel.dao.find("select * from tb_personnel where groupName = ?", groupName);
//        List<String> userNames = new ArrayList<>();
//        if (tbPersonnels.size() != 0) {
//            for (TbPersonnel tbPersonnel : tbPersonnels) {
//                String userName = tbPersonnel.getUserName() + "_" + tbPersonnel.getStudentNumber();
//                userNames.add(userName);
//            }
//        }
//        if (userNames.size() != 0) {
//            for (String userName : userNames) {
//                return TbTemperatureRecord.dao.find("select * from tb_temperature_record where name = ?" + userName);
//            }
//        }
//
//        return null;
    }

    /**
     * 查询所有 的 报表信息
     *
     * @return
     */
    public List<TbTemperatureRecord> findAll() {
        return TbTemperatureRecord.dao.find("select * from (select * from (select * from (SELECT t.*,p.*,(select FROM_UNIXTIME(t.scan_timestamp/1000,'%Y-%m-%d %T' )) \n" +
                "as date ,if(t.temperature>37.4||t.temperature<35.4,'abnormal','normal') \n" +
                "as status1,if(t.name = ''||t.name is null,'陌生人',p.userName) as name1\n" +
                "from tb_temperature_record t\n" +
                "LEFT JOIN tb_personnel p ON p.studentNumber = SUBSTRING_INDEX(t.name,'_',-1) || p.studentNumber = (select GetNum(t.name))\n" +
                "LEFT JOIN tb_group g on g.groupName = p.groupName) t) t ORDER BY t.date desc ) t where t.tag is null or t.tag = '' ");
    }

    /**
     * 根据 人员姓名 查询所有信息
     *
     * @param userName
     * @return
     */
    public List<TbTemperatureRecord> findByUserName(String userName) {
        return TbTemperatureRecord.dao.find("select *  from tb_temperature_record WHERE name like '%" + userName + "%'");
    }

    /**
     * 根据 分组 查询 所有信息
     *
     * @param groupName
     * @return
     */
    public List<TbTemperatureRecord> findGroupName(String groupName) {
        return TbTemperatureRecord.dao.find("select *  from tb_statement WHERE groupName like '%" + groupName + "%'");
    }

    /**
     * 根据宿舍查询 所有信息
     *
     * @param userName
     * @return
     */
    public List<TbTemperatureRecord> findByDormitory(String userName) {
        return TbTemperatureRecord.dao.find("select *  from tb_statement WHERE dormitory like '%" + userName + "%'");
    }

    /**
     * 分页模糊查询
     *
     * @param userName  用户名
     * @param status    状态
     * @param groupName 班级
     * @param dormitory 宿舍
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public List<TbTemperatureRecord> findByBim(String userName, String status, String groupName, String dormitory, String beginTime, String endTime) throws ParseException {
        if (beginTime != null) {
            beginTime = date02(beginTime);
        }
        if (endTime != null) {
            endTime = date01(endTime);
        }

        String sql = "select * from (select * from (select * from (SELECT t.*,p.*,(select FROM_UNIXTIME(t.scan_timestamp/1000,'%Y-%m-%d %T' )) \n" +
                "as date ,if(t.temperature>37.4||t.temperature<35.4,'abnormal','normal') \n" +
                "as status1,if(t.name = ''||t.name is null,'陌生人',p.userName) as name1\n" +
                "from tb_temperature_record t\n" +
                "LEFT JOIN tb_personnel p ON p.studentNumber = SUBSTRING_INDEX(t.name,'_',-1) || p.studentNumber = (select GetNum(t.name))\n" +
                "LEFT JOIN tb_group g on g.groupName = p.groupName) t) t ORDER BY t.date desc ) t ";

        if (userName != null) {
            sql += " where t.name1 like '%" + userName + "%'";
            if (status != null) {
                if (status.equals("normal")) {
                    sql += " and t.status1 = '" + "normal" + "'";
                } else {
                    sql += " and t.status1 = '" + "abnormal" + "'";
                }
            }
            if (groupName != null) {
                sql += " and t.groupName = '" + groupName + "'";
            }
            if (dormitory != null) {
                sql += " and t.dormitory = '" + dormitory + "'";
            }
            if (beginTime != null) {
                sql += " and t.date > '" + beginTime + "'";
            }
            if (endTime != null) {
                sql += " and t.date < '" + endTime + "'";
            }
            return TbTemperatureRecord.dao.find(sql);
        }
        if (status != null) {
            if (status.equals("normal")) {
                sql += " where t.status1 = '" + "normal" + "'";
            } else {
                sql += " where t.status1 = '" + "abnormal" + "'";
            }
            if (userName != null) {
                sql += " and t.name like '%" + userName + "%'";
            }
            if (groupName != null) {
                sql += " and t.groupName = '" + groupName + "'";
            }
            if (dormitory != null) {
                sql += " and t.dormitory = '" + dormitory + "'";
            }
            if (beginTime != null) {
                sql += " and t.date >= '" + beginTime + "'";
            }
            if (endTime != null) {
                sql += " and t.date <= '" + endTime + "'";
            }
            return TbTemperatureRecord.dao.find(sql);
        }
        if (groupName != null) {
            sql += " where t.groupName = '" + groupName + "'";
            if (userName != null) {
                sql += " and t.name like '%" + userName + "%'";
            }
            if (status != null) {
                if (status.equals("normal")) {
                    sql += " and t.status1 = '" + "normal" + "'";
                } else {
                    sql += " and t.status1 = '" + "abnormal" + "'";
                }
            }
            if (dormitory != null) {
                sql += " and t.dormitory = '" + dormitory + "'";
            }
            if (beginTime != null) {
                sql += " and t.date > '" + beginTime + "'";
            }
            if (endTime != null) {
                sql += " and t.date < '" + endTime + "'";
            }
            return TbTemperatureRecord.dao.find(sql);
        }
        if (dormitory != null) {
            sql += " where t.dormitory = " + dormitory + "";
            if (userName != null) {
                sql += " and t.name like '%" + userName + "%'";
            }
            if (status != null) {
                if (status.equals("normal")) {
                    sql += " and t.status1 = '" + "normal" + "'";
                } else {
                    sql += " and t.status1 = '" + "abnormal" + "'";
                }
            }
            if (groupName != null) {
                sql += " and t.groupName = '" + groupName + "'";
            }
            if (beginTime != null) {
                sql += " and t.date > '" + beginTime + "'";
            }
            if (endTime != null) {
                sql += " and t.date < '" + endTime + "'";
            }
            return TbTemperatureRecord.dao.find(sql);
        }
        if (beginTime != null) {
            sql += " where t.date > '" + beginTime + "'";
            if (userName != null) {
                sql += " and t.name like '%" + userName + "%'";
            }
            if (status != null) {
                if (status.equals("normal")) {
                    sql += " and t.status1 = '" + "normal" + "'";
                } else {
                    sql += " and t.status1 = '" + "abnormal" + "'";
                }
            }
            if (groupName != null) {
                sql += " and t.groupName = '" + groupName + "'";
            }
            if (dormitory != null) {
                sql += " and t.dormitory = '" + dormitory + "'";
            }
            if (endTime != null) {
                sql += " and t.date < '" + endTime + "'";
            }
            return TbTemperatureRecord.dao.find(sql);
        }
        if (endTime != null) {
            sql += " where t.date < '" + endTime + "'";
            if (userName != null) {
                sql += " and t.name like '%" + userName + "%'";
            }
            if (status != null) {
                if (status.equals("normal")) {
                    sql += " and t.status1 = '" + "normal" + "'";
                } else {
                    sql += " and t.status1 = '" + "abnormal" + "'";
                }
            }
            if (groupName != null) {
                sql += " and t.groupName = '" + groupName + "'";
            }
            if (dormitory != null) {
                sql += " and t.dormitory = '" + dormitory + "'";
            }
            if (beginTime != null) {
                sql += " and t.date > '" + beginTime + "'";
            }
            sql += sql + " ORDER BY t.date desc ";
            return TbTemperatureRecord.dao.find(sql );
        }
        return TbTemperatureRecord.dao.find(sql);
    }

    /**
     * 当前时间 加一天
     *
     * @param dateTime
     * @return
     * @throws ParseException
     */
    public String date01(String dateTime) throws ParseException {
        Date date = (new SimpleDateFormat("yyyy-MM-dd")).parse(dateTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
//        cal.add(Calendar.DATE, 1);
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cal.getTime()).toString().toString();
    }

    /**
     * 当前时间减一天
     */
    public String date02(String dateTime) throws ParseException {
        Date date = (new SimpleDateFormat("yyyy-MM-dd")).parse(dateTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
//        cal.add(Calendar.DATE, -1);
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(cal.getTime()).toString().toString();
    }

    /**
     * 根据用户查询 报表
     *
     * @param tbPersonnels
     * @return
     */
    public List<TbTemperatureRecord> findByStatement(TbPersonnel tbPersonnels) {
        if (tbPersonnels != null) {
            String name = tbPersonnels.getUserName() + "_" + tbPersonnels.getStudentNumber();
            return TbTemperatureRecord.dao.find("select * from tb_temperature_record where name = ?", name);
        }

        return null;
    }

    /**
     * 根据条件查询 报表
     *
     * @return
     */
    public List<TbTemperatureRecord> findCondition() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //格式时间
        String date = df.format(new Date(System.currentTimeMillis()));
        //当前时间的前5分钟
        String date2 = df.format(new Date(System.currentTimeMillis() - 300000));
        return TbTemperatureRecord.dao.find("select * from (select * from (select * from (SELECT t.*,p.*,(select FROM_UNIXTIME(t.scan_timestamp/1000,'%Y-%m-%d %T' )) \n" +
                "as date ,if(t.temperature>37.4||t.temperature<35.4,'abnormal','normal') \n" +
                "as status1,if(t.name = ''||t.name is null,'陌生人',p.userName) as name1\n" +
                "from tb_temperature_record t\n" +
                "LEFT JOIN tb_personnel p ON p.studentNumber = SUBSTRING_INDEX(t.name,'_',-1) || p.studentNumber = (select GetNum(t.name))\n" +
                "LEFT JOIN tb_group g on g.groupName = p.groupName) t) t ORDER BY t.date desc ) t where t.date between '" + date2 + "' and '" + date + "'");
    }

    /**
     * 根据发送成功的 修改表数据
     */
    public int updateDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //格式时间
        long date = System.currentTimeMillis();
        //当前时间的前5分钟
        long date2 = System.currentTimeMillis() - 300000;
        List<TbTemperatureRecord> recordList = TbTemperatureRecord.dao.find("select * from tb_temperature_record where scan_timestamp between '" + date2 + "' and '" + date + "'");

        int update = 0;
        for (TbTemperatureRecord tbTemperatureRecord : recordList) {
            update = Db.update("UPDATE tb_temperature_record t SET t.tag='0' WHERE t.scan_timestamp = '" + tbTemperatureRecord.getScanTimestamp() + "'");
        }
        return update;
    }

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //格式时间
        String format = df.format(new Date(System.currentTimeMillis()));
        //当前时间的前一秒
        String format2 = df.format(new Date(System.currentTimeMillis() - 1800000));


        System.out.println(format);
        System.out.println(format2);
    }

    /**
     * 查询报表所有的数据
     */
    public TbPersonnel findByPerName(String name) {
        return TbPersonnel.dao.findFirst("select * from tb_personnel where userName = ?", name);
    }

    public List<TbPersonnel> findPerAll() {
        return TbPersonnel.dao.find("select * from tb_personnel");
    }

    /**
     * 查询所有 的 报表信息
     *
     * @return
     */
    public int update() {
        List<TbTemperatureRecord> tbTemperatureRecords = TbTemperatureRecord.dao.find("select * from (select * from (select * from (SELECT t.*,p.*,(select FROM_UNIXTIME(t.scan_timestamp/1000,'%Y-%m-%d %T' )) \n" +
                "as date ,if(t.temperature>37.4||t.temperature<35.4,'abnormal','normal') \n" +
                "as status1,if(t.name = ''||t.name is null,'陌生人',p.userName) as name1\n" +
                "from tb_temperature_record t\n" +
                "LEFT JOIN tb_personnel p ON p.studentNumber = SUBSTRING_INDEX(t.name,'_',-1) || p.studentNumber = (select GetNum(t.name))\n" +
                "LEFT JOIN tb_group g on g.groupName = p.groupName) t) t ORDER BY t.date desc ) t where t.tag is null or t.tag = '' ");
        int update = 0;
        for (TbTemperatureRecord tbTemperatureRecord : tbTemperatureRecords) {
            update = Db.update("UPDATE tb_temperature_record t SET t.tag='0' WHERE t.tag is null or t.tag = '' ");
        }
        return update;
    }
}
