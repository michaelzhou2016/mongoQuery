package com.example.mongo.vo;

import java.io.Serializable;

public class EventRetryUploadVO implements Serializable {
    private String pckgNm = "";
    private String ftgtVer = "";
    private String verId;
    private Integer scomoFileId;
    private String pckgSt;
    private String fileTableName = "";
    private String fsRelationTableName = "";

    public String getPckgNm() {
        return pckgNm;
    }

    public void setPckgNm(String pckgNm) {
        this.pckgNm = pckgNm;
    }

    public String getFtgtVer() {
        return ftgtVer;
    }

    public void setFtgtVer(String ftgtVer) {
        this.ftgtVer = ftgtVer;
    }

    public String getVerId() {
        return verId;
    }

    public void setVerId(String verId) {
        this.verId = verId;
    }

    public Integer getScomoFileId() {
        return scomoFileId;
    }

    public void setScomoFileId(Integer scomoFileId) {
        this.scomoFileId = scomoFileId;
    }

    public String getPckgSt() {
        return pckgSt;
    }

    public void setPckgSt(String pckgSt) {
        this.pckgSt = pckgSt;
    }

    public String getFileTableName() {
        return this.fileTableName;
    }

    public void setFileTableName(String fileTableName) {
        this.fileTableName = fileTableName;
    }

    public String getFsRelationTableName() {
        return this.fsRelationTableName;
    }

    public void setFsRelationTableName(String fsRelationTableName) {
        this.fsRelationTableName = fsRelationTableName;
    }

    public void setTableName() {
        if ("DEV_ABLE".equals(this.pckgSt)) {
            this.fileTableName = "SCOMO_DEV_TEST_FILE";
            this.fsRelationTableName = "SCOMO_DEV_TEST_FILE_RELATION";
        } else if ("TEST_ABLE".equals(this.pckgSt)) {
            this.fileTableName = "SCOMO_TEST_FILE";
            this.fsRelationTableName = "SCOMO_TEST_FILE_RELATION";
        } else if ("DPOY_ABLE".equals(this.pckgSt)) {
            this.fileTableName = "SCOMO_FILE";
            this.fsRelationTableName = "SCOMO_FILE_RELATION";
        }
    }

    @Override
    public String toString() {
        return "EventRetryUploadVO{" +
                "pckgNm='" + pckgNm + '\'' +
                ", ftgtVer='" + ftgtVer + '\'' +
                ", verId='" + verId + '\'' +
                ", scomoFileId=" + scomoFileId +
                ", pckgSt='" + pckgSt + '\'' +
                ", fileTableName='" + fileTableName + '\'' +
                ", fsRelationTableName='" + fsRelationTableName + '\'' +
                '}';
    }
}
