package com.example.mongo.vo;

import java.util.ArrayList;

/**
 * <pre>
 * Statements
 * </pre>
 *
 * @ClassName   : IF_DM_SUMS_CCU_001VO.java
 * @Description : 클래스 설명을 기술합니다.
 * @author Seo Sung Jin
 * @since 2021. 12. 15.
 * @version 1.0
 * @see
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2021. 12. 15.     Seo Sung Jin     	최초 생성
 * </pre>
 */

public class IF_DM_SUMS_CCU_001VO {

    // 이벤트 ID
    private String verId;
    // 이벤트 배포 키
    private String dpoyVerId;
    // 이벤트 등록 결과
    private String resultCd;
    // CDN URL 목록
    private ArrayList<CcuVO> cdnUrlList;
    
    // 2022.09.08 Add by SungJin Seo
    private String pckgSt;
    
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
     * @return the dpoyVerId
     */
    public String getDpoyVerId() {
        return dpoyVerId;
    }
    /**
     * @param dpoyVerId the dpoyVerId to set
     */
    public void setDpoyVerId(String dpoyVerId) {
        this.dpoyVerId = dpoyVerId;
    }
    /**
     * @return the resultCd
     */
    public String getResultCd() {
        return resultCd;
    }
    /**
     * @param resultCd the resultCd to set
     */
    public void setResultCd(String resultCd) {
        this.resultCd = resultCd;
    }
    /**
     * @return the cdnUrlList
     */
    public ArrayList<CcuVO> getCdnUrlList() {
        return cdnUrlList;
    }
    /**
     * @param cdnUrlList the cdnUrlList to set
     */
    public void setCdnUrlList(ArrayList<CcuVO> cdnUrlList) {
        this.cdnUrlList = cdnUrlList;
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
}
