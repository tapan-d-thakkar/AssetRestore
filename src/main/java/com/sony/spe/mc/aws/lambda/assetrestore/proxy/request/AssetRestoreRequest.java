package com.sony.spe.mc.aws.lambda.assetrestore.proxy.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by tthakkar on 10/24/17.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetRestoreRequest implements Serializable {

    private String itemId;

    private List<String> files;

    private Long expirationInDays;

    private String tier;

    private String bucketName;

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

    public Long getExpirationInDays() {
        return expirationInDays;
    }

    public void setExpirationInDays(Long expirationInDays) {
        this.expirationInDays = expirationInDays;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssetRestoreRequest that = (AssetRestoreRequest) o;

        if (itemId != null ? !itemId.equals(that.itemId) : that.itemId != null) return false;
        if (files != null ? !files.equals(that.files) : that.files != null) return false;
        if (expirationInDays != null ? !expirationInDays.equals(that.expirationInDays) : that.expirationInDays != null)
            return false;
        if (tier != null ? !tier.equals(that.tier) : that.tier != null) return false;
        return bucketName != null ? bucketName.equals(that.bucketName) : that.bucketName == null;
    }

    @Override
    public int hashCode() {
        int result = itemId != null ? itemId.hashCode() : 0;
        result = 31 * result + (files != null ? files.hashCode() : 0);
        result = 31 * result + (expirationInDays != null ? expirationInDays.hashCode() : 0);
        result = 31 * result + (tier != null ? tier.hashCode() : 0);
        result = 31 * result + (bucketName != null ? bucketName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AssetRestoreRequest{" +
                "itemId='" + itemId + '\'' +
                ", files=" + files +
                ", expirationInDays=" + expirationInDays +
                ", tier='" + tier + '\'' +
                ", bucketName='" + bucketName + '\'' +
                '}';
    }
}
