package com.xiaosong.common.personnel;

import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.upload.UploadFile;
import com.xiaosong.config.InitHCNetSDK;
import com.xiaosong.config.devicesInit;
import com.xiaosong.constant.Constants;
import com.xiaosong.constant.ErrorCodeDef;
import com.xiaosong.model.TbCompanyuser;
import com.xiaosong.model.TbDevice;
import com.xiaosong.util.Base64_2;
import com.xiaosong.util.FilesUtils;
import com.xiaosong.util.RetUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 人员管理
 **/
public class PersonController extends Controller {
    private PersonService srv = PersonService.me;
    private Logger logger = Logger.getLogger(PersonController.class);

    /**
     * 人脸下发
     */
    public void sendFace() {
        try {
            //获取前台数据
            Map<String, String> map = new HashMap<String, String>();

            String contralFloor = getPara("companyFloor");      //控制楼层*
            map.put(Constants.companyFloor, contralFloor);           //控制楼层*
            String photo1 = getPara("photo");
            System.out.println(PathKit.getWebRootPath());
            map.put(Constants.photoPath, PathKit.getWebRootPath() + "/img/" + getPara("photo"));     //图片*
            map.put(Constants.userId, getPara("userId"));       //用户id
            map.put(Constants.userName, getPara("userName"));   //用户姓名*
            map.put(Constants.idNo, getPara("idNO"));           //身份证号码 海景下发参数
            List<TbDevice> devices = srv.findByFloor(contralFloor);
            if (devices == null || devices.size() == 0) {
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "配置的设备没有该对应的楼层~"));
            } else {
                boolean isSuccess = true;

                TbCompanyuser tc = getModel(TbCompanyuser.class);
                for (TbDevice device : devices) {
                    String deviceType = device.getDeviceType();          //设备型号*
                    map.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                    map.put(Constants.username, device.getAdmin());      //设备用户名*
                    map.put(Constants.password, device.getPassword());   //设备密码*
                    if (deviceType.equals("DH-ASI728")) {
                        //大华设备人脸下发
                        //初始化大华设备
                        devicesInit.initDH();
                        isSuccess = srv.insertInfo(map);

                        if (isSuccess) {
                            logger.info("大华设备添加成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "大华设备添加成功~"));
                        } else {
                            logger.error("大华设备添加失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "大华设备添加失败~"));
                        }
                    } else if (deviceType.equals("DS-K5671")) {
                        //海康门禁设备人脸下发
                        //linux 初始化 海康设备
//                        InitHCNetSDK.run(deviceType);
                        //winds 初始化海康设备
                        devicesInit.initHC();
                        isSuccess = srv.insertInfoHKGuard(map);
                        if (isSuccess) {
                            logger.info("海康门禁添加成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海康门禁添加成功~"));
                        } else {
                            logger.error("海康门禁添加失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海康门禁添加失败"));
                        }
                    } else if (deviceType.equals("TPS980")) {
                        //海景设备人脸下发
                        map.put(Constants.currentStatus, "normal");   //员工状态 海景下发参数
                        String photo = null;
                        File file = new File(map.get(Constants.photoPath));
                        photo = Base64_2.encode(FilesUtils.getBytesFromFile(file));
                        isSuccess = srv.sendFaceHJ(map.get(Constants.deviceIp), map, photo);
                        if (isSuccess) {
                            logger.info("海景设备添加成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海景设备添加成功~"));
                        } else {
                            logger.error("海景设备添加失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海景设备添加失败~"));
                        }
                    }
                    if (!isSuccess) {
                        break;
                    }
                }
                if (isSuccess) {

                    boolean bool = srv.addData(map, tc, photo1);
                    if (bool) {
                        logger.info("添加人员信息成功~");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "添加人员信息成功~"));
                    } else {
                        logger.error("添加人员信息失败~");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "添加人员信息失败~"));
                    }
                }

            }

        } catch (
                UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error("添加异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "添加异常"));
        }
    }

    /**
     * 修改人脸
     */
    public void update() {
        try {
            //获取前台数据
            Map<String, String> map = new HashMap<String, String>();

            String contralFloor = getPara("companyFloor");      //控制楼层*
            map.put(Constants.companyFloor, contralFloor);            //控制楼层*
            String photo1 = getPara("photo");
            map.put(Constants.photoPath, PathKit.getWebRootPath() + "/img/" + getPara("photo"));     //图片*
            map.put(Constants.userId, getPara("userId"));       //用户id
            map.put(Constants.userName, getPara("userName"));   //用户姓名*
            map.put(Constants.idNo, getPara("idNO"));           //身份证号码 海景下发参数
            map.put(Constants.companyUserId, getPara("companyUserId"));  //用户主键

            boolean isSuccess = true;
            List<TbDevice> devices = srv.findByFloor(contralFloor);
            if (devices == null || devices.size() == 0) {
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "配置的设备没有该对应的楼层~"));
            } else {
                TbCompanyuser tc = getModel(TbCompanyuser.class);
                for (TbDevice device : devices) {
                    String deviceType = device.getDeviceType();          //设备型号*
                    map.put(Constants.deviceIp, device.getDeviceIp());   //设备ip*
                    map.put(Constants.username, device.getAdmin());      //设备用户名*
                    map.put(Constants.password, device.getPassword());   //设备密码*
                    if (deviceType.equals("DH-ASI728")) {
                        //大华设备人脸下发
                        //初始化大华设备
                        devicesInit.initDH();
                        isSuccess = srv.updateDH(map);
                        if (isSuccess) {
                            logger.info("大华设备修改成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "大华设备修改成功~"));
                        } else {
                            logger.error("大华设备修改失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "大华设备修改失败~"));
                        }
                    } else if (deviceType.equals("DS-K5671")) {
                        //海康门禁设备人脸下发
                        //linux 下 初始化海康设备
//                        InitHCNetSDK.run(deviceType);
                        //winds下 初始化海康设备
                        devicesInit.initHC();
                        isSuccess = srv.insertInfoHKGuard(map);
                        if (isSuccess) {
                            logger.info("海康门禁修改成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海康门禁修改成功~"));
                        } else {
                            logger.error("海康门禁修改失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海康门禁修改失败~"));
                        }
                    } else if (deviceType.equals("TPS980")) {
                        //海景设备人脸下发
                        map.put(Constants.currentStatus, "normal");   //员工状态 海景下发参数
                        String photo = null;
                        File file = new File(map.get(Constants.photoPath));
                        photo = Base64_2.encode(FilesUtils.getBytesFromFile(file));
                        isSuccess = srv.sendFaceHJ(map.get(Constants.deviceIp), map, photo);
                        if (isSuccess) {
                            logger.info("海景设备修改成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海景设备修改成功~"));
                        } else {
                            logger.error("海景设备修改失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海景设备修改失败~"));
                        }
                    }
                    if (!isSuccess) {
                        break;
                    }
                }
                if (isSuccess) {
                    int bool = srv.updateData(map, photo1);
                    if (bool == 1) {
                        logger.info("修改员工信息成功~");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "修改员工信息添加成功~"));
                    } else {
                        logger.error("修改员工信息添加失败~");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "修改员工信息添加失败~"));
                    }
                }

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error("修改异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "修改异常~"));
        }
    }

    /**
     * 删除数据
     */
    public void delete() {
        try {
            //获取前端数据
            String companyUserId = getPara("companyUserId");
            //根据 用户主键查询 用户所在楼层
            TbCompanyuser companyuser = srv.findCompanyUser(companyUserId);
            if (companyuser != null) {
                //根据 用户所在楼层 查询设备
                boolean del = true;
                List<TbDevice> devices = srv.findByFloor(companyuser.getCompanyFloor());
                Map<String, String> map = new HashMap<String, String>();
                for (TbDevice device : devices) {
                    map.put(Constants.deviceIp, device.getDeviceIp()); //设备ip
                    map.put(Constants.username, device.getAdmin());    //设备用户名
                    map.put(Constants.password, device.getPassword()); //设备密码
                    if (device.getDeviceType().equals("DH-ASI728")) {
                        //删除大华设备
                        devicesInit.initDH();
                        Integer cardInfo = companyuser.getCardInfo();
                        del = srv.deleteDH(map, cardInfo);
                        if (del) {
                            logger.info("大华删除人脸成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "大华删除人脸成功~"));
                        } else {
                            logger.error("大华删除人脸失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "大华删除人脸失败~"));
                        }
                    } else if (device.getDeviceType().equals("DS-K5671")) {
                        //删除海康设备
                        //linux
//                        InitHCNetSDK.run(device.getDeviceType());
                        //初始化海康
                        devicesInit.initHC();
                        String strCardNo = "S" + companyuser.getUserId();
                        del = srv.setCardInfo(device.getDeviceIp(), companyuser.getUserId(),
                                companyuser.getUserName(), strCardNo, "delete", map);
                        if (del) {
                            logger.info("海康设备删除人脸成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海康设备删除人脸成功~"));
                        } else {
                            logger.error("海康设备删除人脸失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海康设备删除人脸失败~"));
                        }

                    } else if (device.getDeviceType().equals("TPS980")) {
                        //删除海景设备
                        map.put(Constants.currentStatus, "delete");
                        map.put(Constants.idNo, companyuser.getIdNO());
                        map.put(Constants.userName, companyuser.getUserName());
                        del = srv.deleteFaceHJ(device.getDeviceIp(), map, null);
                        if (del) {
                            logger.info("海景设备删除人脸成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海景设备删除人脸成功~"));
                        } else {
                            logger.error("海景设备删除人脸失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海景设备删除人脸失败~"));
                        }
                    }
                    if (!del) {
                        break;
                    }
                }
                if (del) {
                    int delete = srv.delete(companyUserId);
                    if (delete == 1) {
                        logger.info("用户删除成功~");
                        renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "用户删除成功~"));
                    } else {
                        logger.error("用户删除失败~");
                        renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "用户删除失败~"));
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "删除异常~"));
        }

    }

    /**
     * 查询所有的 员工
     */
    public void index() {
        try {
            List<TbCompanyuser> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = Integer.parseInt(getPara("pageSize"));
            int index = (page - 1) * number;
            List<TbCompanyuser> companyuser = srv.findCompanyuser();
            for (int i = index; i < companyuser.size() && i < (index + number); i++) {
                list.add(companyuser.get(i));
            }
            if (companyuser != null) {
                logger.info("员工信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, companyuser.size()));
            } else {
                logger.error("员工信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "员工信息查询失败"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "查询异常~"));
        }
    }

    /**
     * 模糊查询
     */
    public void findByName() {
        try {
            List<TbCompanyuser> list = new ArrayList<>();
            int page = Integer.parseInt(getPara("currentPage"));
            int number = Integer.parseInt(getPara("pageSize"));
            int index = (page - 1) * number;
            String userName = getPara("userName");
            List<TbCompanyuser> companyusers = null;
            if (userName != null) {
                companyusers = srv.findByName(userName);
            } else {
                companyusers = srv.findCompanyuser();
            }

            for (int i = index; i < companyusers.size() && i < (index + number); i++) {
                list.add(companyusers.get(i));
            }
            if (companyusers != null) {
                logger.info("员工信息查询成功~");
                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, list, companyusers.size()));
            } else {
                logger.error("员工信息查询失败~");
                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "员工信息查询失败"));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("查询异常~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "查询异常~"));
        }
    }

    /**
     * 获取图片
     */
    public void pic() {

        try {
            UploadFile photo = getFile();
            System.out.println(photo.getUploadPath());
            if (photo != null) {
                logger.info("图片上传成功,上传的图片名" + photo.getFileName());
                renderJson(RetUtil.ok(photo.getFileName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("图片上传失败");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "图片上传失败~"));
        }
    }

    /**
     * 批量删除人员
     */
    public void batchDel() {
        try {
            String companyUserId = getPara("companyUserId");
            String[] split = companyUserId.split(",");
            for (String userId : split) {
                //根据 用户主键查询 用户所在楼层
                TbCompanyuser companyuser = srv.findCompanyUser(userId);
                if (companyuser != null) {
                    //根据 用户所在楼层 查询设备
                    boolean del = true;
                    List<TbDevice> devices = srv.findByFloor(companyuser.getCompanyFloor());
                    Map<String, String> map = new HashMap<String, String>();
                    for (TbDevice device : devices) {
                        map.put(Constants.deviceIp, device.getDeviceIp()); //设备ip
                        map.put(Constants.username, device.getAdmin());    //设备用户名
                        map.put(Constants.password, device.getPassword()); //设备密码
                        if (device.getDeviceType().equals("DH-ASI728")) {
                            //删除大华设备
                            devicesInit.initDH();
                            Integer cardInfo = companyuser.getCardInfo();
                            del = srv.deleteDH(map, cardInfo);
                            if (del) {
                                logger.info("大华删除人脸成功~");
                                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "大华删除人脸成功~"));
                            } else {
                                logger.error("大华删除人脸失败~");
                                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "大华删除人脸失败~"));
                            }
                        } else if (device.getDeviceType().equals("DS-K5671")) {
                            //删除海康设备
                            //linux
                            InitHCNetSDK.run(device.getDeviceType());
                            //初始化海康
//                            devicesInit.initHC();
                            String strCardNo = "S" + companyuser.getUserId();
                            del = srv.setCardInfo(device.getDeviceIp(), companyuser.getUserId(),
                                    companyuser.getUserName(), strCardNo, "delete", map);
                            if (del) {
                                logger.info("海康设备删除人脸成功~");
                                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海康设备删除人脸成功~"));
                            } else {
                                logger.error("海康设备删除人脸失败~");
                                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海康设备删除人脸失败~"));
                            }

                        } else if (device.getDeviceType().equals("TPS980")) {
                            //删除海景设备
                            map.put(Constants.currentStatus, "delete");
                            map.put(Constants.idNo, companyuser.getIdNO());
                            map.put(Constants.userName, companyuser.getUserName());
                            del = srv.deleteFaceHJ(device.getDeviceIp(), map, null);
                            if (del) {
                                logger.info("海景设备删除人脸成功~");
                                renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "海景设备删除人脸成功~"));
                            } else {
                                logger.error("海景设备删除人脸失败~");
                                renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "海景设备删除人脸失败~"));
                            }
                        }
                        if (!del) {
                            break;
                        }
                    }
                    if (del) {
                        int delete = srv.delete(userId);
                        if (delete == 1) {
                            logger.info("用户删除成功~");
                            renderJson(RetUtil.ok(ErrorCodeDef.CODE_NORMAL, "用户删除成功~"));
                        } else {
                            logger.error("用户删除失败~");
                            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "用户删除失败~"));
                        }
                    }

                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error("操作失败~");
            renderJson(RetUtil.fail(ErrorCodeDef.CODE_ERROR, "操作失败~"));
        }

    }
}
