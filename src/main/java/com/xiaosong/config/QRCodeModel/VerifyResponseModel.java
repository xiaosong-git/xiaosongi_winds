package com.xiaosong.config.QRCodeModel;

public class VerifyResponseModel {
	private String sign;
	private String desc;

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "GetStaffScheduleVerifyResponseModel [sign=" + sign + ", desc=" + desc + "]";
	}
	
}
