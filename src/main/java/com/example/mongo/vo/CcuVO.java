package com.example.mongo.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * <pre>
 * Statements
 * </pre>
 *
 * @ClassName   : CcuVO.java
 * @Description : 클래스 설명을 기술합니다.
 * @author Seo Sung Jin
 * @since 2021. 12. 10.
 * @version 1.0
 * @see
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2021. 12. 10.     Seo Sung Jin     	최초 생성
 * </pre>
 */

public class CcuVO implements Serializable {

    /**
     * Statements
     * (long)serialVersionUID 
     */
    private static final long serialVersionUID = -8483780596466843173L;

    // 패키지 이름
    private String pckgNm;
    // 제어기 ID 소스 버전
    private String srcVer;
    // 제어기 ID 타겟 버전
    private String tgtVer;
    // 정합성 체크 시 Patch의 버전
    private String pTgtVer;
    // 제어기 ID
    @JsonProperty("SCOMO_NODE_NM")
    private String scomoNodeNm;
    // 종속된 이벤트 시리얼
    private String ftgtVer;
    // 저장 Type
    private String crudType;
    // 사용자 입력 파일명
    private String filLcNm;
    // 실제 파일명
    private String filRlNm;
    // 바이너리 파일 서버(NAS) 저장 위치
    private String filPath;
    // 파일 해쉬
    private String filHash;
    // 바이너리 파일 크기
    private String filSz;
    // 바이너리 파일의 형태
    private String binType;
    // CDN URL
    private String cdnUrl;
    // 제이기 별 CDN 등록 성공 여부
    private String cdnSuccYn;
    // 제이기 별 CDN 삭제 성공 여부 - 221011 hjy
    private String cdnDelSuccYn;
    // 이벤트 ID
    private String verId;
    // CDN 업로드 Retry 횟수
    private String retryCount;
    // 이벤트의 감증 상태
    private String pckgSt;
    // 수신시간
    private String receivetime;
    // 파일 ID
    private String scomoFilId;

    private String pkgDocNo;

    private String pkgDocSeq;

    private String swUnitId;

    private String swUnitSeq;

    public String getPkgDocNo() {
        return pkgDocNo;
    }

    public void setPkgDocNo(String pkgDocNo) {
        this.pkgDocNo = pkgDocNo;
    }

    public String getPkgDocSeq() {
        return pkgDocSeq;
    }

    public void setPkgDocSeq(String pkgDocSeq) {
        this.pkgDocSeq = pkgDocSeq;
    }

    public String getSwUnitId() {
        return swUnitId;
    }

    public void setSwUnitId(String swUnitId) {
        this.swUnitId = swUnitId;
    }

    public String getSwUnitSeq() {
        return swUnitSeq;
    }

    public void setSwUnitSeq(String swUnitSeq) {
        this.swUnitSeq = swUnitSeq;
    }

