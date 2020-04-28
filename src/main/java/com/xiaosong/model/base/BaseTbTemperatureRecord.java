package com.xiaosong.model.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseTbTemperatureRecord<M extends BaseTbTemperatureRecord<M>> extends Model<M> implements IBean {

	public M setChannelId(Integer channelId) {
		set("channel_id", channelId);
		return (M)this;
	}

	public Integer getChannelId() {
		return getInt("channel_id");
	}

	public M setFrameId(Integer frameId) {
		set("frame_id", frameId);
		return (M)this;
	}

	public Integer getFrameId() {
		return getInt("frame_id");
	}

	public M setTrackId(Integer trackId) {
		set("track_id", trackId);
		return (M)this;
	}

	public Integer getTrackId() {
		return getInt("track_id");
	}

	public M setScanTimestamp(Long scanTimestamp) {
		set("scan_timestamp", scanTimestamp);
		return (M)this;
	}

	public Long getScanTimestamp() {
		return getLong("scan_timestamp");
	}

	public M setImagePath(String imagePath) {
		set("image_path", imagePath);
		return (M)this;
	}

	public String getImagePath() {
		return getStr("image_path");
	}

	public M setTemperature(Double temperature) {
		set("temperature", temperature);
		return (M)this;
	}

	public Double getTemperature() {
		return getDouble("temperature");
	}

	public M setName(String name) {
		set("name", name);
		return (M)this;
	}

	public String getName() {
		return getStr("name");
	}

	public M setScore(Float score) {
		set("score", score);
		return (M)this;
	}

	public Float getScore() {
		return getFloat("score");
	}

	public M setTag(String tag) {
		set("tag", tag);
		return (M)this;
	}

	public String getTag() {
		return getStr("tag");
	}


}
