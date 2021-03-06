package com.xiaosong.model.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseTbAccessrecord<M extends BaseTbAccessrecord<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public M setOrgCode(java.lang.String orgCode) {
		set("orgCode", orgCode);
		return (M)this;
	}
	
	public java.lang.String getOrgCode() {
		return getStr("orgCode");
	}

	public M setPospCode(java.lang.String pospCode) {
		set("pospCode", pospCode);
		return (M)this;
	}
	
	public java.lang.String getPospCode() {
		return getStr("pospCode");
	}

	public M setScanDate(java.lang.String scanDate) {
		set("scanDate", scanDate);
		return (M)this;
	}
	
	public java.lang.String getScanDate() {
		return getStr("scanDate");
	}

	public M setScanTime(java.lang.String scanTime) {
		set("scanTime", scanTime);
		return (M)this;
	}
	
	public java.lang.String getScanTime() {
		return getStr("scanTime");
	}

	public M setInOrOut(java.lang.String inOrOut) {
		set("inOrOut", inOrOut);
		return (M)this;
	}
	
	public java.lang.String getInOrOut() {
		return getStr("inOrOut");
	}

	public M setOutNumber(java.lang.String outNumber) {
		set("outNumber", outNumber);
		return (M)this;
	}
	
	public java.lang.String getOutNumber() {
		return getStr("outNumber");
	}

	public M setDeviceType(java.lang.String deviceType) {
		set("deviceType", deviceType);
		return (M)this;
	}
	
	public java.lang.String getDeviceType() {
		return getStr("deviceType");
	}

	public M setDeviceIp(java.lang.String deviceIp) {
		set("deviceIp", deviceIp);
		return (M)this;
	}
	
	public java.lang.String getDeviceIp() {
		return getStr("deviceIp");
	}

	public M setUserType(java.lang.String userType) {
		set("userType", userType);
		return (M)this;
	}
	
	public java.lang.String getUserType() {
		return getStr("userType");
	}

	public M setUserName(java.lang.String userName) {
		set("userName", userName);
		return (M)this;
	}
	
	public java.lang.String getUserName() {
		return getStr("userName");
	}

	public M setIdCard(java.lang.String idCard) {
		set("idCard", idCard);
		return (M)this;
	}
	
	public java.lang.String getIdCard() {
		return getStr("idCard");
	}

	public M setCardNO(java.lang.String cardNO) {
		set("cardNO", cardNO);
		return (M)this;
	}
	
	public java.lang.String getCardNO() {
		return getStr("cardNO");
	}

	public M setIsSendFlag(java.lang.String isSendFlag) {
		set("isSendFlag", isSendFlag);
		return (M)this;
	}
	
	public java.lang.String getIsSendFlag() {
		return getStr("isSendFlag");
	}

}
