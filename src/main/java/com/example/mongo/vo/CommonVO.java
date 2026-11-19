package com.example.mongo.vo;

/**
 * <pre>
 * Statements
 * </pre>
 *
 * @author 석중환
 * @version 1.0
 * @ClassName : CommonPaginationVO.java
 * @Description : 공통 VO
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2017. 8. 11.     석중환        최초 생성
 * </pre>
 * @see
 * @since 2017. 8. 11.
 */

public class CommonVO {

    //등록자
    private String rgn;
    //등록 일시
    private String rgstDt;
    //수정자
    private String updr;
    //수정일시
    private String updeDt;
    //offset
    private int offset = 0;
    //limit
    private int limit = 0;
    //현재 페이지
    private int currPage = 0;
    //언어코드
    private String languageCd;
    //검색어
    private String searchKeyword;
    private String searchAesKeyword;
    //검색종류
    private String searchCondition;
    //데이터 권한
    private String[] dataRole;
    //지역 권한
    private String[] regionRole;
    //플랫폼 권한
    private String[] platformRole;
    //등록권한
    private String authRoleCrteYn;
    //수정권한
    private String authRoleUpdeYn;
    //삭제권한
    private String authRoleDelYn;
    //승인권한
    private String authRoleIqYn;
    // 내보내기권한
    private String authRoleExportYn;
    //권한그룹
    private String authGrpCd;

    //유저아이디
    private String userId;
    //메뉴주소
    private String menuAdr;
    // 권한 ID (기존에 메뉴주소로 기입되어 있어 수정 -- 2021.12.16 천영락)
    private int authId;

    //패키지
    private String selectPckgNm;
    //지역
    private String selectRegion;
    //플랫폼
    private String SelectPlatform;

    //단말 암호화 해제 여부
    private String dvViewYn;
    //마스킹 해제 여부
    private String maskYn;

    private String uri;

    // 마스킹 해제 요청
    private String unmaskRequest;

    // 마스킹 해제 권한
    private String unmaskPermission;

    private String userIp;

    private String exportReason;

    private String exportFileName;

    private Long exportFileSize;

    private String region;                  // 향지 코드

    public CommonVO(){
        region = "CHN";
    }

    public String getRegion() {
        return region;
    }

    /**
     * @return the authRoleCrteYn
     */
    public String getAuthRoleCrteYn() {
        return authRoleCrteYn;
    }

    /**
     * @param authRoleCrteYn the authRoleCrteYn to set
     */
    public void setAuthRoleCrteYn(String authRoleCrteYn) {
        this.authRoleCrteYn = authRoleCrteYn;
    }

    /**
     * @return the authRoleUpdeYn
     */
    public String getAuthRoleUpdeYn() {
        return authRoleUpdeYn;
    }

    /**
     * @param authRoleUpdeYn the authRoleUpdeYn to set
     */
    public void setAuthRoleUpdeYn(String authRoleUpdeYn) {
        this.authRoleUpdeYn = authRoleUpdeYn;
    }

    /**
     * @return the authRoleDelYn
     */
    public String getAuthRoleDelYn() {
        return authRoleDelYn;
    }

    /**
     * @param authRoleDelYn the authRoleDelYn to set
     */
    public void setAuthRoleDelYn(String authRoleDelYn) {
        this.authRoleDelYn = authRoleDelYn;
    }

    /**
     * @return the authRoleIqYn
     */
    public String getAuthRoleIqYn() {
        return authRoleIqYn;
    }

    /**
     * @param authRoleIqYn the authRoleIqYn to set
     */
    public void setAuthRoleIqYn(String authRoleIqYn) {
        this.authRoleIqYn = authRoleIqYn;
    }

    /**
     * @return the dataRole
     */
    public String[] getDataRole() {
        return dataRole;
    }

    /**
     * @param dataRole the dataRole to set
     */
    public void setDataRole(String[] dataRole) {
        this.dataRole = dataRole;
    }

    /**
     * @return the regionRole
     */
    public String[] getRegionRole() {
        return regionRole;
    }

    /**
     * @param regionRole the regionRole to set
     */
    public void setRegionRole(String[] regionRole) {
        this.regionRole = regionRole;
    }

    /**
     * @return the platformRole
     */
    public String[] getPlatformRole() {
        return platformRole;
    }

    /**
     * @param platformRole the platformRole to set
     */
    public void setPlatformRole(String[] platformRole) {
        this.platformRole = platformRole;
    }

    /**
     * @return the languageCd
     */
    public String getLanguageCd() {
        return languageCd;
    }

    /**
     * @param languageCd the languageCd to set
     */
    public void setLanguageCd(String languageCd) {
        this.languageCd = languageCd;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int Offset) {
        offset = Offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int Limit) {
        limit = Limit;
    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int CurrPage) {
        //Integer page = new Integer(CurrPage);
        //currPage = page.intValue();
        currPage = CurrPage;
    }

    /**
     * @return the rgn
     */
    public String getRgn() {
        return rgn;
    }

