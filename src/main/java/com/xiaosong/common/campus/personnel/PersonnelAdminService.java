package com.xiaosong.common.campus.personnel;

import com.jfinal.plugin.activerecord.Db;
import com.xiaosong.model.TbGroup;
import com.xiaosong.model.TbPersonnel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PersonnelAdminService {
    public static final PersonnelAdminService me = new PersonnelAdminService();

    /**
     * 查询 所有 人员所属组别
     * @return
     */
    public List<TbGroup> findByGroupName() {
        return TbGroup.dao.find("select groupName from tb_group");
    }

    /**
     * 查询所有 人员信息
     */
    public List<TbPersonnel> findAll() {
        return TbPersonnel.dao.findAll();
    }

    /**
     * 删除人员信息
     * @param id
     * @return
     */
    public int delete(String id) {
        return Db.delete("delete from tb_personnel WHERE id = ?",id);
    }


    public String importExcel(String groupName ,BufferedInputStream bis){
        Object[] insertvalue = new Object[48];
        int lastRowNum = 0;
        int physicalRowNum = 0;
        String ghflag = "ok";
        HSSFSheet st = null;
        POIFSFileSystem fs = null;
        HSSFWorkbook wb = null;
        try {
            fs = new POIFSFileSystem(bis);   //对文件进行解析
            wb = new HSSFWorkbook(fs);   //创建一个新的excel
            st = wb.getSheetAt(0);   //创建一个新的sheet页
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastRowNum = st.getLastRowNum();// getLastRowNum方法能够正确返回最后一行的位置
        physicalRowNum = st.getPhysicalNumberOfRows();// getPhysicalNumberOfRows方法能够正确返回物理的行数
        System.out.println("lastRowNum=" + lastRowNum + " physicalRowNum="
                + physicalRowNum);

        for (int r = 1; r <= st.getLastRowNum(); r++) {
            HSSFRow row = st.getRow(r);  //获取一行
            if (row != null) {//这里只判断行是不是空的额，没办法判断单元格的空串
                String adj = notNeed(groupName,insertvalue, row,r-1);
                if(adj == "no"){
                    ghflag = "no";
                    break;
                }
            }
        }

        return ghflag;
    }

    private String notNeed(String groupName,Object[] insertvalue, HSSFRow row,int order) {
        HSSFCell cell0 = row.getCell(0);
        for (int i=0;i<2;i++){
            HSSFCell cell1 = row.getCell(i);
            cell1.setCellType(CellType.STRING);
            insertvalue[i] = (cell1 !=null && cell1.getStringCellValue() != null && !cell1.getStringCellValue().equals(""))?cell1.getStringCellValue():"";
        }

        String userName   = (insertvalue[0]+"").trim() ==  null ? "" : (insertvalue[0]+"").trim() ;
        String gh   = (insertvalue[1]+"").trim() ==  null ? "" : (insertvalue[1]+"").trim() ;

//        if(grgh==null || "".equals(grgh)){
//            String sqlyf = "delete from T_COM_GRSD where ssyf='" + yf +"'";
//            Db.update(sqlyf);
//            return "no";
//        }
        TbPersonnel tbPersonnel = new TbPersonnel();
        tbPersonnel.setUserName(userName);
        tbPersonnel.setStudentNumber(Integer.valueOf(gh));
        tbPersonnel.setGroupName(groupName);
        tbPersonnel.setCreationDate(getDate());
        tbPersonnel.save();
        return "ok";
    }

    /**
     * 获取当前系统时间 年-月-日
     * @return
     */
    private String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String date = df.format(new Date()); // new Date()为获取当前系统时间
        return date;
    }

    /**
     * 模糊查询
     * @param userName
     */
    public TbPersonnel findByUserName(String userName) {
        return TbPersonnel.dao.findFirst("select *  from tb_personnel WHERE userName = ?", userName);
    }
}
