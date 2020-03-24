package com.xiaosong.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseTbVisitor<M extends BaseTbVisitor<M>> extends Model<M> implements IBean {

	public M setId(java.lang.String id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public M setVisitorName(java.lang.String visitorName) {
		set("visitorName", visitorName);
		return (M)this;
	}
	
	public java.lang.String getVisitorName() {
		return getStr("visitorName");
	}

	public M setVisitorIdCard(java.lang.String visitorIdCard) {
		set("visitorIdCard", visitorIdCard);
		return (M)this;
	}
	
	public java.lang.String getVisitorIdCard() {
		return getStr("visitorIdCard");
	}

	public M setUserId(java.lang.String userId) {
		set("userId", userId);
		return (M)this;
	}
	
	public java.lang.String getUserId() {
		return getStr("userId");
	}

	public M setSoleCode(java.lang.String soleCode) {
		set("soleCode", soleCode);
		return (M)this;
	}
	
	public java.lang.String getSoleCode() {
		return getStr("soleCode");
	}

	public M setByVisitorName(java.lang.String byVisitorName) {
		set("byVisitorName", byVisitorName);
		return (M)this;
	}
	
	public java.lang.String getByVisitorName() {
		return getStr("byVisitorName");
	}

	public M setByVisitorIdCard(java.lang.String byVisitorIdCard) {
		set("byVisitorIdCard", byVisitorIdCard);
		return (M)this;
	}
	
	public java.lang.String getByVisitorIdCard() {
		return getStr("byVisitorIdCard");
	}

	public M setVisitDate(java.lang.String visitDate) {
		set("visitDate", visitDate);
		return (M)this;
	}
	
	public java.lang.String getVisitDate() {
		return getStr("visitDate");
	}

	public M setVisitTime(java.lang.String visitTime) {
		set("visitTime", visitTime);
		return (M)this;
	}
	
	public java.lang.String getVisitTime() {
		return getStr("visitTime");
	}

	public M setStartDateTime(java.lang.String startDateTime) {
		set("startDateTime", startDateTime);
		return (M)this;
	}
	
	public java.lang.String getStartDateTime() {
		return getStr("startDateTime");
	}

	public M setEndDateTime(java.lang.String endDateTime) {
		set("endDateTime", endDateTime);
		return (M)this;
	}
	
	public java.lang.String getEndDateTime() {
		return getStr("endDateTime");
	}

	public M setOrgCode(java.lang.String orgCode) {
		set("orgCode", orgCode);
		return (M)this;
	}
	
	public java.lang.String getOrgCode() {
		return getStr("orgCode");
	}

	public M setProvince(java.lang.String province) {
		set("province", province);
		return (M)this;
	}
	
	public java.lang.String getProvince() {
		return getStr("province");
	}

	public M setCity(java.lang.String city) {
		set("city", city);
		return (M)this;
	}
	
	public java.lang.String getCity() {
		return getStr("city");
	}

	public M setCreateTime(java.lang.String createTime) {
		set("createTime", createTime);
		return (M)this;
	}
	
	public java.lang.String getCreateTime() {
		return getStr("createTime");
	}

	public M setVisitId(java.lang.String visitId) {
		set("visitId", visitId);
		return (M)this;
	}
	
	public java.lang.String getVisitId() {
		return getStr("visitId");
	}

	public M setDelFlag(java.lang.String delFlag) {
		set("delFlag", delFlag);
		return (M)this;
	}
	
	public java.lang.String getDelFlag() {
		return getStr("delFlag");
	}

	public M setIsSued(java.lang.String isSued) {
		set("isSued", isSued);
		return (M)this;
	}
	
	public java.lang.String getIsSued() {
		return getStr("isSued");
	}

	public M setDelPosted(java.lang.String delPosted) {
		set("delPosted", delPosted);
		return (M)this;
	}
	
	public java.lang.String getDelPosted() {
		return getStr("delPosted");
	}

	public M setIsPosted(java.lang.String isPosted) {
		set("isPosted", isPosted);
		return (M)this;
	}
	
	public java.lang.String getIsPosted() {
		return getStr("isPosted");
	}

	public M setPreStartTime(java.lang.String preStartTime) {
		set("preStartTime", preStartTime);
		return (M)this;
	}
	
	public java.lang.String getPreStartTime() {
		return getStr("preStartTime");
	}

	public M setIdFrontImgUrl(java.lang.String idFrontImgUrl) {
		set("idFrontImgUrl", idFrontImgUrl);
		return (M)this;
	}
	
	public java.lang.String getIdFrontImgUrl() {
		return getStr("idFrontImgUrl");
	}

	public M setPhoto(java.lang.String photo) {
		set("photo", photo);
		return (M)this;
	}
	
	public java.lang.String getPhoto() {
		return getStr("photo");
	}

}