    /**
     * @param rgn the rgn to set
     */
    public void setRgn(String rgn) {
        this.rgn = rgn;
    }

    /**
     * @return the rgstDt
     */
    public String getRgstDt() {
        return rgstDt;
    }

    /**
     * @param rgstDt the rgstDt to set
     */
    public void setRgstDt(String rgstDt) {
        this.rgstDt = rgstDt;
    }

    /**
     * @return the updr
     */
    public String getUpdr() {
        return updr;
    }

    /**
     * @param updr the updr to set
     */
    public void setUpdr(String updr) {
        this.updr = updr;
    }

    /**
     * @return the updeDt
     */
    public String getUpdeDt() {
        return updeDt;
    }

    /**
     * @param updeDt the updeDt to set
     */
    public void setUpdeDt(String updeDt) {
        this.updeDt = updeDt;
    }

    /**
     * @return the searchCondition
     */
    public String getSearchCondition() {
        return searchCondition;
    }

    /**
     * @return the searchKeyword
     */
    public String getSearchKeyword() {
        return searchKeyword;
    }

    /**
     * @param searchKeyword the searchKeyword to set
     */
    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    /**
     * @return the searchAesKeyword
     */
    public String getSearchAesKeyword() {
        return searchAesKeyword;
    }

    /**
     * @param searchAesKeyword the searchAesKeyword to set
     */
    public void setSearchAesKeyword(String searchAesKeyword) {
        this.searchAesKeyword = searchAesKeyword;
    }

    /**
     * @return the searchContidion
     */
    public String getSearchContidion() {
        return searchCondition;
    }

    /**
     * //@param searchContidion the searchContidion to set
     */
    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    /**
     * @return the authGrpCd
     */
    public String getAuthGrpCd() {
        return authGrpCd;
    }

    /**
     * @param authGrpCd the authGrpCd to set
     */
    public void setAuthGrpCd(String authGrpCd) {
        this.authGrpCd = authGrpCd;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the menuAdr
     */
    public String getMenuAdr() {
        return menuAdr;
    }

    /**
     * @param menuAdr the menuAdr to set
     */
    public void setMenuAdr(String menuAdr) {
        this.menuAdr = menuAdr;
    }

    /**
     * @return the authId
     */
    public int getAuthId() {
        return authId;
    }

    /**
     * @param authId the authId to set
     */
    public void setAuthId(int authId) {
        this.authId = authId;
    }

    /**
     * @return the selectPckgNm
     */
    public String getSelectPckgNm() {
        return selectPckgNm;
    }

    /**
     * @param selectPckgNm the selectPckgNm to set
     */
    public void setSelectPckgNm(String selectPckgNm) {
        this.selectPckgNm = selectPckgNm;
    }

    /**
     * @return the selectRegion
     */
    public String getSelectRegion() {
        return selectRegion;
    }

    /**
     * @param selectRegion the selectRegion to set
     */
    public void setSelectRegion(String selectRegion) {
        this.selectRegion = selectRegion;
    }

    /**
     * @return the selectPlatform
     */
    public String getSelectPlatform() {
        return SelectPlatform;
    }

    /**
     * @param selectPlatform the selectPlatform to set
     */
    public void setSelectPlatform(String selectPlatform) {
        SelectPlatform = selectPlatform;
    }

    /**
     * @return the dvViewYn
     */
    public String getDvViewYn() {
        return dvViewYn;
    }

    /**
     * @param dvViewYn the dvViewYn to set
     */
    public void setDvViewYn(String dvViewYn) {
        this.dvViewYn = dvViewYn;
    }

    /**
     * @return the maskYn
     */
    public String getMaskYn() {
        return maskYn;
    }

    /**
     * @param maskYn the maskYn to set
     */
    public void setMaskYn(String maskYn) {
        this.maskYn = maskYn;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUnmaskRequest() {
        return unmaskRequest;
    }

    public void setUnmaskRequest(String unmaskRequest) {
        this.unmaskRequest = unmaskRequest;
    }

    public String getUnmaskPermission() {
        return unmaskPermission;
    }

    public void setUnmaskPermission(String unmaskPermission) {
        this.unmaskPermission = unmaskPermission;
    }

    /**
     * @return the authRoleExportYn
     */
    public String getAuthRoleExportYn() {
        return authRoleExportYn;
    }

    /**
     * @param authRoleExportYn the authRoleExportYn to set
     */
    public void setAuthRoleExportYn(String authRoleExportYn) {
        this.authRoleExportYn = authRoleExportYn;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getExportReason() {
        return exportReason;
    }

    public void setExportReason(String exportReason) {
        this.exportReason = exportReason;
    }

    public String getExportFileName() {
        return exportFileName;
    }

    public void setExportFileName(String exportFileName) {
        this.exportFileName = exportFileName;
    }

    public Long getExportFileSize() {
        return exportFileSize;
    }

    public void setExportFileSize(Long exportFileSize) {
        this.exportFileSize = exportFileSize;
    }
}