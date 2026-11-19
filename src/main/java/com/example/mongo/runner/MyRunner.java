package com.example.mongo.runner;

import cn.hutool.core.date.DatePattern;
import com.example.mongo.service.EvntVersionUpdateStatusService;
import com.example.mongo.service.TestService;
import com.example.mongo.util.*;
import com.example.mongo.vo.CcuVO;
import com.example.mongo.vo.DpoySuccFailStatVO;
import com.example.mongo.vo.EvntVersionUpdateStatusVO;
import com.example.mongo.vo.IF_DM_SUMS_CCU_001VO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import io.vavr.Tuple;
import io.vavr.Tuple7;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.util.HtmlUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.mongo.util.CommonConstant.CollectionType.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class MyRunner implements CommandLineRunner {
    private static final SimpleDateFormat STAT_DATE_FMT = new SimpleDateFormat("yyyyMMdd");

    private final JsonMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    private final EvntVersionUpdateStatusService evntVersionUpdateStatusService;
    private final TestService testService;

    public static Date convertToDate(LocalDateTime localDateTime) {
        // 将LocalDateTime转换为指定时区的Instant对象
        // ZoneId.systemDefault() 使用系统默认时区
        return Date.from(localDateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant());
    }

    public List<HMap> getDownloadFailList() {
        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>();

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");

        cal.setTime(date);
        String endDate = sdf.format(cal.getTime()) + "0000";

        cal.add(Calendar.HOUR, -1);
        String startDate = sdf.format(cal.getTime()) + "0000";

        // 오전 8시에 도는 배치는 전날 20시부터 집계한다.
        if (endDate.startsWith("08", 8)) {
            cal.add(Calendar.HOUR, -11);
            startDate = sdf.format(cal.getTime()) + "0000";
        }

        criteriaList.add(Criteria.where("requesttime").gte(startDate).lt(endDate));
        criteriaList.add(Criteria.where("packagename").regex(""));
        criteriaList.add(Criteria.where("targetEventID").regex(""));
        criteriaList.add(Criteria.where("targetEventName").regex(""));
        criteriaList.add(Criteria.where("testDv").is("P"));
        criteriaList.add(Criteria.where("downloadresult").regex(""));
        criteriaList.add(Criteria.where("partNo").ne("MAINCONFIG"));

        criteria.andOperator(criteriaList);

        AggregationOperation match = Aggregation.match(criteria);
        AggregationOperation sort = Aggregation.sort(Sort.Direction.DESC, "srresultTime");
        AggregationOperation group = Aggregation.group("deviceId", "partNo").first("$$ROOT").as("doc");
        AggregationOperation replace = Aggregation.replaceRoot("doc");
        AggregationOperation group2 = Aggregation.group("packagename", "srresultCode").count().as("count");
        ProjectionOperation project = Aggregation.project(Fields.from(
                        Fields.field("packagename", "$_id.packagename"),
                        Fields.field("srresultCode", "$_id.srresultCode"),
                        Fields.field("count", "$count")))
                .andExclude("_id");
        Aggregation agg = Aggregation.newAggregation(match, sort, group, replace, group2, project)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

        AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);
        log.debug("DownloadFailCheckQuartz======================================");
        log.debug("agg NOSQL ::: " + agg);
        log.debug("DownloadFailCheckQuartz====================================== ");

        return aggregate.getMappedResults();
    }

    private Criteria getDmRequestCriteria(LocalDate localDate) {
        Pair<Date, Date> timePair = TimeUtil.getDatePair(localDate);
        return Criteria.where("mCreatedAt").gte(timePair.getLeft()).lt(timePair.getRight());
    }

    public List<HMap> selectDayDmRequestMaxConnCnt(LocalDate localDate) throws Exception {
        AggregationOperation match = Aggregation.match(getDmRequestCriteria(localDate));
        AggregationOperation project = Aggregation.project("mCreateTime")
                .andExpression("substr(mCreateTime, 8, 4)").as("createtimeCut");
        AggregationOperation group = Aggregation.group("createtimeCut").count().as("count");

        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "_id");
        Aggregation agg = Aggregation.newAggregation(match, project, group, sort)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

        AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "SessionAbroadTS", HMap.class);

        return aggregate.getMappedResults();
    }

    public List<HMap> selectDayServerDmRequestMaxAndAvgConnCnt(LocalDate localDate) throws Exception {
        AggregationOperation match = Aggregation.match(getDmRequestCriteria(localDate));
        AggregationOperation group = Aggregation.group("mCreateTime", "mJobServer").count().as("count");
        Aggregation agg = Aggregation.newAggregation(match, group)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

        AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "SessionAbroadTS", HMap.class);

        return aggregate.getMappedResults();
    }

    public List<HMap> selectDayServerDmRequestTotConnCnt(LocalDate localDate) throws Exception {
        AggregationOperation match = Aggregation.match(getDmRequestCriteria(localDate));
        AggregationOperation group = Aggregation.group("mJobServer").count().as("count");

        Aggregation agg = Aggregation.newAggregation(match, group)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

        AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "SessionAbroadTS", HMap.class);

        return aggregate.getMappedResults();
    }

    public void saveDmRequestStatus(LocalDate execTime, String maxRequestTime, int maxRequestCount, int totalSessionCount,
                                    List<Map<String, Object>> rpsList) {
        Map<String, Object> dmRequestStatus = new HashMap<>();

        LocalDate yesterday = execTime.minusDays(1L);
        dmRequestStatus.put("date", DatePattern.PURE_DATE_FORMATTER.format(yesterday));
        dmRequestStatus.put("rpsList", rpsList);
        dmRequestStatus.put("maxRequestTime", maxRequestTime);
        dmRequestStatus.put("maxRequestCount", String.valueOf(maxRequestCount));
        dmRequestStatus.put("totalSessionCount", String.valueOf(totalSessionCount));

        mongoTemplate.insert(dmRequestStatus, "DmRequestStatus");
    }

    @Override
    public void run(String... args) throws Exception {

        Date nowDate = new Date();
        LocalDate today = LocalDate.now();
        String succYn = "S";
        String server = "";


        String maxRequestTime = "";
        int maxRequestCount = 0;
        List<HMap> timeConnCntList = selectDayDmRequestMaxConnCnt(today);
        if (CollectionUtils.isNotEmpty(timeConnCntList)) {
            for (HMap tempTimeConnCnt : timeConnCntList) {
                if (tempTimeConnCnt.containsKey("Id")) {
                    String nowTime = tempTimeConnCnt.getString("Id");
                    if (tempTimeConnCnt.containsKey("count")) {
                        int nowConnCnt = (int) tempTimeConnCnt.get("count");

                        if (nowConnCnt >= maxRequestCount) {
                            if (maxRequestTime.isEmpty()) {
                                maxRequestTime = nowTime;
                                maxRequestCount = nowConnCnt;
                            } else {
                                if (Integer.parseInt(nowTime) > Integer.parseInt(maxRequestTime)) {
                                    maxRequestTime = nowTime;
                                    maxRequestCount = nowConnCnt;
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, String> svrRpsMaxTimeMap = new HashMap<>();
        Map<String, Integer> svrRpsMaxValueMap = new HashMap<>();
        Map<String, List<Integer>> svrRpsValueListMap = new HashMap<>();
        List<HMap> timeSvrTimeConnCntList = selectDayServerDmRequestMaxAndAvgConnCnt(today);
        if (CollectionUtils.isNotEmpty(timeSvrTimeConnCntList)) {
            for (HMap tempSvrTimeConnCnt : timeSvrTimeConnCntList) {
                String nowTime = "";
                String nowSvr = "";
                int nowConnCnt = 0;

                Object id = tempSvrTimeConnCnt.get("Id");
                if (id instanceof LinkedHashMap<?, ?>) {
                    LinkedHashMap<String, String> idMap = (LinkedHashMap<String, String>) id;
                    if (idMap.containsKey("mCreateTime")) {
                        nowTime = idMap.get("mCreateTime");
                        nowTime = nowTime.substring(8, 14);
                    }

                    if (idMap.containsKey("mJobServer")) {
                        nowSvr = idMap.get("mJobServer");
                        if (nowSvr.indexOf("/") > 0) {
                            nowSvr = nowSvr.substring(0, nowSvr.indexOf("/"));
                        } else if (nowSvr.indexOf("\\") > 0) {
                            nowSvr = nowSvr.substring(0, nowSvr.indexOf(":"));
                        }
                    }
                }

                if (tempSvrTimeConnCnt.containsKey("count")) {
                    nowConnCnt = (int) tempSvrTimeConnCnt.get("count");
                }

                if (!svrRpsMaxValueMap.containsKey(nowSvr)) {
                    svrRpsMaxTimeMap.put(nowSvr, nowTime);
                    svrRpsMaxValueMap.put(nowSvr, nowConnCnt);
                } else {
                    int tempMaxCnt = svrRpsMaxValueMap.get(nowSvr);
                    if (nowConnCnt >= tempMaxCnt) {
                        String tempMaxTime = svrRpsMaxTimeMap.get(nowSvr);
                        if (Integer.parseInt(nowTime) > Integer.parseInt(tempMaxTime)) {
                            svrRpsMaxTimeMap.remove(nowSvr);
                            svrRpsMaxTimeMap.put(nowSvr, nowTime);
                            svrRpsMaxValueMap.remove(nowSvr);
                            svrRpsMaxValueMap.put(nowSvr, nowConnCnt);
                        }
                    }
                }

                if (!svrRpsValueListMap.containsKey(nowSvr)) {
                    List<Integer> tempValueList = new ArrayList<>();
                    tempValueList.add(nowConnCnt);
                    svrRpsValueListMap.put(nowSvr, tempValueList);
                } else {
                    List<Integer> tempValueList = svrRpsValueListMap.get(nowSvr);
                    tempValueList.add(nowConnCnt);
                    svrRpsValueListMap.remove(nowSvr);
                    svrRpsValueListMap.put(nowSvr, tempValueList);
                }
            }
        }

        Map<String, Integer> svrRpsTotValueMap = new HashMap<>();
        int svrAllTotValue = 0;
        List<HMap> svrDayConnCntList = selectDayServerDmRequestTotConnCnt(today);
        if (CollectionUtils.isNotEmpty(svrDayConnCntList)) {
            for (HMap tempSvrConnCnt : svrDayConnCntList) {
                String nowSvr = "";
                int dayConnCnt = 0;

                if (tempSvrConnCnt.containsKey("Id")) {
                    nowSvr = tempSvrConnCnt.getString("Id");
                    if (nowSvr.indexOf("/") > 0) {
                        nowSvr = nowSvr.substring(0, nowSvr.indexOf("/"));
                    } else if (nowSvr.indexOf("\\") > 0) {
                        nowSvr = nowSvr.substring(0, nowSvr.indexOf(":"));
                    }
                }

                if (tempSvrConnCnt.containsKey("count")) {
                    dayConnCnt = (int) tempSvrConnCnt.get("count");
                }

                if (!svrRpsTotValueMap.containsKey(nowSvr)) {
                    svrRpsTotValueMap.put(nowSvr, dayConnCnt);
                }

                if (svrAllTotValue == 0) {
                    svrAllTotValue = dayConnCnt;
                } else {
                    svrAllTotValue = svrAllTotValue + dayConnCnt;
                }
            }
        }

        Map<String, Map<String, Object>> rpsMap = new HashMap<>();
        if (!svrRpsMaxTimeMap.isEmpty()) {
            for (String key : svrRpsMaxTimeMap.keySet()) {
                if (!rpsMap.containsKey(key)) {
                    Map<String, Object> tempRps = new HashMap<>();

                    tempRps.put("hostName", key);
                    tempRps.put("rpsMaxTime", svrRpsMaxTimeMap.get(key));
                    tempRps.put("rpsMaxValue", String.valueOf(svrRpsMaxValueMap.get(key)));
                    tempRps.put("sessionCount", String.valueOf(svrRpsTotValueMap.get(key)));

                    rpsMap.put(key, tempRps);
                }
            }
        }

        if (!svrRpsValueListMap.isEmpty() && !rpsMap.isEmpty()) {
            for (String key : svrRpsValueListMap.keySet()) {
                List<Integer> tempValueList = svrRpsValueListMap.get(key);

                int totalConnCnt = 0;
                for (int nowConnCnt : tempValueList) {
                    totalConnCnt += nowConnCnt;
                }

                if (rpsMap.containsKey(key)) {
                    Map<String, Object> tempRps = rpsMap.get(key);
                    tempRps.put("rpsAverageValue", String.format("%.4f", ((double) totalConnCnt / (double) tempValueList.size())));
                    rpsMap.remove(key);
                    rpsMap.put(key, tempRps);
                }
            }

            List<String> keySet = new ArrayList<>(rpsMap.keySet());

            Collections.sort(keySet);

            List<Map<String, Object>> rpsList = new ArrayList<>();

            for (String key : keySet) {
                rpsList.add(rpsMap.get(key));
            }

            // MongoDB DmRequestStatus 컬렉션에 배치 정보 저장
            saveDmRequestStatus(today, maxRequestTime, maxRequestCount, svrAllTotValue, rpsList);
        }

//        List<HMap> downloadFailList = getDownloadFailList();
//        log.info("downloadFailList: {}", downloadFailList);
//        downloadFailList.forEach(hMap -> log.info("result: {}", Objects.nonNull(hMap.get("srresultCode"))));


//        String s = Files.readString(Path.of("D:\\MongoDB\\cdnUrl2026082001.json"));
//        IF_DM_SUMS_CCU_001VO ifDmSumsCcu001VO = JsonUtil.jsonToObject(s, IF_DM_SUMS_CCU_001VO.class);
//        ArrayList<CcuVO> cdnUrlList = ifDmSumsCcu001VO.getCdnUrlList();
//        Map<String, Optional<CcuVO>> collect = cdnUrlList.stream()
//                .collect(Collectors.groupingBy(getGroupFunc(),
//                        Collectors.maxBy(Comparator.comparingLong(ccuVO -> Long.parseLong(ccuVO.getScomoFilId())))));
//        List<CcuVO> voList = collect.values().stream()
//                .map(op -> op.orElse(null)).filter(Objects::nonNull).toList();
//        boolean y = voList.stream().anyMatch(v -> v.getCdnSuccYn().equals("N"));
//        log.info("s: {}", y);

//        Map<String, CcuVO> ccuMap = new HashMap<>();
//        for (CcuVO ccuVO : ifDmSumsCcu001VO.getCdnUrlList()) {
//            ccuMap.put(ccuVO.getScomoNodeNm() + "_" + ccuVO.getSrcVer() + "_" + ccuVO.getTgtVer(), ccuVO);
//        }
//        boolean n = ccuMap.values().stream().allMatch(v -> v.getCdnSuccYn().equals("Y"));
//        log.info("n: {}, size: {}", n, ccuMap.values().size());


//        Map<String, String> dataMap = Stream.of(new Object[][]{
//                {"issueNo", "1"},
//                {"srcVer", "2"},
//                {"verId", "3"},
//                {"dvId", "4"},
//                {"brand", "5"},
//                {"message", "6"},
//        }).collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));
//        log.info("dataMap: {}", dataMap);
//
//        Map<String, String> stringStringMap = Map.of("issueNo", "1",
//                "srcVer", "2",
//                "verId", "3",
//                "dvId", "4",
//                "brand", "5",
//                "message", "6");
//        log.info("stringStringMap: {}", stringStringMap);

//        Date nowDate = new Date();
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(nowDate);
//        cal.add(Calendar.DATE, -1);
//        String format = STAT_DATE_FMT.format(cal.getTime());
//        log.info("format: {}", format);
//        log.info("yesterday: {}", TimeUtil.getYesterdayDateStr());

//        String auth = Base64.encode("otafumostg" + ":" + "Gwmsfumo1!");
//        log.info("e:{}", "Basic b3RhZnVtb3N0ZzpHd21zZnVtbzEh".equals("Basic " + auth));

//        query11();

//        query12();


//        LocalDateTime localDateTime = LocalDateTimeUtil.parse("2026-07-03 09:31:00.908", DatePattern.NORM_DATETIME_MS_FORMATTER);
//        Date date = convertToDate(localDateTime);
//        log.info("localDateTime: {}", date);
//        String dateFormat = DateUtil.format(date, DatePattern.NORM_DATETIME_MS_FORMATTER);
//        log.info("dateFormat: {}", dateFormat);
//        HMap hMap = mongoTemplate.findOne(
//                Query.query(Criteria.where("createdAt").is(date)
//                        .and("vin").is("KMTHB81B6RU241662")
//                        .and("carModel").is("JX1")
//                        .and("sourceEventID").is("P0358PCE07ICHN10_016")
//                        .and("targetEventID").is("P0358PCE07ICHN10_017")),
//                HMap.class, "StatDeviceDownload");
//        log.info("h: {}", hMap);

//        LocalDateTime localDateTime2 = LocalDateTimeUtil.parse("2026-07-04 18:53:04.518", DatePattern.NORM_DATETIME_MS_FORMATTER);
//        Date date2 = convertToDate(localDateTime2);
//        HMap hMap = mongoTemplate.findOne(
//                Query.query(Criteria.where("createdAt").is(date2)
//                        .and("vin").is("KMTGA41C3TU314297")
//                        .and("carModel").is("RG3")
//                        .and("sourceEventID").is("P0354PCE07ICHN10_008")
//                        .and("targetEventID").is("P0354PCE07ICHN10_010")),
//                HMap.class, "StatDeviceDownload");
//        log.info("h: {}", hMap);

//        queryInterface();


//        jsonObject();


//        queryLogInterface();

//        getCdnUrl();


//        String verId = "P0334PCE07ICHN10_020";
//        String verId = "P1516PCE07ICHN10_001";
//        long count = getUploadLogCount(verId);
//        log.info("count: {}", count);

//        callFileApi();
//        fileApi();

//        Query query = Query.query(criteria);
//        query.fields().include("deviceId", "mVin", "packagename", "carModel", "brand", "recentEventVersionID",
//                "recentEventVersionNm", "lastUpdateEventVersionID", "lastConnectionDate").exclude("_id");
//        query.skip(10);
//        query.limit(10);
//        List<HMap> deviceList = mongoTemplate.find(query, HMap.class, "Device");
//        List<Map<String, String>> result = new ArrayList<>();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
//        if (CollectionUtils.isNotEmpty(deviceList)) {
//            for (HMap hMap : deviceList) {
//                Map<String, String> map = new HashMap<>();
//                Set<String> keySet = hMap.keySet();
//                for (String key : keySet) {
//                    if("lastConnectionDate".equals(key)){
//                        Date lastConnectionDate = (Date) hMap.get(key);
//                        String dateStr = DateUtil.format(lastConnectionDate, simpleDateFormat);
//                        map.put(key, dateStr);
//                    } else {
//                        map.put(key, hMap.get(key).toString());
//                    }
//                }
//                result.add(map);
//            }
//        }
//        result.forEach(d -> log.info("d: {}", d));

//        long count2 = mongoTemplate.count(Query.query(Criteria.where("packagename").is("NE1KN^ICHN^01").and("testDv").is("P")), "Device");
//        log.info("count2: {}", count2);


            //q();


//        selectDayInstallStatusCntNew();


//        eventUpdateStat();
//        log.info("result: {}", result.size());
//        List<HMap> mapList = evntVersionUpdateStatusService.selectEvntDownloadSuccessDetailList(vo);
//        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
//        for (HMap temp : mapList) {
//            if (temp.containsKey("downloadData")) {
//                List<Map<String, Object>> downloadSuccessData = (List<Map<String, Object>>) temp.get("downloadData");
//
//                for (Map<String, Object> temp1 : downloadSuccessData) {
//                    if (temp1.containsKey("matchdata")) {
//                        Map<String, String> matchdata = (Map<String, String>) temp1.get("matchdata");
//                        result.add(matchdata);
//                    }
//                }
//            }
//        }
//        log.info("result: {}", result.size());


//        deployStatistics2();


//        versionStat();


//        List<HMap> dataList = versionStats();
//        log.info("dataList: {}", dataList);
//        for (HMap row : dataList) {
//            if (row.get("Id") instanceof LinkedHashMap) {
//                LinkedHashMap<String, String> idMap = (LinkedHashMap<String, String>) row.get("Id");
//                log.info("Id: {}", idMap.get("packagename"));
//                log.info("region: {}", idMap.get("region"));
//            }
//        }

//        selectDeviceList();


//        query();

//        deployStatistics();

//        MongoExpression mongoExpression = MongoExpression.create(jsonStr);
//        AggregationExpression aggregationExpression = AggregationExpression.from(mongoExpression);


//        String s1 =
//        "789CBD995173A2301485DFFD151DDF6B2080D80EDAB1A675D92DB4A3E8EC3E7518482D231216D02DFDF59B002AE374FAD07AF3E42124E7724FF2F912EBE66D135FEC6896472C1976D59ED2BDA049C0C228590DBB0BEFFE72D0BDC80B3F09FD982574D82D69DEBD1975AC799904CE43237E8419574B9A118F8CD41EB650A3ABC1A78C156C441CB47F530FF0A53417656DBE46510C0B1D9F3B9693AFF8AF66A15A742CCFCF56B4E0E281058B993D5ADAEEF5C34FD2BF1DABA43F57AEAE064A7FF03C5EBA9E859A291D0B1D56CDD9360BE871F96B51A4D708A559D85B6797ACF07BAFE5967719F502B6B91E28AA82C20DDAF02FF257F4867F573874BC003BEF77FF1EC91FC37D5F6367A9BF3964ACFE8E5DE229EA53BBEEA1DC8CE62958BDBDB92878D806A16E59580A59F8C536E762B20945CA16AA4515EF8CBE34F90A55CD1142A9261D86468D71357AD806F1FAB30D38CEDA272F34480647FB8E45FCC21F6185B7502911CB3E8169750AEAF6F13107BBA09B0F0E570FCD270EB2931D4D0A969588D03466250DD155DFE82BC419A8CAB38A6CF2E151438DE9B7BC9FD6FCD8F3AEE02A2C6BE8E10A88EC29ACFD3687F3BF7B2BC42E78650AD8842832A301E3FFC0E58CFEDD461970B1451AF25D01DFFB2A3C3F2B5C06550303E287C1F1C3D0F86158FC30307E58067E58267E581E7E58027E1A207E1A387E1A347E1A2C7E1A307E9A0CFC3499F869F2F0D324E0A703E2A783E3A743E3A7C3E2A703E3A7CBC04F97899F2E0F3F5D027E06207E06387E06347E062C7E06307E860CFC0C99F819F2F03324E06702E26782E36742E367C2E26702E367CAC0CF94899F290F3F130A3F676CBB9347F7DE9E9E1BBE9633087A2D7F88F05BF6E7C7EEC4FCCCD0B5DCA1903B290109DC492938DC4E63FB066C84EEECE485353EC1DA5F51D7DF7C3197B6597D2D7227BEFDABB8B6EDEAB767B3BB8F123F9E463BBA48CF91DBB7F227B4F0A3B872FAF1CB99A0711C4F33B64DEB6679914F9DD1B47AAC1A128F07D1BA8643CD65E97F585A3BE7";
//        String s2 =
//        "789CBD995173A2301485DFFD151DDF6B2080D80EDAB1A675D92DB4A3E8EC3E7518482D231216D02DFDF59B002AE374FAD07AF3E42124E7724FF2F912EBE66D135FEC6896472C1976D59ED2BDA049C0C228590DBB0BEFFE72D0BDC80B3F09FD982574D82D69DEBD1975AC799904CE43237E8419574B9A118F8CD41EB650A3ABC1A78C156C441CB47F530FF0A53417656DBE46510C0B1D9F3B9693AFF8AF66A15A742CCFCF56B4E0E281058B993D5ADAEEF5C34FD2BF1DABA43F57AEAE064A7FF03C5EBA9E859A291D0B1D56CDD9360BE871F96B51A4D708A559D85B6797ACF07BAFE5967719F502B6B91E28AA82C20DDAF02FF257F4867F573874BC003BEF77FF1EC91FC37D5F6367A9BF3964ACFE8E5DE229EA53BBEEA1DC8CE62958BDBDB92878D806A16E59580A59F8C536E762B20945CA16AA4515EF8CBE34F90A55CD1142A9261D86468D71357AD806F1FAB30D38CEDA272F34480647FB8E45FCC21F6185B7502911CB3E8169750AEAF6F13107BBA09B0F0E570FCD270EB2931D4D0A969588D03466250DD155DFE82BC419A8CAB38A6CF2E151438DE9B7BC9FD6FCD8F3AEE02A2C6BE8E10A88EC29ACFD3687F3BF7B2BC42E78650AD8842832A301E3FFC0E58CFEDD461970B1451AF25D01DFFB2A3C3F2B5C06550303E287C1F1C3D0F86158FC30307E58067E58267E581E7E58027E1A207E1A387E1A347E1A2C7E1A307E9A0CFC3499F869F2F0D324E0A703E2A783E3A743E3A7C3E2A703E3A7CBC04F97899F2E0F3F5D027E06207E06387E06347E062C7E06307E860CFC0C99F819F2F03324E06702E26782E36742E367C2E26702E367CAC0CF94899F290F3F130A3F676CBB9347F7DE9E9E1BBE9633087A2D7F88F05BF6E7C7EEC4FCCCD0B5DCA1903B290109DC492938DC4E63FB066C84EEECE485353EC1DA5F51D7DF7C3197B6597D2D7227BEFDABB8B6EDEAB767B3BB8F123F9E463BBA48CF91DBB7F227B4F0A3B872FAF1CB99A0711C4F33B64DEB6679914F9DD1B47AAC1A128F07D1BA8643CD65E97F585A3BE7";
//        log.info("equals: {}", s1.equals(s2));

//        CompressStringUtil compressStringUtil = new CompressStringUtil();
//        String decompressString = compressStringUtil.decompressString
//        ("789CBD995173A2301485DFFD151DDF6B2080D80EDAB1A675D92DB4A3E8EC3E7518482D231216D02DFDF59B002AE374FAD07AF3E42124E7724FF2F912EBE66D135FEC6896472C1976D59ED2BDA049C0C228590DBB0BEFFE72D0BDC80B3F09FD982574D82D69DEBD1975AC799904CE43237E8419574B9A118F8CD41EB650A3ABC1A78C156C441CB47F530FF0A53417656DBE46510C0B1D9F3B9693AFF8AF66A15A742CCFCF56B4E0E281058B993D5ADAEEF5C34FD2BF1DABA43F57AEAE064A7FF03C5EBA9E859A291D0B1D56CDD9360BE871F96B51A4D708A559D85B6797ACF07BAFE5967719F502B6B91E28AA82C20DDAF02FF257F4867F573874BC003BEF77FF1EC91FC37D5F6367A9BF3964ACFE8E5DE229EA53BBEEA1DC8CE62958BDBDB92878D806A16E59580A59F8C536E762B20945CA16AA4515EF8CBE34F90A55CD1142A9261D86468D71357AD806F1FAB30D38CEDA272F34480647FB8E45FCC21F6185B7502911CB3E8169750AEAF6F13107BBA09B0F0E570FCD270EB2931D4D0A969588D03466250DD155DFE82BC419A8CAB38A6CF2E151438DE9B7BC9FD6FCD8F3AEE02A2C6BE8E10A88EC29ACFD3687F3BF7B2BC42E78650AD8842832A301E3FFC0E58CFEDD461970B1451AF25D01DFFB2A3C3F2B5C06550303E287C1F1C3D0F86158FC30307E58067E58267E581E7E58027E1A207E1A387E1A347E1A2C7E1A307E9A0CFC3499F869F2F0D324E0A703E2A783E3A743E3A7C3E2A703E3A7CBC04F97899F2E0F3F5D027E06207E06387E06347E062C7E06307E860CFC0C99F819F2F03324E06702E26782E36742E367C2E26702E367CAC0CF94899F290F3F130A3F676CBB9347F7DE9E9E1BBE9633087A2D7F88F05BF6E7C7EEC4FCCCD0B5DCA1903B290109DC492938DC4E63FB066C84EEECE485353EC1DA5F51D7DF7C3197B6597D2D7227BEFDABB8B6EDEAB767B3BB8F123F9E463BBA48CF91DBB7F227B4F0A3B872FAF1CB99A0711C4F33B64DEB6679914F9DD1B47AAC1A128F07D1BA8643CD65E97F585A3BE7");
//        log.info("msg: \n{}", decompressString);
//        Aggregation aggregation = Aggregation.newAggregation(
//                Aggregation.group("vin"),
//                Aggregation.count().as("cnt")
//        );

//        Aggregation aggregation2 = Aggregation.newAggregation(
//                Aggregation.group("vin"),
//                Aggregation.lookup("StatDeviceDownload", "_id", "vin", "downloadInfo"),
//                Aggregation.unwind("downloadInfo"),
//                Aggregation.group("downloadInfo.brand").count().as("cnt")
//        );

//        Aggregation aggregation2 = Aggregation.newAggregation(
//                Aggregation.group("vin"),
//                Aggregation.lookup("StatDeviceDownload", "_id", "vin", "downloadInfo"),
//                Aggregation.unwind("downloadInfo"),
//                Aggregation.group("downloadInfo.vin").first("downloadInfo.brand").as("brand"),
//                Aggregation.group("brand").first("brand").as("brand").count().as("cnt"),
//                Aggregation.project().andExclude("_id")
//        );

//        Aggregation aggregation = Aggregation.newAggregation(
//                Aggregation.group("vin")    // 根据 'age' 字段去重
//                        .first("$$ROOT").as("document"), // 保留该分组下的第一个完整文档
//                Aggregation.replaceRoot("document"), // 将文档提升为根
//                Aggregation.sort(Sort.Direction.ASC, "num")
//        );


//        AggregationResults<HMap> aggregationResults = mongoTemplate.aggregate(aggregation, "TransactionCHN", HMap
//        .class);
//        aggregationResults.getMappedResults().forEach(r -> log.info("result: {}", r));

//        AggregationResults<HMap> aggregationResults2 = mongoTemplate.aggregate(aggregation2, "TransactionCHN", HMap
//        .class);
//        aggregationResults2.getMappedResults().forEach(r -> log.info("result2: {}", r));
//        log.info("size: {}", aggregationResults.getMappedResults().size());


//        mongoTemplate.findDistinct(new Query(), "vin", )


//        HMap result = mongoTemplate.findOne(Query.query(Criteria.where("_id")
//                .is("692ef1d6db28ad7f2c463420")), HMap.class, "Interface");
//        Files.writeString(Path.of("C:\\Users\\Admin\\Downloads\\MongoData",
//                        "scomo_ccu_P0334PCE07ICHN10_024_20251211.json"),
//                result.get("ifbody").toString());
//        HMap result = mongoTemplate.findOne(Query.query(Criteria.where("_id")
//                        .is("692f1932b6a9fb4c78181c6e")), HMap.class, "Interface");
//        Files.writeString(Path.of("C:\\Users\\Admin\\Downloads\\MongoData", "ifbody_20251203005201.json"),
//                result.get("ifbody").toString());


//        String filePath = "https://cdn-avnt-hyundai.hmgmobility.com.cn/avnt_scomo_prd/NE1KN/ICHN/C/SCOMO/700203/89b5643e203348fd9e65abb82d4a1162.enc";
//        Path path = Paths.get(filePath);
//        String name = StringUtils.getFilename(filePath);
//        log.info(" name: {}",name);
//        Query query = new Query();
//        Criteria criteriaOr = new Criteria();
//        Criteria criteriaAnd1 = new Criteria();
//        Criteria criteriaAnd2 = new Criteria();
//        List<Criteria> criteriaList1 = new ArrayList<>();
//        criteriaList1.add(Criteria.where("mMofVin").regex(""));
//        criteriaList1.add(Criteria.where("mSvrsessionid").is("1763695921562"));
//        criteriaList1.add(Criteria.where("mPartNo").is("CLU"));
//        List<Criteria> criteriaList2 = new ArrayList<>();
//        criteriaList2.add(Criteria.where("mMofVin").regex(""));
//        criteriaList2.add(Criteria.where("mSvrsessionid").is("1763695921562"));
//        criteriaList2.add(Criteria.where("mPartNo").exists(false));
//        criteriaAnd1.andOperator(criteriaList1.toArray(new Criteria[criteriaList1.size()]));
//        criteriaAnd2.andOperator(criteriaList2.toArray(new Criteria[criteriaList2.size()]));
//
//        criteriaOr.orOperator(criteriaAnd1, criteriaAnd2);
//
//        query.addCriteria(criteriaOr);
//        query.with(Sort.by(new Sort.Order(Sort.Direction.ASC, "mCreateTime")));
//
//        query.limit(10).skip(10 * (1 - 1));
//        List<HMap> list = mongoTemplate.find(query, HMap.class, "SessionAbroadTS");
//        log.info("list: {}", list);


//        String startDateStr = "2025-09-12 12:30";
//        String endDateStr = "2025-09-12 12:32";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        Date startDate = sdf.parse(HtmlUtils.htmlUnescape(startDateStr));
//        Date endDate = sdf.parse(HtmlUtils.htmlUnescape(endDateStr));
//        log.info("startDate: {}", startDate);

//        String name = FilenameUtils.getName("https://cdn-avnt-hyundai.hmgmobility.com.cn/avnt_scomo_prd/NE1KN/ICHN/C/SCOMO/704156/461f955c243344eda55bbe32d4b2f53c.enc");
//        log.info("s: {}", name);

//        Set<String> set = Set.of("96560NI870_5^###^ccNC.CHN.HMC.251017.2503C19^ef666e55b679447ba4fa51f585c3883a.enc",
//                "96560NI870_1^###^NE1KN26.CHN.ccNC.001.001.250808^4419eb22b5c34c05873ad689a0705121.enc",
//                "96560NI870_1^###^NE1KN26.CHN.ccNC.001.001.250828^2e25d19c7c2b45d195f3fe833b48b5c3.enc",
//                "96560NI870_1^###^NE1KN26.CHN.ccNC.001.001.251030^461f955c243344eda55bbe32d4b2f53c.enc"
//        );
//        HMap result = mongoTemplate.findOne(Query.query(Criteria.where("_id").is("691ca618b6a9fb4c78181af2")),
//                HMap.class, "Interface");
////        log.info("result: {}", result.get("ifbody"));
//        Files.writeString(Path.of("C:\\Users\\Admin\\Downloads\\MongoData", "ifbody.json"),
//                result.get("ifbody").toString());
//        ClassPathResource resource = new ClassPathResource("ifbody.json");
//        File file = ResourceUtils.getFile("classpath:ifbody.json");
//        String s = Files.readString(file.toPath());

//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(resource.getInputStream())) {
//            StreamUtils.copy(bufferedInputStream, byteArrayOutputStream);
//        } catch (IOException e) {
//            log.error("", e);
//        }
//        String s = Files.readString(Paths.get("C:\\Users\\Admin\\Downloads\\MongoData\\ifbody1.json"));
//        String s = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
//        JsonNode bodyNode = objectMapper.readTree(s);
//        JsonNode cdnUrlList = bodyNode.get("cdnUrlList");
//        if (cdnUrlList.isArray()) {
//            ArrayNode arrayNode = (ArrayNode) cdnUrlList;
//            Map<String, JsonNode> nodeMap = arrayNode.valueStream()
//                    .filter(n -> set.contains(n.get("scomoNodeNm").textValue()
//                            + "^" + n.get("srcVer").textValue()
//                            + "^" + n.get("tgtVer").textValue()
//                            + "^" + FilenameUtils.getName(n.get("cdnUrl").textValue())))
//                    .collect(Collectors.groupingBy(n -> n.get("scomoNodeNm").textValue()
//                                    + "^" + n.get("srcVer").textValue()
//                                    + "^" + n.get("tgtVer").textValue()
//                                    + "^" + FilenameUtils.getName(n.get("cdnUrl").textValue()),
//                            Collectors.collectingAndThen(Collectors.reducing((c1, c2) -> c1.get("scomoFilId").asInt
//                            () > c2.get("scomoFilId").asInt() ? c1 : c2), Optional::get))
//                    );

//            Map<String, JsonNode> nodeMap = arrayNode.valueStream()
//                    .filter(n -> set.contains(n.get("scomoNodeNm").textValue()
//                            + "^" + n.get("srcVer").textValue()
//                            + "^" + n.get("tgtVer").textValue()
//                            + "^" + FilenameUtils.getName(n.get("cdnUrl").textValue())))
//                    .collect(Collectors.groupingBy(n -> n.get("scomoNodeNm").textValue()
//                                    + "^" + n.get("srcVer").textValue()
//                                    + "^" + n.get("tgtVer").textValue()
//                                    + "^" + FilenameUtils.getName(n.get("cdnUrl").textValue()),
//                            Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(n -> n.get
//                            ("scomoFilId").asInt())),
//                                    Optional::get)
//                    ));

//            Map<String, JsonNode> nodeMap = arrayNode.valueStream()
//                    .filter(n -> set.contains(n.get("scomoNodeNm").textValue()
//                            + "^" + n.get("srcVer").textValue()
//                            + "^" + n.get("tgtVer").textValue()
//                            + "^" + FilenameUtils.getName(n.get("cdnUrl").textValue())))
//                    .collect(Collectors.toMap(n -> n.get("scomoNodeNm").textValue()
//                            + "^" + n.get("srcVer").textValue()
//                            + "^" + n.get("tgtVer").textValue()
//                            + "^" + FilenameUtils.getName(n.get("cdnUrl").textValue()), Function.identity(),
//                            BinaryOperator.maxBy(Comparator.comparing(n -> n.get("scomoFilId").asInt()))));


//        nodeMap.values().forEach(n -> log.info("node: {}", n));
//    }

//        List<String> resultArr = new ArrayList<String>();
//        String SrBody = "902 User cancel V9327DCEMICHN10_021 V9327DCEMICHN10_022";
//        Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(SrBody);
//        while (matcher.find()) {
//            resultArr.add(matcher.group(1));
//        }
//        log.info("resultArr: {}", resultArr);

        }

        private static @NonNull Function<CcuVO, String> getGroupFunc () {
            return vo -> vo.getScomoNodeNm() + "_" + vo.getSrcVer() + "_" + vo.getTgtVer();
        }

//        private void query12 () {
//            StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//            Criteria criteria = new Criteria();
//            List<Criteria> criteriaList = setBasicRoleCriteria();
//
//            criteria.andOperator(criteriaList);
//
//            AggregationOperation match = Aggregation.match(criteria);
//            AggregationOperation group = Aggregation.group("mCreateTime", "mJobServer").count().as("count");
//
//            Aggregation agg = Aggregation.newAggregation(match, group).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
//
//            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "SessionTS", HMap.class);
//            List<HMap> mappedResults = aggregate.getMappedResults();
//            stopWatch.stop();
//            log.info("r: {}", mappedResults);
//            log.info("cost: {}", stopWatch.getTotalTimeSeconds());
//        }

//        private void query11 () {
//            StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//            Criteria criteria = new Criteria();
//            List<Criteria> criteriaList = setBasicRoleCriteria();
//
//            criteria.andOperator(criteriaList);
//
//            AggregationOperation match = Aggregation.match(criteria);
//            AggregationOperation project = Aggregation.project("mCreateTime").andExpression("substr(mCreateTime, 8, 4)").as("createtimeCut");
//            AggregationOperation group = Aggregation.group("createtimeCut").count().as("count");
//
//            Aggregation agg = Aggregation.newAggregation(match, project, group)
//                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
//
//            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "SessionTS", HMap.class);
//
//            List<HMap> mappedResults = aggregate.getMappedResults();
//            stopWatch.stop();
//            log.info("r: {}", mappedResults);
//            log.info("cost: {}", stopWatch.getTotalTimeSeconds());
//        }

        private static List<Criteria> setBasicRoleCriteria () {
            List<Criteria> criteriaList = new ArrayList<>();

            Pair<Date, Date> timePair = TimeUtil.getYesterdayDatePair();
            criteriaList.add(Criteria.where("mCreatedAt").gte(timePair.getLeft()).lt(timePair.getRight()));
            return criteriaList;
        }

        private void queryInterface () {
            HMap hMap = mongoTemplate.findOne(Query.query(Criteria.where("_id").eq(new ObjectId("6a85ef1362fc225ae0dbce9e"))),
                    HMap.class, "Interface");
            try {
                Files.writeString(Path.of("D:\\MongoDB\\cdnUrl2026082001.json"), hMap.getString("ifbody"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private static void jsonObject () throws IOException {
            ClassPathResource classPathResource = new ClassPathResource("ifbody.json");
            JsonElement jsonElement = JsonParser.parseString(classPathResource.getContentAsString(StandardCharsets.UTF_8));
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            log.info("verId: {}", jsonObject.get("verId").getAsString());
            JsonArray jsonArr = jsonObject.get("cdnUrlList").getAsJsonArray();
            for (int i = 0; i < jsonArr.size(); i++) {
                JsonObject jsonObj = jsonArr.get(i).getAsJsonObject();
                String scomoNodeNm = jsonObj.get("scomoNodeNm").getAsString();
                log.info("scomoNodeNm: {}", scomoNodeNm);
            }
        }

        private void queryLogInterface () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("ifapi").is("/file/scomo_ccu"));
            criteriaList.add(Criteria.where("ifbody").regex("P1516DCE01ICHN10_071"));
            criteria.andOperator(criteriaList);
            Query query = Query.query(criteria);
            query.with(Sort.by(Sort.Order.desc("receivetime")));
//        query.skip(2).limit(1);
            List<HMap> mapList = mongoTemplate.find(query, HMap.class, "Interface");
            mapList.forEach(hMap -> {
                log.info("hMap: {}", hMap);
//            try {
//                Files.writeString(Path.of("C:\\Users\\Admin\\Downloads\\DownloadDir12\\fileCcu.json"), hMap.getString("ifbody"));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            });
        }

        private void getCdnUrl () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("ifapi").is("/private/v1/ota/hive/cdnurl"));
            criteriaList.add(Criteria.where("ifbody").regex("\"cdnUrlList\":\\[\\]"));
            criteria.andOperator(criteriaList);
            Query query = Query.query(criteria);
            query.with(Sort.by(Sort.Direction.DESC, "receivetime"));
            query.skip(2).limit(1);
            List<HMap> mapList = mongoTemplate.find(query, HMap.class, "Interface");
            log.info("mapList: {}", mapList);
        }

        private long getUploadLogCount (String verId){
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("ifapi").is("/file/scomo_ccu"));
            criteriaList.add(Criteria.where("ifbody").regex(String.format("\"verId\":\"%s\"", verId)));
            criteria.andOperator(criteriaList);
            long count = mongoTemplate.count(Query.query(criteria), "Interface");
            return count;
        }

        private void callFileApi () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("ifapi").is("/file/scomo_ccu"));
            criteriaList.add(Criteria.where("ifbody").regex("25659"));
//        criteriaList.add(Criteria.where("mVin").not().regex("TEST"));
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
            List<HMap> mapList = mongoTemplate.find(Query.query(criteria), HMap.class, "Interface");
            mapList.stream().filter(hMap -> !hMap.get("Id").equals(new ObjectId("69aa82e9b6e2f206881a4354")))
                    .forEach(hMap -> {
                        log.info("{}", hMap.get("ifbody"));
                    });

//        try (HttpClient client = HttpClient.newHttpClient()) {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("http://10.81.98.115:8020/file/scomo_ccu"))
//                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                    .POST(HttpRequest.BodyPublishers.ofString(mapList.getFirst().get("ifbody").toString()))
//                    .build();
//
//            // 发送请求并获取响应
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            // 打印响应状态码和响应内容
//            System.out.println("Response Code: " + response.statusCode());
//            System.out.println("Response Body: " + response.body());
//        } catch (Exception e) {
//            log.error("", e);
//        }
        }

        private void fileApi () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("ifapi").is("/file/scomo_ccu"));
            criteriaList.add(Criteria.where("ifbody").regex(String.format("\"scomoFileId\":%s", "25566")));
            criteriaList.add(Criteria.where("ifbody").regex(String.format("\"verId\":\"%s\"", "P0354DCE01ICHN10_146")));
//        criteriaList.add(Criteria.where("mVin").not().regex("TEST"));
            criteria.andOperator(criteriaList);
            DataMap anInterface = mongoTemplate.findOne(Query.query(criteria), DataMap.class, "Interface");
            log.info("anInterface: {}", anInterface);

            String jsonStr = anInterface.getString("ifbody");
            log.info("jsonStr: {}", jsonStr);


            callCdnProxyApi(jsonStr);
        }

        private static void callCdnProxyApi (String jsonStr){
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://10.81.98.115:8020/file/scomo_ccu"))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonStr))
                        .build();

                // 发送请求并获取响应
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                // 打印响应状态码和响应内容
                System.out.println("Response Code: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            } catch (Exception e) {
                log.error("", e);
            }
        }

        private void eventUpdateStat () {
            EvntVersionUpdateStatusVO vo = new EvntVersionUpdateStatusVO();
            vo.setPartNo("HU");
            vo.setTargetEventID("P0199PCE07ICHN10_010");
            vo.setSelectPckgNm("NE1KN^ICHN^01");
            vo.setEventVer("P0199PCE07ICHN10_010/NE1KN_ccNC_CHN_2501RU");
            vo.setDateText1("2025-12-22 00:00");
            vo.setDateText2("2025-12-31 12:05");
            vo.setStartDate("202512220000");
            vo.setEndDate("202601041029");
            vo.setEvntType("P");
            vo.setCurrPage(1);
            vo.setLimit(100);
            vo.setDownloadOrUpdate("update");
            List<Map<String, String>> result = evntVersionUpdateStatusService.selectEvntDownloadSuccessDetailList(vo);
            log.info("result: {}", result.size());
        }

        private void versionStat () throws Exception {
            EvntVersionUpdateStatusVO vo = new EvntVersionUpdateStatusVO();
            vo.setSelectPckgNm("NE1KN^ICHN^01");
            vo.setEventVer("P0199PCE07ICHN10_010/NE1KN_ccNC_CHN_2501RU");
            vo.setDateText1("2025-12-22 00:00");
            vo.setDateText2("2025-12-31 12:05");
            vo.setStartDate("202512220000");
            vo.setEndDate("202512311205");
            vo.setEvntType("P");
            vo.setCurrPage(1);
//        List<Map<String, String>> mapList = evntVersionUpdateStatusService.pckgNmUpdateStateNew(vo);
//        log.info("mapList: {}", mapList);
            List<Map<String, String>> partNoUpdateList = evntVersionUpdateStatusService.partNoUpdateStateNew(vo);
            log.info("partNoUpdateList: {}", partNoUpdateList);
        }

        public List<HMap> versionStats () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();


            criteriaList.add(Criteria.where("testDv").is("P"));


            criteriaList.add(Criteria.where("packagename").is("NE1KN^ICHN^01"));


            criteriaList.add(Criteria.where("region").regex(""));

//        criteriaList.add(Criteria.where("recentEventVersionID").ne(""));
//        criteriaList.add(Criteria.where("recentEventVersionID").ne("NULL"));
//        criteriaList.add(Criteria.where("recentEventVersionID").exists(true));


            if (criteriaList.size() > 0) {
                criteria.andOperator(criteriaList);
            }
            String sortStr = "    {" +
                    "        $sort: {" +
                    "            _id.packagename: 1" +
                    "        }" +
                    "    }";

            AggregationOperation match = Aggregation.match(criteria);
            AggregationOperation group1 = Aggregation.group(Fields.fields().and("region").and("packagename").and(
                    "recentEventVersionID").and("recentEventVersionNm")).count().as("vinCount");
            AggregationOperation group2 = Aggregation.group(Fields.fields().and("$_id.region").and("$_id.packagename"))
                    .push(new BasicDBObject("recentEventVersionID", "$_id.recentEventVersionID").append(
                            "recentEventVersionNm", "$_id.recentEventVersionNm").append("count", "$vinCount")).as("vins").sum("vinCount").as("totalCount");
            AggregationOperation sort = Aggregation.sort(Sort.Direction.ASC, "recentEventVersionID");
            Aggregation agg = Aggregation.newAggregation(match, group1, sort, group2);
            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "Device", HMap.class);
            return aggregate.getMappedResults();
        }

        private void selectDeviceList () {
            Query query = new Query();
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("packagename").regex(""));
            criteriaList.add(Criteria.where("testDv").is("P"));
            criteriaList.add(Criteria.where("recentEventVersionID").is("P0199PCE07ICHN10_006"));
            criteriaList.add(Criteria.where("recentEventVersionNm").is("NE1KN_ccNC_CHN_SOP_RC1"));
            criteria.andOperator(criteriaList);
            query.addCriteria(criteria);
//        query.with(Sort.by(new Sort.Order(Sort.Direction.DESC, "registerTime")));
            query.with(Sort.by(Sort.Order.desc("registerTime")));
            query.limit(10).skip(0);
            List<HMap> deviceList = mongoTemplate.find(query, HMap.class, "Device");
            if (CollectionUtils.isNotEmpty(deviceList)) {
                log.info("deviceList size: {}", deviceList.size());
                deviceList.forEach(d -> log.info("d: {}", d));
            }
        }

        private void query () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();
            String startDate = "20251222143700";
            String endDate = "20251225143659";
            String packagename = "NE1KN^ICHN^01";
            String evntVer = "P0199PCE07ICHN10_010";
            String evntVerNm = "NE1KN_ccNC_CHN_2501RU";
            String evntType = "P";
            criteriaList.add(Criteria.where("requesttime").gte(startDate).lte(endDate));
            criteriaList.add(Criteria.where("packagename").in(packagename));
            criteriaList.add(Criteria.where("targetEventID").in(evntVer));
            criteriaList.add(Criteria.where("targetEventName").in(HtmlUtils.htmlUnescape(evntVerNm)));
            criteriaList.add(Criteria.where("testDv").in(evntType));
            criteria.andOperator(criteriaList);
            // match
            AggregationOperation match = Aggregation.match(criteria);
            // group
            AggregationOperation group = Aggregation.group(Fields.fields().and("vin").and("partNo")).max("createdAt").as(
                    "maxCreatedAt");
            EvaluationOperators.Expr expr = EvaluationOperators.Expr.valueOf(BooleanOperators.And.and(
                    ComparisonOperators.valueOf("$vin").equalTo("$$vin"),
                    ComparisonOperators.valueOf("$partNo").equalTo("$$partNo"),
                    ComparisonOperators.valueOf("$createdAt").equalTo("$$createdAt")));
            LookupOperation lookup = Aggregation.lookup().from("StatDeviceDownload").let(
                            VariableOperators.Let.ExpressionVariable.newVariable("vin").forField("$_id.vin"),
                            VariableOperators.Let.ExpressionVariable.newVariable("partNo").forField("$_id.partNo"),
                            VariableOperators.Let.ExpressionVariable.newVariable("createdAt").forField("$maxCreatedAt"))
                    .pipeline(Aggregation.match(expr))
                    .as("matchdata");
            UnwindOperation unwind = Aggregation.unwind("$matchdata");
            FacetOperation facet =
                    Aggregation.facet(Aggregation.match(Criteria.where("matchdata.downloadresult").in(List.of("0"))),
                                    Aggregation.group("matchdata.packagename", "matchdata.downloadresult").count().as(
                                            "downloadresultcount"),
                                    Aggregation.group("$_id.packagename").push(new BasicDBObject("downloadresult", "$_id" +
                                                    ".downloadresult")
                                                    .append("count", "$downloadresultcount")).as("downloadresults")
                                            .sum("$downloadresultcount").as("count")
                            ).as("download_ing_data")
                            .and(Aggregation.match(new Criteria().andOperator(List.of(
                                            Criteria.where("matchdata.reportCode").in(List.of("1002")),
                                            Criteria.where("matchdata.isresultCode").is("")).toArray(new Criteria[0]))),
                                    Aggregation.group("matchdata.packagename", "matchdata.reportCode").count().as(
                                            "reportCodecount"),
                                    Aggregation.group("$_id.packagename").push(new BasicDBObject("reportCode", "$_id.reportCode")
                                                    .append("count", "$reportCodecount")).as("reportCodes")
                                            .sum("$reportCodecount").as("count")).as("update_ing_data")
                            .and(Aggregation.group("matchdata.packagename", "matchdata.srresultCode").count().as(
                                            "srresultCodecount"),
                                    Aggregation.sort(Sort.Direction.ASC, "_id.srresultCode"),
                                    Aggregation.group("$_id.packagename").push(new BasicDBObject("srresultCode", "$_id" +
                                                    ".srresultCode")
                                                    .append("count", "$srresultCodecount")).as("srresultCodes")
                                            .sum("$srresultCodecount").as("count")).as("download_data")
                            .and(Aggregation.group("matchdata.packagename", "matchdata.isresultCode").count().as(
                                            "isresultCodecount"),
                                    Aggregation.sort(Sort.Direction.ASC, "_id.isresultCode"),
                                    Aggregation.group("$_id.packagename").push(new BasicDBObject("isresultCode", "$_id" +
                                                    ".isresultCode")
                                                    .append("count", "$isresultCodecount")).as("isresultCodes")
                                            .sum("$isresultCodecount").as("count")).as("update_data")
                            .and(Aggregation.group("matchdata.packagename").addToSet("$matchdata.partNo").as("partNos"),
                                    Aggregation.unwind("$partNos"),
                                    Aggregation.group("$_id")
                                            .count().as("partNoCount")).as("part_count");
            Aggregation agg =
                    Aggregation.newAggregation(match, group, lookup, unwind, facet).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

            log.info("======================================");
            log.info("HMap Result ::: " + aggregate.getMappedResults());
        }

        private void deployStatistics () throws IOException {
//        String jsonStr = Files.readString(Path.of("C:\\Users\\Admin\\Downloads\\Projects\\jsonCommand2.json"));
            ClassPathResource classPathResource = new ClassPathResource("jsonCommand2.json");
            String jsonStr = Files.readString(classPathResource.getFile().toPath());
            JsonMapper mapper = new JsonMapper();
            try {
                JsonNode jsonNode = mapper.readTree(jsonStr);
                List<BsonDocument> bsonList = jsonNode.valueStream()
                        .map(node -> BsonDocument.parse(node.toString()))
                        .collect(Collectors.toList());
//            ArrayNode arrayNode = (ArrayNode) mapper.readTree(jsonStr);
//            List<Bson> bsonList = new ArrayList<>();
//            for (JsonNode jsonNode : arrayNode) {
//                Document document = MongoExpression.create(jsonNode.toString()).toDocument();
//                bsonList.add(document.toBsonDocument());
//            }
                AggregateIterable<Document> aggregateIterable = mongoTemplate.getCollection("StatDeviceDownload")
                        .aggregate(bsonList);
                List<Document> documentList = new ArrayList<>();
                try (MongoCursor<Document> cursor = aggregateIterable.cursor()) {
                    if (cursor.hasNext()) {
                        Document document = cursor.next();
                        log.info("{}", document);
                        documentList.add(document);
                    }
                }
                List<Map<String, String>> resultList = parse(documentList);
                log.info("resultList: {}", resultList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void deployStatistics2 () throws IOException {
//        String jsonStr = Files.readString(Path.of("C:\\Users\\Admin\\Downloads\\Projects\\jsonCommand2.json"));
            ClassPathResource classPathResource = new ClassPathResource("jsonCommand3.json");
            String jsonStr = Files.readString(classPathResource.getFile().toPath());
            JsonMapper mapper = new JsonMapper();
            try {
                tools.jackson.databind.JsonNode jsonNode = mapper.readTree(jsonStr);
                List<BsonDocument> bsonList = jsonNode.valueStream()
                        .map(node -> BsonDocument.parse(node.toString()))
                        .collect(Collectors.toList());
//            ArrayNode arrayNode = (ArrayNode) mapper.readTree(jsonStr);
//            List<Bson> bsonList = new ArrayList<>();
//            for (JsonNode jsonNode : arrayNode) {
//                Document document = MongoExpression.create(jsonNode.toString()).toDocument();
//                bsonList.add(document.toBsonDocument());
//            }


                AggregateIterable<Document> aggregateIterable = mongoTemplate.getCollection("StatDeviceDownload")
                        .aggregate(bsonList);
                List<Document> documentList = new ArrayList<>();
                try (MongoCursor<Document> cursor = aggregateIterable.cursor()) {
                    if (cursor.hasNext()) {
                        Document document = cursor.next();
                        log.info("{}", document);
                        documentList.add(document);
                    }
                }
                log.info("documentList: {}", documentList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Document{{download_ing_data=[Document{{_id=Document{{packagename=NE1KN^ICHN^01}},
        // downloadresults=[Document{{downloadresult=0, count=1}}], count=1}}],
        // update_ing_data=[Document{{_id=Document{{packagename=NE1KN^ICHN^01}}, reportCodes=[Document{{reportCode=1002,
        // count=9}}], count=9}}], download_data=[Document{{_id=Document{{packagename=NE1KN^ICHN^01}},
        // srresultCodes=[Document{{srresultCode=, count=1}}, Document{{srresultCode=900, count=59}}], count=60}}],
        // update_data=[Document{{_id=Document{{packagename=NE1KN^ICHN^01}}, isresultCodes=[Document{{isresultCode=,
        // count=20}}, Document{{isresultCode=1200, count=40}}], count=60}}],
        // part_count=[Document{{_id=Document{{packagename=NE1KN^ICHN^01}}, partNoCount=2}}]}}
        private List<Map<String, String>> parse (List < Document > documentList) {
            List<Map<String, String>> result = new ArrayList<>();

            for (Document temp : documentList) {
                String pckgNm = "";
                int totalPartCnt = 0;
                int downloadProgress = 0;
                int downloadSuccess = 0;
                int downloadFail = 0;
                int downloadEtc = 0;
                double downloadFailRate = 0;
                int updateProgress = 0;
                int updateSuccess = 0;
                int updateFail = 0;
                int updateEtc = 0;
                double updateFailRate = 0;

                if (temp.containsKey("download_ing_data")) {
                    List<Document> tempdownload_ing_data = (List<Document>) temp.get("download_ing_data");

                    for (Document temp1 : tempdownload_ing_data) {
                        if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                            Document temp2 = (Document) temp1.get("_id");
                            if (temp2.containsKey("packagename")) {
                                pckgNm = (String) temp2.get("packagename");
                            }
                        }

                        if (temp1.containsKey("downloadresults")) {
                            List<Document> tempDownloadResults = (List<Document>) temp1.get("downloadresults");

                            for (Document temp2 : tempDownloadResults) {
                                if (temp2.containsKey("downloadresult")) {
                                    String downloadresult = (String) temp2.get("downloadresult");

                                    if ("0".equals(downloadresult)) {
                                        downloadProgress = (int) temp2.get("count");
                                    }
                                }
                            }
                        }
                    }
                }

                if (temp.containsKey("update_ing_data")) {
                    List<Document> tempdownload_ing_data = (List<Document>) temp.get("update_ing_data");

                    for (Document temp1 : tempdownload_ing_data) {
                        if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                            Document temp2 = (Document) temp1.get("_id");
                            if (temp2.containsKey("packagename")) {
                                pckgNm = (String) temp2.get("packagename");
                            }
                        }

                        if (temp1.containsKey("reportCodes")) {
                            List<Document> tempReportCodes = (List<Document>) temp1.get("reportCodes");

                            for (Document temp2 : tempReportCodes) {
                                if (temp2.containsKey("reportCode")) {
                                    String reportCode = (String) temp2.get("reportCode");

                                    if ("1002".equals(reportCode)) {
                                        updateProgress = (int) temp2.get("count");
                                    }
                                }
                            }
                        }
                    }
                }

                if (temp.containsKey("download_data")) {
                    List<Document> tempdownload_data = (List<Document>) temp.get("download_data");

                    for (Document temp1 : tempdownload_data) {
                        if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                            Document temp2 = (Document) temp1.get("_id");
                            if (temp2.containsKey("packagename")) {
                                pckgNm = (String) temp2.get("packagename");
                            }
                        }

                        if (temp1.containsKey("srresultCodes")) {
                            List<Document> tempSrResultCodes = (List<Document>) temp1.get("srresultCodes");

                            for (Document temp2 : tempSrResultCodes) {
                                if (temp2.containsKey("srresultCode")) {
                                    String srresultCode = (String) temp2.get("srresultCode");

                                    if (!srresultCode.isEmpty()) {
                                        int intSrresultCode = Integer.parseInt(srresultCode);

                                        if (intSrresultCode == 900) {
                                            if (downloadSuccess == 0) {
                                                downloadSuccess = (int) temp2.get("count");
                                            } else {
                                                downloadSuccess += (int) temp2.get("count");
                                            }
                                        } else if (intSrresultCode == 901 || intSrresultCode == 903 || intSrresultCode == 905 ||
                                                (intSrresultCode >= 951 && intSrresultCode < 955)) {
                                            if (downloadFail == 0) {
                                                downloadFail = (int) temp2.get("count");
                                            } else {
                                                downloadFail += (int) temp2.get("count");
                                            }
                                        } else if (intSrresultCode == 902) {
                                            if (downloadEtc == 0) {
                                                downloadEtc = (int) temp2.get("count");
                                            } else {
                                                downloadEtc += (int) temp2.get("count");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (temp.containsKey("update_data")) {
                    List<Document> tempupdate_data = (List<Document>) temp.get("update_data");

                    for (Document temp1 : tempupdate_data) {
                        if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                            Document temp2 = (Document) temp1.get("_id");
                            if (temp2.containsKey("packagename")) {
                                pckgNm = (String) temp2.get("packagename");
                            }
                        }

                        if (temp1.containsKey("isresultCodes")) {
                            List<Document> tempDownloadResults = (List<Document>) temp1.get("isresultCodes");

                            for (Document temp2 : tempDownloadResults) {
                                if (temp2.containsKey("isresultCode")) {
                                    String isresultCode = (String) temp2.get("isresultCode");

                                    if (!isresultCode.isEmpty()) {
                                        int intIsresultCode = Integer.parseInt(isresultCode);

                                        if (intIsresultCode == 1200 || (intIsresultCode >= 1251 && intIsresultCode < 1300)) {
                                            if (updateSuccess == 0) {
                                                updateSuccess = (int) temp2.get("count");
                                            } else {
                                                updateSuccess += (int) temp2.get("count");
                                            }
                                        } else if (intIsresultCode == 1400 ||
                                                (intIsresultCode >= 1402 && intIsresultCode < 1414) ||
                                                (intIsresultCode >= 1450 && intIsresultCode < 1502) ||
                                                (intIsresultCode >= 1550 && intIsresultCode < 1600)) {
                                            if (updateFail == 0) {
                                                updateFail = (int) temp2.get("count");
                                            } else {
                                                updateFail += (int) temp2.get("count");
                                            }
                                        } else if (intIsresultCode == 1401) {
                                            if (updateEtc == 0) {
                                                updateEtc = (int) temp2.get("count");
                                            } else {
                                                updateEtc += (int) temp2.get("count");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (temp.containsKey("part_count")) {
                    List<Document> temppart_count = (List<Document>) temp.get("part_count");

                    for (Document temp1 : temppart_count) {
                        if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                            Document temp2 = (Document) temp1.get("_id");
                            if (temp2.containsKey("packagename")) {
                                pckgNm = (String) temp2.get("packagename");
                            }
                        }

                        if (temp1.containsKey("partNoCount")) {
                            totalPartCnt = (int) temp1.get("partNoCount");
                        }
                    }
                }

                int downloadTotal = downloadProgress + downloadSuccess + downloadFail + downloadEtc;
                if (downloadTotal > 0) {
                    downloadFailRate = Math.round(((double) downloadFail / (double) downloadTotal) * 1000) / (double) 10;
                }

                int updateTotal = updateProgress + updateSuccess + updateFail + updateEtc;
                if (updateTotal > 0) {
                    updateFailRate = Math.round(((double) updateFail / (double) updateTotal) * 1000) / (double) 10;
                }

                Map<String, String> resultMap = new HashMap<String, String>();

                resultMap.put("pckgNm", pckgNm);
                resultMap.put("totalPartCnt", String.valueOf(totalPartCnt));
                resultMap.put("downloadProgress", String.valueOf(downloadProgress));
                resultMap.put("downloadSuccess", String.valueOf(downloadSuccess));
                resultMap.put("downloadFail", String.valueOf(downloadFail));
                resultMap.put("downloadEtc", String.valueOf(downloadEtc));
                resultMap.put("downloadFailRate", String.valueOf(downloadFailRate));
                resultMap.put("updateProgress", String.valueOf(updateProgress));
                resultMap.put("updateSuccess", String.valueOf(updateSuccess));
                resultMap.put("updateFail", String.valueOf(updateFail));
                resultMap.put("updateEtc", String.valueOf(updateEtc));
                resultMap.put("updateFailRate", String.valueOf(updateFailRate));

                result.add(resultMap);
            }

            return result;
        }

        private void selectDayInstallStatusCntNew () {
            Map<String, Integer> dayInstallIngStatusCntMap = selectDayInstallStatusCntIng();

            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();

            Date nowDate = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(nowDate);
            cal.add(Calendar.DATE, -1);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

            String startDate = sdf.format(cal.getTime()) + "000000";
            String endDate = sdf.format(cal.getTime()) + "235959";

            criteriaList.add(Criteria.where("isresultTime").gte(startDate).lt(endDate));

            criteria.andOperator(criteriaList);

            AggregationOperation match = Aggregation.match(criteria);
            AggregationOperation group1 = Aggregation.group(
                    Fields.fields().and("packagename").and("testDv").and("carModel").and("region").and("targetEventID").and("targetEventName").and("partNo")
                            .and("isresultCode")).count().as("isResultCodeCount");
            AggregationOperation group2 = Aggregation.group(
                            Fields.fields().and("$_id.packagename").and("$_id.testDv").and("$_id.carModel").and("$_id" +
                                            ".region").and("$_id.targetEventID")
                                    .and("$_id.targetEventName").and("$_id.partNo"))
                    .push(new BasicDBObject("isresultCode", "$_id.isresultCode").append("count", "$isResultCodeCount")).as("isresultCodes")
                    .sum("isResultCodeCount").as("totalCount");

            Aggregation agg = Aggregation.newAggregation(match, group1, group2)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

            log.info("======================================");
            log.info("agg NOSQL ::: " + agg.toString());
            log.info("======================================");

            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

            log.info("======================================");
            log.info("HMap Result ::: " + aggregate.getMappedResults());
            log.info("======================================");

            List<HMap> dayInstallStatusCntList = aggregate.getMappedResults();

            if (!dayInstallStatusCntList.isEmpty()) {
                List<DpoySuccFailStatVO> saveDataList = new ArrayList<>();

                for (HMap tempHMap : dayInstallStatusCntList) {
                    int succCnt = 0;
                    int failCnt = 0;
                    int etcCnt = 0;
                    int ingCnt = 0;
                    int totalCnt = 0;

                    Tuple7<String, String, String, String, String, String, String> idTuple = getIdTuple(tempHMap);
                    log.info("idTuple: {}", idTuple);

                    if (tempHMap.containsKey("isresultCodes")) {
                        List<Map<String, Object>> isresultCodeList = (List<Map<String, Object>>) tempHMap.get(
                                "isresultCodes");

                        for (Map<String, Object> tempUpdateData : isresultCodeList) {
                            if (tempUpdateData.containsKey("isresultCode")) {
                                String isresultCode = String.valueOf(tempUpdateData.get("isresultCode"));

                                int tempCnt = 0;
                                if (tempUpdateData.containsKey("count")) {
                                    tempCnt = Integer.parseInt(String.valueOf(tempUpdateData.get("count")));
                                }

                                int intIsResultCode = Integer.parseInt(isresultCode);

                                if (intIsResultCode == 1200 ||
                                        (intIsResultCode >= 1250 && intIsResultCode <= 1299)) {
                                    if (succCnt == 0) {
                                        succCnt = tempCnt;
                                    } else {
                                        succCnt += tempCnt;
                                    }
                                } else if (intIsResultCode == 1401) {
                                    etcCnt = tempCnt;
                                } else if (intIsResultCode == 1400 ||
                                        (intIsResultCode >= 1402 && intIsResultCode <= 1413) ||
                                        (intIsResultCode >= 1450 && intIsResultCode <= 1501) ||
                                        (intIsResultCode >= 1550 && intIsResultCode <= 1599)) {
                                    if (failCnt == 0) {
                                        failCnt = tempCnt;
                                    } else {
                                        failCnt += tempCnt;
                                    }
                                }
                            }
                        }
                    }

                    if (tempHMap.containsKey("totalCount")) {
                        totalCnt = (int) tempHMap.get("totalCount");
                    }

                    DpoySuccFailStatVO dpoySuccFailStatVO = new DpoySuccFailStatVO();

                    SimpleDateFormat statDate = new SimpleDateFormat("yyyyMMdd");
                    dpoySuccFailStatVO.setRgstD(statDate.format(cal.getTime()));
                    dpoySuccFailStatVO.setPckgNm(idTuple._1);
                    dpoySuccFailStatVO.setPckgSt(idTuple._2);
                    dpoySuccFailStatVO.setCarMdl(idTuple._3);
                    dpoySuccFailStatVO.setRegn(idTuple._4);
                    dpoySuccFailStatVO.setTgtVer(idTuple._5);
                    if (!idTuple._6.isEmpty()) {
                        dpoySuccFailStatVO.setVerNm(idTuple._6);
                    }
                    dpoySuccFailStatVO.setPartNo(idTuple._7);
                    if (succCnt > 0) {
                        dpoySuccFailStatVO.setSuccVinCnt(succCnt);
                    }
                    if (failCnt > 0) {
                        dpoySuccFailStatVO.setFailVinCnt(failCnt);
                    }
                    if (etcCnt > 0) {
                        dpoySuccFailStatVO.setEtcVinCnt(etcCnt);
                    }
                    String key = idTuple.toSeq().mkString("@");
                    if (dayInstallIngStatusCntMap.containsKey(key)) {
                        ingCnt = dayInstallIngStatusCntMap.get(key);
                        if (ingCnt > 0) {
                            dpoySuccFailStatVO.setIngVinCnt(ingCnt);
                            if (succCnt > 0 || failCnt > 0 || etcCnt > 0) {
                                totalCnt += ingCnt;
                            } else {
                                totalCnt = ingCnt;
                            }
                        }
                    }
                    if (totalCnt > 0) {
                        dpoySuccFailStatVO.setTotVinCnt(totalCnt);
                    }

                    if (succCnt > 0 || failCnt > 0 || etcCnt > 0 || ingCnt > 0) {
                        saveDataList.add(dpoySuccFailStatVO);
                    }
                }

                log.info("saveDataList: {}", saveDataList);
            }
        }

        private Map<String, Integer> selectDayInstallStatusCntIng () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();

            Date nowDate = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(nowDate);
            cal.add(Calendar.DATE, -1);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

            String startDate = sdf.format(cal.getTime()) + "000000";
            String endDate = sdf.format(cal.getTime()) + "235959";

            criteriaList.add(Criteria.where("requesttime").gte(startDate).lt(endDate));

            criteriaList.add(Criteria.where("reportCode").is("1002"));

            criteria.andOperator(criteriaList);

            AggregationOperation match = Aggregation.match(criteria);
            AggregationOperation group1 = Aggregation.group(
                    Fields.fields().and("packagename").and("testDv").and("carModel").and("region").and("targetEventID").and("targetEventName").and("partNo")
                            .and("reportCode")).count().as("reportCodeCount");
            AggregationOperation group2 = Aggregation.group(
                            Fields.fields().and("$_id.packagename").and("$_id.testDv").and("$_id.carModel").and("$_id" +
                                            ".region").and("$_id.targetEventID")
                                    .and("$_id.targetEventName").and("$_id.partNo"))
                    .push(new BasicDBObject("reportCode", "$_id.reportCode").append("count", "$reportCodeCount")).as(
                            "reportCodes").sum("reportCodeCount")
                    .as("totalCount");

            Aggregation agg = Aggregation.newAggregation(match, group1, group2)
                    .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

            log.info("======================================");
            log.info("agg NOSQL ::: " + agg.toString());
            log.info("======================================");

            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

            log.info("======================================");
            log.info("HMap Result ::: " + aggregate.getMappedResults());
            log.info("======================================");

            Map<String, Integer> dayInstallIngStatusCntMap = new HashMap<>();
            List<HMap> dayInstallStatusIngCntList = aggregate.getMappedResults();

            if (!dayInstallStatusIngCntList.isEmpty()) {
                for (HMap tempHMap : dayInstallStatusIngCntList) {
                    Tuple7<String, String, String, String, String, String, String> idTuple = getIdTuple(tempHMap);

                    if (tempHMap.containsKey("reportCodes")) {
                        List<Map<String, Object>> reportCodeList = (List<Map<String, Object>>) tempHMap.get("reportCodes");

                        for (Map<String, Object> tempUpdateData : reportCodeList) {
                            if (tempUpdateData.containsKey("count")) {
                                int tempCnt = Integer.parseInt(String.valueOf(tempUpdateData.get("count")));


                                String key = idTuple.toSeq().mkString("@");
//                            String key = tuple7._1 + "@" + tuple7._2 + "@" + tuple7._3 + "@" + tuple7._4 + "@" +
//                            tuple7._5 + "@" + tuple7._6 + "@" + tuple7._7;

                                if (!dayInstallIngStatusCntMap.containsKey(key)) {
                                    dayInstallIngStatusCntMap.put(key, tempCnt);
                                }
                            }
                        }
                    }
                }
            }

            log.info("dayInstallIngStatusCntMap: {}", dayInstallIngStatusCntMap);
            return dayInstallIngStatusCntMap;
        }

        private static Tuple7<String, String, String, String, String, String, String> getIdTuple (HMap tempHMap){
            String pckgNm = "";
            String pckgSt = "";
            String carMdl = "";
            String region = "";
            String tgtVer = "";
            String verNm = "";
            String partNo = "";

            if (tempHMap.containsKey("Id")) {
                if (tempHMap.get("Id") instanceof LinkedHashMap<?, ?>) {
                    HashMap<String, String> idMap = (LinkedHashMap<String, String>) tempHMap.get("Id");
                    if (idMap.containsKey("packagename")) {
                        pckgNm = idMap.get("packagename");
                    }

                    if (idMap.containsKey("testDv")) {
                        String testDv = idMap.get("testDv");

                        switch (testDv) {
                            case "D":
                                pckgSt = "DEV_ABLE";
                                break;
                            case "T":
                                pckgSt = "TEST_ABLE";
                                break;
                            case "P":
                                pckgSt = "DPOY_ABLE";
                                break;
                        }
                    }

                    if (idMap.containsKey("carModel")) {
                        carMdl = idMap.get("carModel");
                    }

                    if (idMap.containsKey("region")) {
                        region = idMap.get("region");
                    }

                    if (idMap.containsKey("targetEventID")) {
                        tgtVer = idMap.get("targetEventID");
                    }

                    if (idMap.containsKey("targetEventName")) {
                        verNm = idMap.get("targetEventName");
                    }

                    if (idMap.containsKey("partNo")) {
                        partNo = idMap.get("partNo");
                    }
                }
            }

            return Tuple.of(pckgNm, pckgSt, carMdl, region, tgtVer, verNm, partNo);
        }

        public void q () {
            Criteria criteria = new Criteria();
            List<Criteria> criteriaList = new ArrayList<>();

            criteriaList.add(Criteria.where("targetEventID").is("P0199PCE07ICHN10_010"));


            criteriaList.add(Criteria.where("carModel").in("NE1KN"));


            criteriaList.add(Criteria.where("region").in("CHN"));


            criteriaList.add(Criteria.where("partNo").in("MAINCONFIG"));


            criteriaList.add(Criteria.where("testDv").in("P"));


            criteria.andOperator(criteriaList);

            // match
            AggregationOperation match = Aggregation.match(criteria);
            // group
            AggregationOperation group =
                    Aggregation.group(Fields.fields().and("vin").and("carModel").and("region").and("targetEventID").and(
                            "targetEventName").and("partNo").and("packagename")).max("createdAt").as("maxCreatedAt");
            // lookup
            String strLookup = "{\"$lookup\": {\"from\": \"StatDeviceDownload\", \"let\": {\"vin\": \"$_id.vin\", " +
                    "\"carModel\": \"$_id.carModel\", \"region\": \"$_id.region\", \"targetEventID\": \"$_id" +
                    ".targetEventID\", \"targetEventName\": \"$_id.targetEventName\", \"packagename\": \"$_id" +
                    ".packagename\",\"partNo\": \"$_id.partNo\", \"createdAt\": \"$maxCreatedAt\"}, \"pipeline\": " +
                    "[{\"$match\": {\"$expr\": {\"$and\": [{\"$eq\": [\"$vin\", \"$$vin\"]}, {\"$eq\": [\"$partNo\", " +
                    "\"$$partNo\"]}, {\"$eq\": [\"$createdAt\", \"$$createdAt\"]}]}}}], \"as\": \"matchdata\"}}";
            AggregationOperation lookup = new JsonAggregationOperation(strLookup);
            // unwind
            String strUnwind = "{\"$unwind\": \"$matchdata\"}";
            AggregationOperation unwind = new JsonAggregationOperation(strUnwind);
            // facet
            String strFacet = "{\"$facet\":{\"is_result_code_data\":[{\"$group\":{\"_id\":{\"carModel\":\"$matchdata" +
                    ".carModel\",\"region\":\"$matchdata.region\",\"sourceEventID\":\"$matchdata.sourceEventID\"," +
                    "\"targetEventID\":\"$matchdata.targetEventID\",\"partNo\":\"$matchdata.partNo\"," +
                    "\"isresultCode\":\"$matchdata.isresultCode\",\"packagename\":\"$matchdata.packagename\"}," +
                    "\"isresultCodeCount\":{\"$sum\":1}}},{\"$group\":{\"_id\":{\"carModel\":\"$_id.carModel\"," +
                    "\"region\":\"$_id.region\",\"sourceEventID\":\"$_id.sourceEventID\",\"targetEventID\":\"$_id" +
                    ".targetEventID\",\"partNo\":\"$_id.partNo\",\"packagename\":\"$_id.packagename\"}," +
                    "\"isresultCodes\":{\"$push\":{\"isresultCode\":\"$_id.isresultCode\"," +
                    "\"count\":\"$isresultCodeCount\"}},\"totalCount\":{\"$sum\":\"$isresultCodeCount\"}}}]," +
                    "\"sr_result_code_data\":[{\"$group\":{\"_id\":{\"carModel\":\"$matchdata.carModel\"," +
                    "\"region\":\"$matchdata.region\",\"sourceEventID\":\"$matchdata.sourceEventID\"," +
                    "\"targetEventID\":\"$matchdata.targetEventID\",\"partNo\":\"$matchdata.partNo\"," +
                    "\"srresultCode\":\"$matchdata.srresultCode\",\"packagename\":\"$matchdata.packagename\"}," +
                    "\"srresultCodeCount\":{\"$sum\":1}}},{\"$group\":{\"_id\":{\"carModel\":\"$_id.carModel\"," +
                    "\"region\":\"$_id.region\",\"sourceEventID\":\"$_id.sourceEventID\",\"targetEventID\":\"$_id" +
                    ".targetEventID\",\"partNo\":\"$_id.partNo\",\"packagename\":\"$_id.packagename\"}," +
                    "\"srresultCodes\":{\"$push\":{\"srresultCode\":\"$_id.srresultCode\"," +
                    "\"count\":\"$srresultCodeCount\"}},\"totalCount\":{\"$sum\":\"$srresultCodeCount\"}}}]," +
                    "\"detail_result_code_data\":[{\"$group\":{\"_id\":{\"carModel\":\"$matchdata.carModel\"," +
                    "\"region\":\"$matchdata.region\",\"sourceEventID\":\"$matchdata.sourceEventID\"," +
                    "\"targetEventID\":\"$matchdata.targetEventID\",\"partNo\":\"$matchdata.partNo\"," +
                    "\"reportDetailResult\":\"$matchdata.reportDetailResult\",\"packagename\":\"$matchdata" +
                    ".packagename\"},\"reportDetailResultCount\":{\"$sum\":1}}}," +
                    "{\"$group\":{\"_id\":{\"carModel\":\"$_id.carModel\",\"region\":\"$_id.region\"," +
                    "\"sourceEventID\":\"$_id.sourceEventID\",\"targetEventID\":\"$_id.targetEventID\"," +
                    "\"partNo\":\"$_id.partNo\",\"packagename\":\"$_id.packagename\"}," +
                    "\"reportDetailResults\":{\"$push\":{\"reportDetailResult\":\"$_id.reportDetailResult\"," +
                    "\"count\":\"$reportDetailResultCount\"}}," +
                    "\"totalCount\":{\"$sum\":\"$reportDetailResultCount\"}}}]," +
                    "\"vin_count\":[{\"$group\":{\"_id\":{\"carModel\":\"$matchdata.carModel\"," +
                    "\"region\":\"$matchdata.region\",\"sourceEventID\":\"$matchdata.sourceEventID\"," +
                    "\"targetEventID\":\"$matchdata.targetEventID\",\"partNo\":\"$matchdata.partNo\"," +
                    "\"packagename\":\"$matchdata.packagename\"},\"vins\":{\"$addToSet\":\"$matchdata.vin\"}}}," +
                    "{\"$unwind\":\"$vins\"},{\"$group\":{\"_id\":\"$_id\",\"vinCount\":{\"$sum\":1}}}]}}";
            AggregationOperation facet = new JsonAggregationOperation(strFacet);

            Aggregation agg =
                    Aggregation.newAggregation(match, group, lookup, unwind, facet).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());
            log.info("agg NOSQL ::: {}", agg);
            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

            log.info("======================================");
            log.info("HMap Result ::: " + aggregate.getMappedResults());
            log.info("======================================");
        }
    }
