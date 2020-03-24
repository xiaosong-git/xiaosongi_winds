package com.xiaosong.config.QRCodeModel;

public class ShareRoomResponse {

	private VerifyResponseModel verify;
	
	private ShareRoomDataResponse data;

	public VerifyResponseModel getVerify() {
		return verify;
	}

	public void setVerify(VerifyResponseModel verify) {
		this.verify = verify;
	}

	public ShareRoomDataResponse getData() {
		return data;
	}

	public void setData(ShareRoomDataResponse data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ShareRoomResponse [verify=" + verify + ", data=" + data + ", getVerify()=" + getVerify()
				+ ", getData()=" + getData() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}
	
	
}
