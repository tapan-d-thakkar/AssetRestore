package com.sony.spe.mc.aws.lambda.assetrestore.proxy.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tthakkar on 01/31/18.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetRestoreIntermediateResponse {
	
	private String itemId;
	
	private List<String> files = new ArrayList<String>();

	private Integer delay;

	private String tier;

	private String restoreStatus;

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(Integer delay) {
		this.delay = delay;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public String getRestoreStatus() {
		return restoreStatus;
	}

	public void setRestoreStatus(String restoreStatus) {
		this.restoreStatus = restoreStatus;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AssetRestoreIntermediateResponse response = (AssetRestoreIntermediateResponse) o;

		if (itemId != null ? !itemId.equals(response.itemId) : response.itemId != null) return false;
		if (files != null ? !files.equals(response.files) : response.files != null) return false;
		if (delay != null ? !delay.equals(response.delay) : response.delay != null) return false;
		if (tier != null ? !tier.equals(response.tier) : response.tier != null) return false;
		return restoreStatus != null ? restoreStatus.equals(response.restoreStatus) : response.restoreStatus == null;
	}

	@Override
	public int hashCode() {
		int result = itemId != null ? itemId.hashCode() : 0;
		result = 31 * result + (files != null ? files.hashCode() : 0);
		result = 31 * result + (delay != null ? delay.hashCode() : 0);
		result = 31 * result + (tier != null ? tier.hashCode() : 0);
		result = 31 * result + (restoreStatus != null ? restoreStatus.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "AssetRestoreIntermediateResponse{" +
				"itemId='" + itemId + '\'' +
				", files=" + files +
				", delay=" + delay +
				", tier='" + tier + '\'' +
				", restoreStatus='" + restoreStatus + '\'' +
				'}';
	}
}
