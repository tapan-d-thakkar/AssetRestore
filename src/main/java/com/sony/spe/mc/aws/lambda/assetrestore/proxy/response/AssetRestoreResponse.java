package com.sony.spe.mc.aws.lambda.assetrestore.proxy.response;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by tthakkar on 10/24/17.
 */
@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetRestoreResponse implements Serializable {

    private String processInstanceId;

    private String message;

    private String status;

    private Date restoreExpirationTime;

    private String itemId;

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getRestoreExpirationTime() {
        return restoreExpirationTime;
    }

    public void setRestoreExpirationTime(Date restoreExpirationTime) {
        this.restoreExpirationTime = restoreExpirationTime;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssetRestoreResponse response = (AssetRestoreResponse) o;

        if (processInstanceId != null ? !processInstanceId.equals(response.processInstanceId) : response.processInstanceId != null)
            return false;
        if (message != null ? !message.equals(response.message) : response.message != null) return false;
        if (status != null ? !status.equals(response.status) : response.status != null) return false;
        if (restoreExpirationTime != null ? !restoreExpirationTime.equals(response.restoreExpirationTime) : response.restoreExpirationTime != null)
            return false;
        return itemId != null ? itemId.equals(response.itemId) : response.itemId == null;
    }

    @Override
    public int hashCode() {
        int result = processInstanceId != null ? processInstanceId.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (restoreExpirationTime != null ? restoreExpirationTime.hashCode() : 0);
        result = 31 * result + (itemId != null ? itemId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AssetRestoreResponse{" +
                "processInstanceId='" + processInstanceId + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", restoreExpirationTime=" + restoreExpirationTime +
                ", itemId='" + itemId + '\'' +
                '}';
    }
}
