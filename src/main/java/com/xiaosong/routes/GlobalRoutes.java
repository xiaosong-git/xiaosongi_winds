package com.xiaosong.routes;

import com.jfinal.config.Routes;
import com.xiaosong.common.LoginController;
import com.xiaosong.common.accessrecord.AccessRecordsController;
import com.xiaosong.common.campus.group.GroupAdminController;
import com.xiaosong.common.campus.personnel.PersonnelAdminController;
import com.xiaosong.common.campus.statement.StatementController;
import com.xiaosong.common.device.DeviceController;
import com.xiaosong.common.device.DeviceRelatedController;
import com.xiaosong.common.floor.FloorController;
import com.xiaosong.common.licence.LicenseController;
import com.xiaosong.common.pass.PassWayController;
import com.xiaosong.common.penetrate.PenetrateController;
import com.xiaosong.common.personnel.PersonController;
import com.xiaosong.common.server.ServerController;
import com.xiaosong.common.server.ServerService;

/**
 * 所有控制器配置地址以及route级别过滤器
 * @author wgm
 * create by 2019-11-05
 *
 */
public class GlobalRoutes extends Routes{

	@Override
	public void config() {
		/**配置说明 controllerKey为Controller的前缀，如UserController的前缀为User
		 *   配置路径                                实际访问路径
		 * controllerKey        YourController.index()
		 * controllerKey/method YourController.method()
		 * controllerKey/method/v0-v1 YourController.method() 
		 * controllerKey/v0-v1 YourController.index()，所带 url 参数值为：v0-v1
		 */
		
		//this.add(controllerKey, controllerClass);
		//该处还可配置route级别的拦截器，对N个含有共同拦截器的控制层实现统一配置，减少代码冗余

		//路由路劲
		//设备配置
		this.add("/device", DeviceController.class);
		//设备使用配置
		this.add("/dr", DeviceRelatedController.class);
		//大楼配置
		this.add("/floor", FloorController.class);
		//licence管理
		this.add("/license", LicenseController.class);
		//通行方式配置
		this.add("/passWay", PassWayController.class);
		//人员管理配置
		this.add("/person", PersonController.class);
		//服务器配置
		this.add("/server", ServerController.class);
		//通行记录管理
		this.add("/access", AccessRecordsController.class);
		//登录
		this.add("/login", LoginController.class);
		//内网穿透
		this.add("/penetrate", PenetrateController.class);
		//分组管理
		this.add("/group", GroupAdminController.class);
		//人员管理
		this.add("/per", PersonnelAdminController.class);
		//报表统计
		this.add("/statement", StatementController.class);

	}
}
