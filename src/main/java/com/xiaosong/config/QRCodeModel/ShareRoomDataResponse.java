package com.xiaosong.config.QRCodeModel;

import com.xiaosong.model.TbShareroom;

import java.util.List;

public class ShareRoomDataResponse {
	
	private int pageNum;
	
	private int pageSize;
	
	private List<TbShareroom> rows;
	
	private String totalPage;
	
	private String total;

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	

	public List<TbShareroom> getRows() {
		return rows;
	}

	public void setRows(List<TbShareroom> rows) {
		this.rows = rows;
	}

	public String getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(String totalPage) {
		this.totalPage = totalPage;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "ShareRoomDataResponse [pageNum=" + pageNum + ", pageSize=" + pageSize + ", rows=" + rows
				+ ", totalPage=" + totalPage + ", total=" + total + ", getPageNum()=" + getPageNum()
				+ ", getPageSize()=" + getPageSize() + ", getRows()=" + getRows() + ", getTotalPage()=" + getTotalPage()
				+ ", getTotal()=" + getTotal() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}
	
	
}
