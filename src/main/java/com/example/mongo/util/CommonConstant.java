package com.example.mongo.util;

import java.util.Arrays;

/**
 * <pre>
 * Statements
 * </pre>
 *
 * @author Jung Jae Heon
 * @version 1.0
 * @ClassName : CommonConstant.java
 * @Description : 공통코드 정의
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2017. 11. 14.     Jung Jae Heon     	최초 생성
 * </pre>
 * @see
 * @since 2017. 11. 14.
 */

public class CommonConstant {
    public static enum JsonAttribute {
        resultCd("resultCd"), errorCd("errorCd");
        private String retCode;

        JsonAttribute(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    public static enum MapAttribute {
        packageStatus("pckgSt");

        private String retCode;

        MapAttribute(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    public static enum UserRslt {
        reject("REJECT"),
        pass("PASS"),
        fail("FAIL");

        private String retCode;

        UserRslt(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }


    public static enum ResutCd {
        Success("S"), Fail("F"), Duplication("D"), AuthError("A");
        private String retCode;

        ResutCd(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    //Error 코드
    public static enum ErrorCd {
        E000("000"), E001("001"), E002("002"), E003("003"), E004("004"), E005("005"), E006("006"), E007("007"), E008("008"), E009("009"), E010("010"), E011("011"), E012("012"), E013("013"), E020("020"), E021("021"), E022("022"), E023("023"), E024("024"), E025("025"), E030("030"), E040("040"), E050("050"), E051("051"), E060("060"), E070("070"), E099("099");
        private String retCode;

        ErrorCd(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    //페키지 상태 코드
    public static enum PckgSt {
        P_ST10("10")//등록
        , P_ST20("20")//검증-개발사
        , P_ST30("30")//Pending-개발사
        , P_ST40("40")//배포(고객사에 배포)
        , P_ST50("50")//Reject-개발사
        , P_ST60("60")//검증-고객사
        , P_ST70("70")//Pending-고객사
        , P_ST80("80")//Reject-고객사
        , P_ST90("90")//승인대기
        , P_ST100("100")//배포승인
        , P_ST110("110")//배포만료
        , P_ST120("120")//Reject-등록자
        , P_ST130("130")//branch-reject-신규
        , P_ST140("140")//branch-reject-기존
        , P_ST150("150")//branch-reject 개발사 검증
        , P_ST160("160")//branch-reject 고객사 검증
        , P_ST170("170")//branch-reject 승인 대기
        , P_ST180("180")//
        , P_ST190("190")//branch-reject 개발사 reject - 상태 돌아감
        , P_ST200("200")//branch-reject 고객사 reject - 상태 돌아감
        , P_ST210("210")//service-reject 요청
        , P_ST220("220")//service-reject 개발사 검증
        , P_ST230("230")//service-reject 고객사 검증
        , P_ST240("240")//service-reject 승인 대기
        , P_ST250("250");//
        private String retCode;

        PckgSt(String retCode) {
            this.retCode = retCode;
        }

        public String getCode() {
            return retCode;
        }
    }

    public static enum UserSt {
        U_ST10("10")//정상
        , U_ST20("20")//승인대기
        , U_ST30("30")//승인거부
        , U_ST40("40")//승인완료
        , U_ST50("50")//정지
        , U_ST60("60")//정지해재
        , U_ST70("70")//재발급
        , U_ST80("80")//휴면계정
        , U_ST90("90");//탈퇴
        private String retCode;

        UserSt(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    public static enum PacakgeType {
        FUMO("FUMO"), SCOMO("SCOMO"), WHATSNEW("WHATSNEW"), MAP("MAP");
        private String retCode;

        PacakgeType(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    public static enum ApiResultCd {
        SUCCESS("0000"),
        DUPLICATE_REG("0010"),
        PARAMETER_MISS("9996"),
        AUTH_FAIL("9997"),
        DM_DB_FAIL("9998"),
        UNDEFINED_FAIL("9999"),
        EVENT_NOT_EXIST("4002"),
        WARRANTY_SYSTEM_LINKAGE_REQUIRED("4003");
        private String retCode;

        ApiResultCd(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    public static enum ApiResult {
        SUCCESS("SUCC"), FAIL("FAIL");
        private String retCode;

        ApiResult(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    public static enum RetryMessage {
        IF_SUMS_DM_CCU_001("DEIM", "이벤트 정보 관리 전문 DM 송신 실패"),
        IF_DM_SUMS_CCU_001("SERR", "이벤트 정보 등록 결과 SUMS 송신 실패"),
        IF_PT_DM_CCU_003("DESM", "이벤트 스케줄 정보 관리 전문 DM 송신 실패"),
        IF_PT_DM_CCU_004("DEEM", "이벤트 긴급 스케줄 정보 관리 전문 DM 송신 실패"),
        IF_PT_DM_CCU_0015("DRIM", "차량 Retry 정보 관리 전문 DM 송신 실패"),
        IF_PT_DM_CCU_005("DEPR", "이벤트 중지/재개 전문 DM 송신 실패"),
        IF_SUMS_DM_CCU_006("DEUU", "이벤트 미사용 전문 DM 송신 실패"),
        IF_PT_DM_CCU_0016("DDIM", "단말 정보 관리 전문 DM 송신 실패"),
        IF_PT_DM_CCU_0017("DSPR", "이벤트 스케줄 중지/재개"),
        IF_DM_SUMS_CCU_001_CDN_ULD_ERR("SERE", "이벤트 정보 등록 결과 CDN 업로드 실패"),
        IF_DM_PT_CCU_0002("CDND", "미사용 이벤트 CDN 파일 삭제 실패"),
        IF_DM_SUMS_CCU_001_INVAL_VAL_ERR("INVAL", "유효하지 않은 값으로 인한 이벤트 등록 실패"),
        IF_DM_SUMS_CCU_001_VAL_FAIL("VALFAIL", "기타 이벤트 validation 실패"),
        IF_PT_DM_CCU_0020("SERU", "이벤트 정규이벤트 등록 실패"),
        IF_ISSUE_SEND_ERR("ISER", "Hive Link 이벤트 전송 실패"),
        IF_CAMPAIGN_SEND_ERR("CPER", "GWMS/KGWMS 이벤트 전송 실패");
        private String mailCode;
        private String retMsg;

        RetryMessage(String mailCode, String retMsg) {
            this.mailCode = mailCode;
            this.retMsg = retMsg;
        }

        public String getMailCode() {
            return mailCode;
        }

        public String getMsg() {
            return retMsg;
        }
    }

    public static enum ScomoType {
        MAP("MAP"), POLICY("POLICY");
        private String retCode;

        ScomoType(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    public static enum IcuNodeNm {
        ICU_POLICY("ICU_POLICY"), ECU_POLICY("ECU_POLICY");

        private String retCode;

        IcuNodeNm(String retCode) {
            this.retCode = retCode;

        }

        public String getCode() {
            return retCode;
        }

    }

    //향지코드를 static enum 으로 관리 (버전1)
    public static enum RegionCodeEnum {
        KOR_REGION_CODE("KOR", new String[]{"TKR1", "TKR2", "TKR3", "TKR4", "KOR", "IKOR", "IGEN", "01", "02", "03", "08", "0A", "0F", "10", "11", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "IF", "XKOR"}), EUR_REGION_CODE("EUR", new String[]{"EUR", "IEUR", "04", "05", "0D", "0E", "12"})    //유럽
        , AUS_REGION_CODE("AUS", new String[]{"IAUS"})   //호주
        , USA_REGION_CODE("USA", new String[]{"TUS1", "TUS2", "TUS3", "TUS4", "IUSA"})   //미국
        , CAN_REGION_CODE("CAN", new String[]{"TCA1", "TCA2", "TCA3", "TCA4", "ICAN"})   //캐나다
        , CHN_REGION_CODE("CHN", new String[]{"TCN1", "TCN2", "TCN3", "TCN4", "ICHN"})   //중국
        , IDN_REGION_CODE("IDN", new String[]{"IIDN"})   //인도네시아
        , SGP_REGION_CODE("SGP", new String[]{"ISGP"})   //싱가폴
        , JPN_REGION_CODE("JPN", new String[]{"IJPN"})   //일본
        , IND_REGION_CODE("IND", new String[]{"IIND"})   //인도
        , NZL_REGION_CODE("NZL", new String[]{"INZL"})   //뉴질랜드
        , MES_REGION_CODE("MES", new String[]{"IMES", "TME0", "TME1", "TME2", "TME3", "TME4", "TME5", "TME6", "TME7", "TME8", "TME9"})   // 중동
        , PHL_REGION_CODE("PHL", new String[]{"IPHL", "TPH0", "TPH1", "TPH2", "TPH3", "TPH4", "TPH5", "TPH6", "TPH7", "TPH8", "TPH9"})   //필리핀
        , THA_REGION_CODE("THA", new String[]{"ITHA", "TTH0", "TTH1", "TTH2", "TTH3", "TTH4", "TTH5", "TTH6", "TTH7", "TTH8", "TTH9"})   //태국
        ;


        private String region;
        private String[] regionCode;

        RegionCodeEnum(String region, String[] regionCode) {
            this.region = region;
            this.regionCode = regionCode;
        }

        public String getRegion() {
            return region;
        }

        public String[] getRegionCode() {
            return regionCode;
        }

        public static String getRegionByRegionCode(String regionCode) {
            for (RegionCodeEnum regionCodeEnum : values()) {
                //regionCode배열 중 포함하는 구문이 있다 
                if (Arrays.stream(regionCodeEnum.getRegionCode()).anyMatch(regionCode::equals)) {
                    return regionCodeEnum.getRegion();
                }
            }

            //위 정의된 배열에 포함되지 않는 예외케이스 - E나 TEU로 시작 (유럽), T로 시작 (국내)
            if ("E".equals(regionCode.substring(0, 1)) || regionCode.startsWith("TEU")) {
                return EUR_REGION_CODE.getRegion();
            } else if (regionCode.startsWith("TJP")) {// 23.06.30 일본 테스트 향지 추가
                return JPN_REGION_CODE.getRegion();
            } else if (regionCode.startsWith("TAU")) {// 23.11.02 호주 테스트 향지 추가
                return AUS_REGION_CODE.getRegion();
                // T로 시작하고 바로 뒤에 숫자 3개가 연속된다면 한국
            } else if (regionCode.matches("^T\\d{3}.*")) {
                return KOR_REGION_CODE.getRegion();
            }

            //정의되지 않은 향지코드는 null return (AS-IS가 초기값 선언 X)
            return null;
        }

        public static String getRegionByPckgNm(String pckgNm) {
            String region = null;
            if (pckgNm != null && !pckgNm.isEmpty()) {
                String[] splitPckgNm = pckgNm.split("\\^");
                if (splitPckgNm.length >= 2) {
                    region = getRegionByRegionCode(splitPckgNm[1]);
                }
            }
            return region;
        }

    }

    //향지코드를 static enum 으로 관리 (버전2 - 원래 있던 분기 로직 분리)
    /*
      public static String getRegionByRegionCode2(String regionCode){
          if (regionCode.startsWith("TEU")) return "EUR";
          if ("T".equals(regionCode.substring(0, 1))) {
              switch(regionCode){
                  case "TUS1": case "TUS2": case "TUS3": case "TUS4": //북미 테스트 향지 코드 추가 - 221221
                      return "USA";
                  case "TCA1": case "TCA2": case "TCA3": case "TCA4": //캐나다 테스트 향지 코드 추가 - 221221
                      return "CAN";
                  //case "TEU1": case "TEU2": case "TEU3": case "TEU4": //유럽 테스트 향지 코드 추가 - 221221
                  //    return "EUR";
                  case "TAU1": case "TAU2": case "TAU3": case "TAU4": //호주 테스트 향지 코드 추가 - 221221
                      return "AUS";
                  case "TCN1": case "TCN2": case "TCN3": case "TCN4": //중국 테스트 향지 코드 추가 - 221221
                      return "CHN";
                  case "TKR1": case "TKR2": case "TKR3": case "TKR4": //국내 테스트 향지 코드 추가 - 230303
                      return "KOR";
                  default : //T + 3자리 숫자코드인 경우 국내
                      return "KOR";
              }   
          } else if ("E".equals(regionCode.substring(0, 1))) {
              return "EUR";
          } else {
              switch (regionCode) {
                  case "KOR":
                  case "IKOR":
                  case "IGEN":
                  case "01":
                  case "02":
                  case "03":
                  case "08":
                  case "0A":
                  case "0F":
                  case "10":
                  case "11":
                  case "13":
                  case "14":
                  case "15":
                  case "16":
                  case "17":
                  case "18":
                  case "19":
                  case "1A":
                  case "1B":
                  case "1C":
                  case "1D":
                  case "1E":
                  case "1F":
                      return "KOR";
                  case "EUR":
                  case "IEUR":
                  case "04":
                  case "05":
                  case "0D":
                  case "0E":
                  case "12":
                      return "EUR";
                  case "IAUS":
                      return "AUS";
                  case "ICAN":
                      return "CAN";
                  case "IIDN":
                      return "IDN";
                  case "ISGP":
                      return "SGP";
                  case "IUSA":
                      return "USA";
                  case "ICHN":
                      return "CHN";
                  case "IJPN":
                      return "JPN";
                  case "IIND":
                      return "IND";
              }
          }
          
          //정의되지 않은 향지 코드는 null을 return
          return null;
      }
     */

    public static enum AipErrMessage {
        FILE_NM_ERR(1, "Filename is null"),
        HOST_NM_ERR(2, "Hostname is null"),
        AIP_GW_IP_ERR(3, "AIP G/W IP is null"),
        FILE_ENC_STAT_ERR(4, "fileEncryptedStatus is null"),
        SET_LABEL_ERR(5, "setLabel is null"),
        EXCEL_EXP_ERR(6, "excelExport is null");

        private int errCode;
        private String errMsg;

        AipErrMessage(int errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }

        public int getErrCode() {
            return errCode;
        }

        public String getErrMsg() {
            return errMsg;
        }
    }

    public enum IssueCampaignStatus {
        BEFORE_LINK(0),//연동 전
        LINKING_EVNT_SEND_COMPLETE(1),//연동 중 (SW/이벤트 정보 보증 시스템으로 전송 완료)
        LINKING_ISSUE_RECEIVE_COMPLETE(2),//연동 중 (캠페인/이슈 번호 수신 완료)
        LINK_COMPLETE(3);//연동 완료

        private int code;

        IssueCampaignStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public enum CollectionType {
        STANDARD_TYPE("기존"),
        TIME_SERIES_TYPE("시계열"),
        BOTH_TYPE("기존/시계열");
        private String type;

        CollectionType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public enum YesNoEnum {
        Y("Y", "success"),
        N("N", "failed");

        private final String code;
        private final String desc;

        YesNoEnum(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}

