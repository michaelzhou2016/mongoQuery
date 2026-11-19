 package com.example.mongo.vo;

import lombok.ToString;

/**
 * <pre>
 * Statements
 * </pre>
 *
 * @ClassName   : DpoySuccFailStatVO.java
 * @Description : 클래스 설명을 기술합니다.
 * @author Seo Sung Jin
 * @since 2022. 7. 25.
 * @version 1.0
 * @see
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2022. 7. 25.     Seo Sung Jin     	최초 생성
 * </pre>
 */

@ToString
public class DpoySuccFailStatVO {

    private String rgstD;
    private String pckgNm;
    private String pckgSt;
    private String carMdl;
    private String regn;
    private String tgtVer;
    private String verNm;
    private String partNo;
    private int succVinCnt = 0;
    private int failVinCnt = 0;
    private int etcVinCnt = 0;
    private int ingVinCnt = 0;
    private int totVinCnt = 0;
    /**
     * @return the rgstDt
     */
    public String getRgstD() {
        return rgstD;
    }
    /**
     * @param rgstDt the rgstDt to set
     */
    public void setRgstD(String rgstD) {
        this.rgstD = rgstD;
    }
    /**
     * @return the pckgNm
     */
    public String getPckgNm() {
        return pckgNm;
    }
    /**
     * @param pckgNm the pckgNm to set
     */
    public void setPckgNm(String pckgNm) {
        this.pckgNm = pckgNm;
    }
    /**
     * @return the pckgSt
     */
    public String getPckgSt() {
        return pckgSt;
    }
    /**
     * @param pckgSt the pckgSt to set
     */
    public void setPckgSt(String pckgSt) {
        this.pckgSt = pckgSt;
    }
    /**
     * @return the carMdl
     */
    public String getCarMdl() {
        return carMdl;
    }
    /**
     * @param carMdl the carMdl to set
     */
    public void setCarMdl(String carMdl) {
        this.carMdl = carMdl;
    }
    /**
     * @return the regn
     */
    public String getRegn() {
        return regn;
    }
    /**
     * @param regn the regn to set
     */
    public void setRegn(String regn) {
        this.regn = regn;
    }
    /**
     * @return the tgtVer
     */
    public String getTgtVer() {
        return tgtVer;
    }
    /**
     * @param tgtVer the tgtVer to set
     */
    public void setTgtVer(String tgtVer) {
        this.tgtVer = tgtVer;
    }
    /**
     * @return the verNm
     */
    public String getVerNm() {
        return verNm;
    }
    /**
     * @param verNm the verNm to set
     */
    public void setVerNm(String verNm) {
        this.verNm = verNm;
    }
    /**
     * @return the partNo
     */
    public String getPartNo() {
        return partNo;
    }
    /**
     * @param partNo the partNo to set
     */
    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }
    /**
     * @return the succVinCnt
     */
    public int getSuccVinCnt() {
        return succVinCnt;
    }
    /**
     * @param succVinCnt the succVinCnt to set
     */
    public void setSuccVinCnt(int succVinCnt) {
        this.succVinCnt = succVinCnt;
    }
    /**
     * @return the failVinCnt
     */
    public int getFailVinCnt() {
        return failVinCnt;
    }
    /**
     * @param failVinCnt the failVinCnt to set
     */
    public void setFailVinCnt(int failVinCnt) {
        this.failVinCnt = failVinCnt;
    }
    /**
     * @return the etcVinCnt
     */
    public int getEtcVinCnt() {
        return etcVinCnt;
    }
    /**
     * @param etcVinCnt the etcVinCnt to set
     */
    public void setEtcVinCnt(int etcVinCnt) {
        this.etcVinCnt = etcVinCnt;
    }
    /**
     * @return the ingVinCnt
     */
    public int getIngVinCnt() {
        return ingVinCnt;
    }
    /**
     * @param ingVinCnt the ingVinCnt to set
     */
    public void setIngVinCnt(int ingVinCnt) {
        this.ingVinCnt = ingVinCnt;
    }
    /**
     * @return the totVinCnt
     */
    public int getTotVinCnt() {
        return totVinCnt;
    }
    /**
     * @param totVinCnt the totVinCnt to set
     */
    public void setTotVinCnt(int totVinCnt) {
        this.totVinCnt = totVinCnt;
    }
}
