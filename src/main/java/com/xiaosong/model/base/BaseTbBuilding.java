package com.xiaosong.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseTbBuilding<M extends BaseTbBuilding<M>> extends Model<M> implements IBean {

	public M setOrgCode(java.lang.String orgCode) {
		set("orgCode", orgCode);
		return (M)this;
	}
	
	public java.lang.String getOrgCode() {
		return getStr("orgCode");
	}

	public M setOrgName(java.lang.String orgName) {
		set("orgName", orgName);
		return (M)this;
	}
	
	public java.lang.String getOrgName() {
		return getStr("orgName");
	}

	public M setPospCode(java.lang.String pospCode) {
		set("pospCode", pospCode);
		return (M)this;
	}
	
	public java.lang.String getPospCode() {
		return getStr("pospCode");
	}

	public M setNetType(java.lang.String netType) {
		set("netType", netType);
		return (M)this;
	}
	
	public java.lang.String getNetType() {
		return getStr("netType");
	}

	public M setFaceComparesCope(java.lang.String faceComparesCope) {
		set("faceComparesCope", faceComparesCope);
		return (M)this;
	}
	
	public java.lang.String getFaceComparesCope() {
		return getStr("faceComparesCope");
	}

	public M setAccessType(java.lang.String accessType) {
		set("accessType", accessType);
		return (M)this;
	}
	
	public java.lang.String getAccessType() {
		return getStr("accessType");
	}

	public M setKey(java.lang.String key) {
		set("key", key);
		return (M)this;
	}
	
	public java.lang.String getKey() {
		return getStr("key");
	}

	public M setVisitorCheckType(java.lang.String visitorCheckType) {
		set("visitorCheckType", visitorCheckType);
		return (M)this;
	}
	
	public java.lang.String getVisitorCheckType() {
		return getStr("visitorCheckType");
	}

	public M setStaffCheckType(java.lang.String staffCheckType) {
		set("staffCheckType", staffCheckType);
		return (M)this;
	}
	
	public java.lang.String getStaffCheckType() {
		return getStr("staffCheckType");
	}

	public M setShareCheckType(java.lang.String shareCheckType) {
		set("shareCheckType", shareCheckType);
		return (M)this;
	}
	
	public java.lang.String getShareCheckType() {
		return getStr("shareCheckType");
	}

}
