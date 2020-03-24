package com.xiaosong.common.accessrecord;

import com.jfinal.core.Controller;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbAccessrecord;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 通行记录管理
 */
public class AccessRecordsController extends Controller {
    private AccessRecordsService srv = AccessRecordsService.me;
    private static Logger logger = Logger.getLogger(AccessRecordsController.class);

    /**
     * 获取所有的通行记录
     */
    public void index(){
        try {
            List<TbAccessrecord> accessrecords = srv.findAll();
            if(accessrecords==null||accessrecords.size()==0){
                logger.error("查询通行记录失败");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR,"查询 通行记录失败~"));
            }else{
                logger.info("查询通行记录成功");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL,accessrecords,accessrecords.size()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败"));
        }
    }
}
