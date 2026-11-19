package com.example.mongo.vo;

import org.springframework.data.annotation.Id;

import java.util.Map;

/**
 * <pre>
 * Statements
 * </pre>
 *
 * @ClassName   : EvntVersionUpdateStatusVO.java
 * @Description : 클래스 설명을 기술합니다.
 * @author Windows 占쏙옙占쏙옙占?
 * @since 2022. 3. 7.
 * @version 1.0
 * @see
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2022. 3. 7.     Windows 占쏙옙占쏙옙占?     	최초 생성
 * </pre>
 */

public class EvntVersionUpdateStatusVO extends CommonVO {
    // StatDeviceDownload Collection VO
    @Id
    private String _id;                     // 몽고 DB id
    private String transactionId;           // 통계 대상 트랜잭션 ID
    private String carModel;                // 차종 코드
    private String region;                  // 향지 코드
    private String vin;                     // 차량 식별 번호(VIN)
    private String type;                    // 요청 타입
    private String testDv;                  // 차량의 이벤트 적용 타입
    private String packagename;             // 패키지 기준 정보 명
    private String[] packagenameArr;
    private String sourceEventID;           // 소스 이벤트 ID           
    private String targetEventID;           // 타겟 이벤트 ID
    private String sessionid;               // 세션 ID
    private String requesttime;             // 요청 시간
    private String partNo;                  // 파트 넘버
    private String srresultCode;            // SR 결과 코드 (파트 별)
    private String srresultTime;            // SR 결과 시간 (파트 별)
    private String downloadresult;          // 다운로드 상태 (파트 별)
    private String isresultCode;            // 업데이트 결과 코드 (파트 별)
    private String isresultTime;            // 업데이트 결과 시간 (파트 별)
    private String updateresult;            // 설치 상태 (파트 별)
    private String reportCode;              // 별도보고 이벤트 수행 결과
    private String reportDetail;            // 별도보고 제어기 업데이트 수행 결과
    private String reportFgcondition;       // 별도보고 FG 업데이트 결과
    
    // StatUnitDownload Collection VO
    private String swUnitID;                // UnitID
    private String downloadStarttime;       // 다운로드 시작시간 (유닛 별)
    private String downloadEndtime;         // 다운로드 종료시간 (유닛 별)
    private String downloadresultCode;      // 다운로드 결과 (유닛 별)
    private String updateStarttime;         // 설치 시작시간 (유닛 별)
    private String updateEndtime;           // 설치 종료시간 (유닛 별)
    private String updateresultCode;        // 설치 결과 (유닛 별)
    private String downloadPartsectime;     // 다운로드 시간(초) (유닛 별)
    private String updatePartsectime;       // 설치 시간(초) (유닛 별)
    
    // web VO (검색옵션)
    // packagename  (있음)
    private String eventVer;                // evntVer (옵션 중 이벤트 버전)
    private String selectDownCd;            // selectDownCd (옵션 중 srresult)
    private String selectUpdateCd;          // selectUpdateCd (옵션 중 isresult)
    private String evntType;                // evntType (옵션 중 이벤트 타입)
    private String downloadSuccess;         // downloadSuccess (검색옵션 여부 미확인되어 현재 마스킹)
    private String updateSuccess;           // updateSuccess (검색옵션 여부 미확인되어 현재 마스킹)
    private String controllerUpdateStatus;  // controllerUpdateStatus (검색옵션 여부 미확인되어 현재 마스킹)
    private String vinList;                 // vinList (검색옵션 여부 미확인되어 현재 마스킹)
    private String flag;
    private String downloadOrUpdate;        // event fail Detail 검색 시 download실패검색인지  update실패검색인지
    private String failDetailTotal;
    private String[] downloadFailCodeArr;
    private String[] updateFailCodeArr;
    
    private String srResult;
    private String isResult;
    
    private String startDate;       //검색조건 startDate
    private String endDate;         //검색조건 endDate
    private String dateText1;       // 검색조건 startDate YYYYMMDD MI:SS
    private String dateText2;       // 검색조건 endDate YYYYMMDD MI:SS
    private String todayDate;       //금일 날짜
    
    private Map<String, Object> downloadFailDataMap;
    private Map<String, Object> updateFailDataMap;
    