    // 제어기 명 2024.04.22 국내 보증시스템 연동을 위해 추가.
    @JsonProperty("ECU_NM")
    private String ecuNm;

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
     * @return the srcVer
     */
    public String getSrcVer() {
        return srcVer;
    }
    /**
     * @param srcVer the srcVer to set
     */
    public void setSrcVer(String srcVer) {
        this.srcVer = srcVer;
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
     * @return the pTgtVer
     */
    public String getpTgtVer() {
        return pTgtVer;
    }
    /**
     * @param pTgtVer the pTgtVer to set
     */
    public void setpTgtVer(String pTgtVer) {
        this.pTgtVer = pTgtVer;
    }
    /**
     * @return the scomoNodeNm
     */
    public String getScomoNodeNm() {
        return scomoNodeNm;
    }
    /**
     * @param scomoNodeNm the scomoNodeNm to set
     */
    public void setScomoNodeNm(String scomoNodeNm) {
        this.scomoNodeNm = scomoNodeNm;
    }
    /**
     * @return the ftgtVer
     */
    public String getFtgtVer() {
        return ftgtVer;
    }
    /**
     * @param ftgtVer the ftgtVer to set
     */
    public void setFtgtVer(String ftgtVer) {
        this.ftgtVer = ftgtVer;
    }
    /**
     * @return the crudType
     */
    public String getCrudType() {
        return crudType;
    }
    /**
     * @param crudType the crudType to set
     */
    public void setCrudType(String crudType) {
        this.crudType = crudType;
    }
    /**
     * @return the filLcNm
     */
    public String getFilLcNm() {
        return filLcNm;
    }
    /**
     * @param filLcNm the filLcNm to set
     */
    public void setFilLcNm(String filLcNm) {
        this.filLcNm = filLcNm;
    }
    /**
     * @return the filRlNm
     */
    public String getFilRlNm() {
        return filRlNm;
    }
    /**
     * @param filRlNm the filRlNm to set
     */
    public void setFilRlNm(String filRlNm) {
        this.filRlNm = filRlNm;
    }
    /**
     * @return the filPath
     */
    public String getFilPath() {
        return filPath;
    }
    /**
     * @param filPath the filPath to set
     */
    public void setFilPath(String filPath) {
        this.filPath = filPath;
    }
    /**
     * @return the filHash
     */
    public String getFilHash() {
        return filHash;
    }
    /**
     * @param filHash the filHash to set
     */
    public void setFilHash(String filHash) {
        this.filHash = filHash;
    }

    public String getFilSz() {
        return filSz;
    }

    public void setFilSz(String filSz) {
        this.filSz = filSz;
    }

    /**
     * @return the binType
     */
    public String getBinType() {
        return binType;
    }
    /**
     * @param binType the binType to set
     */
    public void setBinType(String binType) {
        this.binType = binType;
    }
    /**
     * @return the cdnUrl
     */
    public String getCdnUrl() {
        return cdnUrl;
    }
    /**
     * @param cdnUrl the cdnUrl to set
     */
    public void setCdnUrl(String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }
    /**
     * @return the cdnSuccYn
     */
    public String getCdnSuccYn() {
        return cdnSuccYn;
    }
    /**
     * @param cdnSuccYn the cdnSuccYn to set
     */
    public void setCdnSuccYn(String cdnSuccYn) {
        this.cdnSuccYn = cdnSuccYn;
    }
    /**
     * @return the cdnDelSuccYn
     */
    public String getCdnDelSuccYn() {
        return cdnDelSuccYn;
    }
    /**
     * @param cdnDelSuccYn the cdnDelSuccYn to set
     */
    public void setCdnDelSuccYn(String cdnDelSuccYn) {
        this.cdnDelSuccYn = cdnDelSuccYn;
    }
    /**
     * @return the verId
     */
    public String getVerId() {
        return verId;
    }
    /**
     * @param verId the verId to set
     */
    public void setVerId(String verId) {
        this.verId = verId;
    }
    /**
     * @return the retryCount
     */
    public String getRetryCount() {
        return retryCount;
    }
    /**
     * @param retryCount the retryCount to set
     */
    public void setRetryCount(String retryCount) {
        this.retryCount = retryCount;
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
     * @return the receivetime
     */
    public String getReceivetime() {
        return receivetime;
    }
    /**
     * @param receivetime the receivetime to set
     */
    public void setReceivetime(String receivetime) {
        this.receivetime = receivetime;
    }
    /**
     * @return the scomoFilId
     */
    public String getScomoFilId() {
        return scomoFilId;
    }
    /**
     * @param scomoFilId the scomoFilId to set
     */
    public void setScomoFilId(String scomoFilId) {
        this.scomoFilId = scomoFilId;
    }

    public String getEcuNm() {
        return ecuNm;
    }

    public void setEcuNm(String ecuNm) {
        this.ecuNm = ecuNm;
    }
}