    /**
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }
    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    /**
     * @return the endDate
     */
    public String getEndDate() {
        return endDate;
    }
    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    /**
     * @return the dateText1
     */
    public String getDateText1() {
        return dateText1;
    }
    /**
     * @param dateText1 the dateText1 to set
     */
    public void setDateText1(String dateText1) {
        this.dateText1 = dateText1;
    }
    /**
     * @return the dateText2
     */
    public String getDateText2() {
        return dateText2;
    }
    /**
     * @param dateText2 the dateText2 to set
     */
    public void setDateText2(String dateText2) {
        this.dateText2 = dateText2;
    }
    /**
     * @return the todayDate
     */
    public String getTodayDate() {
        return todayDate;
    }
    /**
     * @param todayDate the todayDate to set
     */
    public void setTodayDate(String todayDate) {
        this.todayDate = todayDate;
    }
    /**
     * @return the srResult
     */
    public String getSrResult() {
        return srResult;
    }
    /**
     * @param srResult the srResult to set
     */
    public void setSrResult(String srResult) {
        this.srResult = srResult;
    }
    /**
     * @return the isResult
     */
    public String getIsResult() {
        return isResult;
    }
    /**
     * @param isResult the isResult to set
     */
    public void setIsResult(String isResult) {
        this.isResult = isResult;
    }
    /**
     * @return the downloadSuccess
     */
    public String getDownloadSuccess() {
        return downloadSuccess;
    }
    /**
     * @param downloadSuccess the downloadSuccess to set
     */
    public void setDownloadSuccess(String downloadSuccess) {
        this.downloadSuccess = downloadSuccess;
    }
    /**
     * @return the updateSuccess
     */
    public String getUpdateSuccess() {
        return updateSuccess;
    }
    /**
     * @param updateSuccess the updateSuccess to set
     */
    public void setUpdateSuccess(String updateSuccess) {
        this.updateSuccess = updateSuccess;
    }
    /**
     * @return the controllerUpdateStatus
     */
    public String getControllerUpdateStatus() {
        return controllerUpdateStatus;
    }
    /**
     * @param controllerUpdateStatus the controllerUpdateStatus to set
     */
    public void setControllerUpdateStatus(String controllerUpdateStatus) {
        this.controllerUpdateStatus = controllerUpdateStatus;
    }
    /**
     * @return the vinList
     */
    public String getVinList() {
        return vinList;
    }
    /**
     * @param vinList the vinList to set
     */
    public void setVinList(String vinList) {
        this.vinList = vinList;
    }
    /**
     * @return the swUnitID
     */
    public String getSwUnitID() {
        return swUnitID;
    }
    /**
     * @return the eventVer
     */
    public String getEventVer() {
        return eventVer;
    }
    /**
     * @param eventVer the eventVer to set
     */
    public void setEventVer(String eventVer) {
        this.eventVer = eventVer;
    }
    /**
     * @return the selectDownCd
     */
    public String getSelectDownCd() {
        return selectDownCd;
    }
    /**
     * @param selectDownCd the selectDownCd to set
     */
    public void setSelectDownCd(String selectDownCd) {
        this.selectDownCd = selectDownCd;
    }
    /**
     * @return the selectUpdateCd
     */
    public String getSelectUpdateCd() {
        return selectUpdateCd;
    }
    /**
     * @param selectUpdateCd the selectUpdateCd to set
     */
    public void setSelectUpdateCd(String selectUpdateCd) {
        this.selectUpdateCd = selectUpdateCd;
    }
    /**
     * @return the evntType
     */
    public String getEvntType() {
        return evntType;
    }
    /**
     * @param evntType the evntType to set
     */
    public void setEvntType(String evntType) {
        this.evntType = evntType;
    }
    /**
     * @param swUnitID the swUnitID to set
     */
    public void setSwUnitID(String swUnitID) {
        this.swUnitID = swUnitID;
    }
    /**
     * @return the downloadStarttime
     */
    public String getDownloadStarttime() {
        return downloadStarttime;
    }
    /**
     * @param downloadStarttime the downloadStarttime to set
     */
    public void setDownloadStarttime(String downloadStarttime) {
        this.downloadStarttime = downloadStarttime;
    }
    /**
     * @return the downloadEndtime
     */
    public String getDownloadEndtime() {
        return downloadEndtime;
    }
    /**
     * @param downloadEndtime the downloadEndtime to set
     */
    public void setDownloadEndtime(String downloadEndtime) {
        this.downloadEndtime = downloadEndtime;
    }
    /**
     * @return the downloadresultCode
     */
    public String getDownloadresultCode() {
        return downloadresultCode;
    }
    /**
     * @param downloadresultCode the downloadresultCode to set
     */
    public void setDownloadresultCode(String downloadresultCode) {
        this.downloadresultCode = downloadresultCode;
    }
    /**
     * @return the updateStarttime
     */
    public String getUpdateStarttime() {
        return updateStarttime;
    }
    /**
     * @param updateStarttime the updateStarttime to set
     */
    public void setUpdateStarttime(String updateStarttime) {
        this.updateStarttime = updateStarttime;
    }
    /**
     * @return the updateEndtime
     */
    public String getUpdateEndtime() {
        return updateEndtime;
    }
    /**
     * @param updateEndtime the updateEndtime to set
     */
    public void setUpdateEndtime(String updateEndtime) {
        this.updateEndtime = updateEndtime;
    }
    /**
     * @return the updateresultCode
     */
    public String getUpdateresultCode() {
        return updateresultCode;
    }
    /**
     * @param updateresultCode the updateresultCode to set
     */
    public void setUpdateresultCode(String updateresultCode) {
        this.updateresultCode = updateresultCode;
    }
    /**
     * @return the downloadPartsectime
     */
    public String getDownloadPartsectime() {
        return downloadPartsectime;
    }
    /**
     * @param downloadPartsectime the downloadPartsectime to set
     */
    public void setDownloadPartsectime(String downloadPartsectime) {
        this.downloadPartsectime = downloadPartsectime;
    }
    /**
     * @return the updatePartsectime
     */
    public String getUpdatePartsectime() {
        return updatePartsectime;
    }
    /**
     * @param updatePartsectime the updatePartsectime to set
     */
    public void setUpdatePartsectime(String updatePartsectime) {
        this.updatePartsectime = updatePartsectime;
    }
    /**
     * @return the _id
     */
    public String get_id() {
        return _id;
    }
    /**
     * @param _id the _id to set
     */
    public void set_id(String _id) {
        this._id = _id;
    }
    /**
     * @return the transactionId
     */
    public String getTransactionId() {
        return transactionId;
    }
    /**
     * @param transactionId the transactionId to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    /**
     * @return the carModel
     */
    public String getCarModel() {
        return carModel;
    }
    /**
     * @param carModel the carModel to set
     */
    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }
    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }
    /**
     * @param region the region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }
    /**
     * @return the vin
     */
    public String getVin() {
        return vin;
    }
    /**
     * @param vin the vin to set
     */
    public void setVin(String vin) {
        this.vin = vin;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * @return the testDv
     */
    public String getTestDv() {
        return testDv;
    }
    /**
     * @param testDv the testDv to set
     */
    public void setTestDv(String testDv) {
        this.testDv = testDv;
    }
    /**
     * @return the packagename
     */
    public String getPackagename() {
        return packagename;
    }
    /**
     * @param packagename the packagename to set
     */
    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }
    /**
     * @return the sourceEventID
     */
    public String getSourceEventID() {
        return sourceEventID;
    }
    /**
     * @param sourceEventID the sourceEventID to set
     */
    public void setSourceEventID(String sourceEventID) {
        this.sourceEventID = sourceEventID;
    }
    /**
     * @return the targetEventID
     */
    public String getTargetEventID() {
        return targetEventID;
    }
    /**
     * @param targetEventID the targetEventID to set
     */
    public void setTargetEventID(String targetEventID) {
        this.targetEventID = targetEventID;
    }
    /**
     * @return the sessionid
     */
    public String getSessionid() {
        return sessionid;
    }
    /**
     * @param sessionid the sessionid to set
     */
    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
    /**
     * @return the requesttime
     */
    public String getRequesttime() {
        return requesttime;
    }
    /**
     * @param requesttime the requesttime to set
     */
    public void setRequesttime(String requesttime) {
        this.requesttime = requesttime;
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
     * @return the srresultCode
     */
    public String getSrresultCode() {
        return srresultCode;
    }
    /**
     * @param srresultCode the srresultCode to set
     */
    public void setSrresultCode(String srresultCode) {
        this.srresultCode = srresultCode;
    }
    /**
     * @return the srresultTime
     */
    public String getSrresultTime() {
        return srresultTime;
    }
    /**
     * @param srresultTime the srresultTime to set
     */
    public void setSrresultTime(String srresultTime) {
        this.srresultTime = srresultTime;
    }
    /**
     * @return the downloadresult
     */
    public String getDownloadresult() {
        return downloadresult;
    }
    /**
     * @param downloadresult the downloadresult to set
     */
    public void setDownloadresult(String downloadresult) {
        this.downloadresult = downloadresult;
    }
    /**
     * @return the isresultCode
     */
    public String getIsresultCode() {
        return isresultCode;
    }
    /**
     * @param isresultCode the isresultCode to set
     */
    public void setIsresultCode(String isresultCode) {
        this.isresultCode = isresultCode;
    }
    /**
     * @return the isresultTime
     */
    public String getIsresultTime() {
        return isresultTime;
    }
    /**
     * @param isresultTime the isresultTime to set
     */
    public void setIsresultTime(String isresultTime) {
        this.isresultTime = isresultTime;
    }
    /**
     * @return the updateresult
     */
    public String getUpdateresult() {
        return updateresult;
    }
    /**
     * @param updateresult the updateresult to set
     */
    public void setUpdateresult(String updateresult) {
        this.updateresult = updateresult;
    }
    /**
     * @return the reportCode
     */
    public String getReportCode() {
        return reportCode;
    }
    /**
     * @param reportCode the reportCode to set
     */
    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }
    /**
     * @return the reportDetail
     */
    public String getReportDetail() {
        return reportDetail;
    }
    /**
     * @param reportDetail the reportDetail to set
     */
    public void setReportDetail(String reportDetail) {
        this.reportDetail = reportDetail;
    }
    /**
     * @return the reportFgcondition
     */
    public String getReportFgcondition() {
        return reportFgcondition;
    }
    /**
     * @param reportFgcondition the reportFgcondition to set
     */
    public void setReportFgcondition(String reportFgcondition) {
        this.reportFgcondition = reportFgcondition;
    }
    /**
     * @return the flag
     */
    public String getFlag() {
        return flag;
    }
    /**
     * @param flag the flag to set
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }
    /**
     * @return the packagenameArr
     */
    public String[] getPackagenameArr() {
        return packagenameArr;
    }
    /**
     * @param packagenameArr the packagenameArr to set
     */
    public void setPackagenameArr(String[] packagenameArr) {
        this.packagenameArr = packagenameArr;
    }
    
    public String getDownloadOrUpdate() {
        return downloadOrUpdate;
    }
    /**
     * @param downloadOrUpdate the packagenameArr to set
     */
    public void setDownloadOrUpdate(String downloadOrUpdate) {
        this.downloadOrUpdate = downloadOrUpdate;
    }
    
    public String getFailDetailTotal() {
        return failDetailTotal;
    }
    /**
     * @param failDetailTotal the startDate to set
     */
    public void setFailDetailTotal(String failDetailTotal) {
        this.failDetailTotal = failDetailTotal;
    }
    /**
     * @return the downloadFailCodeArr
     */
    public String[] getDownloadFailCodeArr() {
        return downloadFailCodeArr;
    }
    /**
     * @param downloadFailCodeArr the downloadFailCodeArr to set
     */
    public void setDownloadFailCodeArr(String[] downloadFailCodeArr) {
        this.downloadFailCodeArr = downloadFailCodeArr;
    }
    /**
     * @return the updateFailCodeArr
     */
    public String[] getUpdateFailCodeArr() {
        return updateFailCodeArr;
    }
    /**
     * @param updateFailCodeArr the updateFailCodeArr to set
     */
    public void setUpdateFailCodeArr(String[] updateFailCodeArr) {
        this.updateFailCodeArr = updateFailCodeArr;
    }
    /**
     * @return the downloadFailDataMap
     */
    public Map<String, Object> getDownloadFailDataMap() {
        return downloadFailDataMap;
    }
    /**
     * @param downloadFailDataMap the downloadFailDataMap to set
     */
    public void setDownloadFailDataMap(Map<String, Object> downloadFailDataMap) {
        this.downloadFailDataMap = downloadFailDataMap;
    }
    /**
     * @return the updateFailDataMap
     */
    public Map<String, Object> getUpdateFailDataMap() {
        return updateFailDataMap;
    }
    /**
     * @param updateFailDataMap the updateFailDataMap to set
     */
    public void setUpdateFailDataMap(Map<String, Object> updateFailDataMap) {
        this.updateFailDataMap = updateFailDataMap;
    }

    
}
