package com.example.mongo.service.impl;

import com.example.mongo.service.EvntVersionUpdateStatusService;
import com.example.mongo.util.HMap;
import com.example.mongo.vo.EvntVersionUpdateStatusVO;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.stream.IntStream;

/**
 * <pre>
 * Statements
 * </pre>
 *
 * @author Windows 占쏙옙占쏙옙占?
 * @version 1.0
 * @ClassName : EvntVersionUpdateStatusVO.java
 * @Description : 클래스 설명을 기술합니다.
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2022. 3. 7.     Windows 占쏙옙占쏙옙占?     	최초 생성partNoUpdateStateNew
 * </pre>
 * @see
 * @since 2022. 3. 7.
 */
@Service("evntVersionUpdateStatusService")
public class EvntVersionUpdateStatusServiceImpl implements EvntVersionUpdateStatusService {
    private static final Logger logger = LoggerFactory.getLogger(EvntVersionUpdateStatusServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private List<Criteria> setBasicRoleCriteria(EvntVersionUpdateStatusVO vo, String key) {
        List<Criteria> criteriaList = new ArrayList<Criteria>();

        if (vo.getStartDate() != null && !vo.getStartDate().isEmpty()
                && vo.getEndDate() != null && !vo.getEndDate().isEmpty()) {
            String startDate = vo.getStartDate() + "00";
            String endDate = vo.getEndDate() + "59";
            criteriaList.add(Criteria.where(key).gte(startDate).lte(endDate));
        }

        return criteriaList;
    }

    @Override
    public List<Map<String, String>> pckgNmUpdateStateNew(EvntVersionUpdateStatusVO vo) throws Exception {
        logger.debug(">>>>>>><<<<<<<< pckgNmUpdateStateNew EvntVersionUpdateStatusDAO.getInstance before getSelectRegion[" + vo.getSelectRegion() + "]");

        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = setBasicRoleCriteria(vo, "requesttime");
        logger.info("############### pckgNmUpdateStateNew2 Test 2");
        // package명 으로 검색
        if (vo.getSelectPckgNm() != null && !vo.getSelectPckgNm().isEmpty()) {
            String packagename = vo.getSelectPckgNm();
            criteriaList.add(Criteria.where("packagename").in(packagename));
        } else {
            if (!"A".equals(vo.getAuthGrpCd())) {
                if (vo.getPackagenameArr() != null && vo.getPackagenameArr().length > 0) {
                    criteriaList.add(Criteria.where("packagename").in(vo.getPackagenameArr()));
                } else {
                    criteriaList.add(Criteria.where("packagename").regex(""));
                }
            } else {
                criteriaList.add(Criteria.where("packagename").regex(""));
            }
        }

        if (vo.getEventVer() != null && !vo.getEventVer().isEmpty()) {
            // 이벤트 버전명 / 이벤트 버전은 따로 VO를 만들지 않고 가공해서 쿼리에 사용하는것으로 변경 (22.05.17 천영락)
            String[] evntVerArr = vo.getEventVer().split("/");
            // 현재는 이벤트 버전명이 없는 값들이 많이 있기 때문에 예외처리로 해놓음. (나중에는 정상적일때는 정리 필요)
            if (evntVerArr[1] == null || evntVerArr[1].isEmpty()) {
                String evntVer = evntVerArr[0];

                if (evntVer != null && !evntVer.isEmpty()) {
                    criteriaList.add(Criteria.where("targetEventID").in(evntVer));
                    // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
                } else {
                    criteriaList.add(Criteria.where("targetEventID").regex(""));
                }
                criteriaList.add(Criteria.where("targetEventName").regex(""));
            } else {
                String evntVer = evntVerArr[0];
                String evntVerNm = evntVerArr[1];
                // 이벤트 버전
                if (evntVer != null && !evntVer.isEmpty()) {
                    criteriaList.add(Criteria.where("targetEventID").in(evntVer));
                    // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
                } else {
                    criteriaList.add(Criteria.where("targetEventID").regex(""));
                }

                if (evntVerNm != null && !evntVerNm.isEmpty()) {
                    criteriaList.add(Criteria.where("targetEventName").in(HtmlUtils.htmlUnescape(evntVerNm)));
                    // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
                } else {
                    criteriaList.add(Criteria.where("targetEventName").regex(""));
                }
            }
        }

        // 이벤트 타입 명으로 검색
        if (vo.getEvntType() != null && !vo.getEvntType().isEmpty()) {
            String evntType = vo.getEvntType();
            criteriaList.add(Criteria.where("testDv").in(evntType));
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaList.add(Criteria.where("testDv").regex(""));
        }

        criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        logger.info("############### pckgNmUpdateStateNew2 Test 3");
        // match
        AggregationOperation match = Aggregation.match(criteria);
        logger.info("############### pckgNmUpdateStateNew2 Test 4");
        // group
        AggregationOperation group = Aggregation.group(Fields.fields().and("vin").and("partNo")).max("createdAt").as("maxCreatedAt");
        logger.info("############### pckgNmUpdateStateNew2 Test 5");
        // lookup
        String expression = "\"$expr\" : { \"$and\" : [{ \"$eq\" : [\"$vin\", \"$$vin\"]}, { \"$eq\" : [\"$partNo\", \"$$partNo\"]}, { \"$eq\" : [\"$createdAt\", \"$$createdAt\"]}]}";
        LookupOperation lookup = Aggregation.lookup().from("StatDeviceDownload").let(
                        VariableOperators.Let.ExpressionVariable.newVariable("vin").forField("$_id.vin"),
                        VariableOperators.Let.ExpressionVariable.newVariable("partNo").forField("$_id.partNo"),
                        VariableOperators.Let.ExpressionVariable.newVariable("createdAt").forField("$maxCreatedAt"))
                .pipeline(Aggregation.match(AggregationExpression.from(MongoExpression.create(expression))))
                .as("matchdata");
        UnwindOperation unwind = Aggregation.unwind("$matchdata");
        // facet
        FacetOperation facet = Aggregation.facet(Aggregation.match(Criteria.where("matchdata.downloadresult").in(List.of("0"))),
                        Aggregation.group("matchdata.packagename", "matchdata.downloadresult").count().as("downloadresultcount"),
                        Aggregation.group(Fields.fields().and("packagename", "$_id.packagename")).push(new BasicDBObject("downloadresult", "$_id.downloadresult")
                                        .append("count", "$downloadresultcount")).as("downloadresults")
                                .sum("$downloadresultcount").as("count")
                ).as("download_ing_data")
                .and(Aggregation.match(new Criteria().andOperator(List.of(
                                Criteria.where("matchdata.reportCode").in(List.of("1002")),
                                Criteria.where("matchdata.isresultCode").is("")).toArray(new Criteria[0]))),
                        Aggregation.group("matchdata.packagename", "matchdata.reportCode").count().as("reportCodecount"),
                        Aggregation.group(Fields.from(Fields.field("packagename", "$_id.packagename"))).push(new BasicDBObject("reportCode", "$_id.reportCode")
                                        .append("count", "$reportCodecount")).as("reportCodes")
                                .sum("$reportCodecount").as("count")).as("update_ing_data")
                .and(Aggregation.group("matchdata.packagename", "matchdata.srresultCode").count().as("srresultCodecount"),
                        Aggregation.sort(Sort.Direction.ASC, "_id.srresultCode"),
                        Aggregation.group(Fields.from(Fields.field("packagename", "$_id.packagename"))).push(new BasicDBObject("srresultCode", "$_id.srresultCode")
                                        .append("count", "$srresultCodecount")).as("srresultCodes")
                                .sum("$srresultCodecount").as("count")).as("download_data")
                .and(Aggregation.group("matchdata.packagename", "matchdata.isresultCode").count().as("isresultCodecount"),
                        Aggregation.sort(Sort.Direction.ASC, "_id.isresultCode"),
                        Aggregation.group(Fields.from(Fields.field("packagename", "$_id.packagename"))).push(new BasicDBObject("isresultCode", "$_id.isresultCode")
                                        .append("count", "$isresultCodecount")).as("isresultCodes")
                                .sum("$isresultCodecount").as("count")).as("update_data")
                .and(Aggregation.group("matchdata.packagename").addToSet("$matchdata.partNo").as("partNos"),
                        Aggregation.unwind("$partNos"),
                        Aggregation.group("$_id")
                                .count().as("partNoCount")).as("part_count");
        Aggregation agg = Aggregation.newAggregation(match, group, lookup, unwind, facet).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

        AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

        logger.info("======================================");
        logger.info("HMap Result ::: " + aggregate.getMappedResults());
        logger.info("======================================");

        List<HMap> pckgNmUpdateStateList = aggregate.getMappedResults();

        return parsePckgNmUpdateStateListNew2(pckgNmUpdateStateList);
    }

    /*
     * @see ota.statistics.service.EvntVersionUpdateStatusService#partNoUpdateStateNew(ota.statistics.vo.EvntVersionUpdateStatusVO)
     */
    @Override
    public List<Map<String, String>> partNoUpdateStateNew(EvntVersionUpdateStatusVO vo) throws Exception {
        settingDownloadAndUpdateFailCode(vo);
//        logger.info("vo: {}", Arrays.toString(vo.getUpdateFailCodeArr()));

        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = setBasicRoleCriteria(vo, "requesttime");

        // package명 으로 검색
        if (vo.getSelectPckgNm() != null && !vo.getSelectPckgNm().isEmpty()) {
            String packagename = vo.getSelectPckgNm();
            criteriaList.add(Criteria.where("packagename").in(packagename));
        } else {
            if (!"A".equals(vo.getAuthGrpCd())) {
                if (vo.getPackagenameArr() != null && vo.getPackagenameArr().length > 0) {
                    criteriaList.add(Criteria.where("packagename").in(vo.getPackagenameArr()));
                } else {
                    criteriaList.add(Criteria.where("packagename").regex(""));
                }
            } else {
                criteriaList.add(Criteria.where("packagename").regex(""));
            }
        }

        if (vo.getEventVer() != null && !vo.getEventVer().isEmpty()) {
            // 이벤트 버전명 / 이벤트 버전은 따로 VO를 만들지 않고 가공해서 쿼리에 사용하는것으로 변경 (22.05.17 천영락)
            String[] evntVerArr = vo.getEventVer().split("/");
            // 현재는 이벤트 버전명이 없는 값들이 많이 있기 때문에 예외처리로 해놓음. (나중에는 정상적일때는 정리 필요)
            if (evntVerArr[1] == null || evntVerArr[1].isEmpty()) {
                String evntVer = evntVerArr[0];

                if (evntVer != null && !evntVer.isEmpty()) {
                    criteriaList.add(Criteria.where("targetEventID").in(evntVer));
                    // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
                } else {
                    criteriaList.add(Criteria.where("targerEventID").regex(""));
                }
                criteriaList.add(Criteria.where("targetEventName").regex(""));
            } else {
                String evntVer = evntVerArr[0];
                String evntVerNm = evntVerArr[1];

                // 이벤트 버전
                if (evntVer != null && !evntVer.isEmpty()) {
                    criteriaList.add(Criteria.where("targetEventID").in(evntVer));
                    // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
                } else {
                    criteriaList.add(Criteria.where("targetEventID").regex(""));
                }

                if (evntVerNm != null && !evntVerNm.isEmpty()) {

                    criteriaList.add(Criteria.where("targetEventName").in(HtmlUtils.htmlUnescape(evntVerNm)));
                    // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
                } else {
                    criteriaList.add(Criteria.where("targetEventName").regex(""));
                }
            }
        }

        // 이벤트 타입 명으로 검색
        if (vo.getEvntType() != null && !vo.getEvntType().isEmpty()) {
            String evntType = vo.getEvntType();
            criteriaList.add(Criteria.where("testDv").in(evntType));
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaList.add(Criteria.where("testDv").regex(""));
        }

        criteria.andOperator(criteriaList.toArray(new Criteria[0]));

        // match
        AggregationOperation match = Aggregation.match(criteria);
        // group
        AggregationOperation group = Aggregation.group("vin", "partNo").max("createdAt").as("maxCreatedAt");
        // lookup
        String expression = "\"$expr\" : { \"$and\" : [{ \"$eq\" : [\"$vin\", \"$$vin\"]}, { \"$eq\" : [\"$partNo\", \"$$partNo\"]}, { \"$eq\" : [\"$createdAt\", \"$$createdAt\"]}]}";
        EvaluationOperators.Expr expr = EvaluationOperators.Expr.valueOf(BooleanOperators.And.and(
                ComparisonOperators.valueOf("$vin").equalTo("$$vin"),
                ComparisonOperators.valueOf("$partNo").equalTo("$$partNo"),
                ComparisonOperators.valueOf("$createdAt").equalTo("$$createdAt")));
        LookupOperation lookup = Aggregation.lookup().from("StatDeviceDownload").let(
                        VariableOperators.Let.ExpressionVariable.newVariable("vin").forField("$_id.vin"),
                        VariableOperators.Let.ExpressionVariable.newVariable("partNo").forField("$_id.partNo"),
                        VariableOperators.Let.ExpressionVariable.newVariable("createdAt").forField("$maxCreatedAt"))
//                .pipeline(Aggregation.match(AggregationExpression.from(MongoExpression.create(expression))))
                .pipeline(Aggregation.match(expr))
                .as("matchdata");
        UnwindOperation unwind = Aggregation.unwind("$matchdata");
        // facet
        FacetOperation facet = Aggregation.facet(Aggregation.match(Criteria.where("matchdata.downloadresult").in(List.of("0"))),
                        Aggregation.group("matchdata.partNo", "matchdata.targetEventID", "matchdata.downloadresult").count().as("downloadresultcount"),
                        Aggregation.group("$_id.partNo", "$_id.targetEventID").push(new BasicDBObject("downloadresult", "$_id.downloadresult")
                                        .append("count", "$downloadresultcount")).as("downloadresults")
                                .sum("downloadresultcount").as("count")).as("download_ing_data")
                .and(Aggregation.match(new Criteria().andOperator(List.of(
                                Criteria.where("matchdata.reportCode").in(List.of("1002")),
                                Criteria.where("matchdata.isresultCode").is("")).toArray(new Criteria[0]))),
                        Aggregation.group("matchdata.partNo", "matchdata.targetEventID", "matchdata.reportCode").count().as("reportCodecount"),
                        Aggregation.group("$_id.partNo", "$_id.targetEventID").push(new BasicDBObject("reportCode", "$_id.reportCode")
                                        .append("count", "$reportCodecount")).as("reportCodes")
                                .sum("reportCodecount").as("count")).as("update_ing_data")
                .and(Aggregation.group("matchdata.partNo", "matchdata.targetEventID", "matchdata.srresultCode").count().as("srresultCodecount"),
                        Aggregation.group("$_id.partNo", "$_id.targetEventID").push(new BasicDBObject("srresultCode", "$_id.srresultCode")
                                        .append("count", "$srresultCodecount")).as("srresultCodes")
                                .sum("srresultCodecount").as("count")).as("download_data")
                .and(Aggregation.group("matchdata.partNo", "matchdata.targetEventID", "matchdata.isresultCode").count().as("isresultCodecount"),
                        Aggregation.group("$_id.partNo", "$_id.targetEventID").push(new BasicDBObject("isresultCode", "$_id.isresultCode")
                                        .append("count", "$isresultCodecount")).as("isresultCodes")
                                .sum("isresultCodecount").as("count")).as("update_data")
                .and(Aggregation.group("matchdata.partNo", "matchdata.targetEventID").addToSet("matchdata.vin").as("vins"),
                        Aggregation.unwind("$vins"),
                        Aggregation.group("$_id").count().as("carCount")).as("vin_count")
                .and(Aggregation.match(new Criteria().andOperator(List.of(
                                Criteria.where("matchdata.srresultCode").in(Arrays.asList(vo.getDownloadFailCodeArr()))).toArray(new Criteria[0]))),
                        Aggregation.group("matchdata.partNo", "matchdata.targetEventID", "matchdata.sourceEventID", "matchdata.srresultCode", "matchdata.vin"),
                        Aggregation.group("$_id.partNo", "$_id.targetEventID").push(new BasicDBObject("sourceEventID", "$_id.sourceEventID")
                                .append("srresultCode", "$_id.srresultCode").append("vin", "$_id.vin")).as("downloadFailDetail"))
                .as("download_fail_data")
                .and(Aggregation.match(new Criteria().andOperator(List.of(
                                Criteria.where("matchdata.isresultCode").in(Arrays.asList(vo.getUpdateFailCodeArr()))).toArray(new Criteria[0]))),
                        Aggregation.group("matchdata.partNo", "matchdata.targetEventID", "matchdata.sourceEventID", "matchdata.isresultCode", "matchdata.vin"),
                        Aggregation.group("$_id.partNo", "$_id.targetEventID").push(new BasicDBObject("sourceEventID", "$_id.sourceEventID")
                                .append("isresultCode", "$_id.isresultCode").append("vin", "$_id.vin")).as("updateFailDetail"))
                .as("update_fail_data");

        try {

            Aggregation agg = Aggregation.newAggregation(match, group, lookup, unwind, facet).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

            logger.info("**************************************");
            logger.info("agg NOSQL ::: " + agg.toString());
            logger.info("**************************************");

            AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

            logger.info("**************************************");
            logger.info("HMap Result ::: " + aggregate.getMappedResults());
            logger.info("**************************************");

            List<HMap> partNoUpdateStateNList = aggregate.getMappedResults();
            //Map<String, Object> downloadFailDataMap = vo.get
            Map<String, Object> updateFailDataMap = new HashMap<String, Object>();

            for (HMap temp : partNoUpdateStateNList) {
                if (temp.containsKey("downloadFailData")) {
                    List<Map<String, Object>> tempDownloadFailData = (List<Map<String, Object>>) temp.get("downloadFailData");

                    for (Map<String, Object> temp1 : tempDownloadFailData) {
                        String partNo = "";
                        String targetEventID = "";
                        List<Map<String, Object>> downloadFailDetail = new ArrayList<>();

                        if (temp1.containsKey("_id")) {
                            Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                            if (_id.containsKey("partNo")) {
                                partNo = (String) _id.get("partNo");
                            }

                            if (_id.containsKey("targetEventID")) {
                                targetEventID = (String) _id.get("targetEventID");
                            }
                        }

                        if (temp1.containsKey("downloadFailDetail")) {
                            downloadFailDetail = (List<Map<String, Object>>) temp1.get("downloadFailDetail");

                        }

                        String key = partNo + "^" + targetEventID;
//                        if (!downloadFailDataMap.containsKey(key)) {
//                            downloadFailDataMap.put(key, downloadFailDetail);
//                        }
                    }
                }

                if (temp.containsKey("updateFailData")) {
                    List<Map<String, Object>> tempUpdateFailData = (List<Map<String, Object>>) temp.get("updateFailData");

                    for (Map<String, Object> temp1 : tempUpdateFailData) {
                        String partNo = "";
                        String targetEventID = "";
                        List<Map<String, Object>> updateFailDetail = new ArrayList<>();

                        if (temp1.containsKey("_id")) {
                            Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                            if (_id.containsKey("partNo")) {
                                partNo = (String) _id.get("partNo");
                            }

                            if (_id.containsKey("targetEventID")) {
                                targetEventID = (String) _id.get("targetEventID");
                            }
                        }

                        if (temp1.containsKey("updateFailDetail")) {
                            updateFailDetail = (List<Map<String, Object>>) temp1.get("updateFailDetail");

                        }

                        String key = partNo + "^" + targetEventID;
                        if (!updateFailDataMap.containsKey(key)) {
                            updateFailDataMap.put(key, updateFailDetail);
                        }
                    }
                }
            }

            return parsePartNoUpdateStateListNew2(partNoUpdateStateNList);
        } catch (Exception ex) {
            logger.error("agg err:", ex);
        }

        return Collections.emptyList();
    }

    private List<Map<String, String>> parsePckgNmUpdateStateList(List<HMap> hMapList) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        for (HMap temp : hMapList) {
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

            if (temp.containsKey("downloadData")) {
                List<Map<String, Object>> tempDownloadData = (List<Map<String, Object>>) temp.get("downloadData");

                for (Map<String, Object> temp1 : tempDownloadData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("downloadresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("downloadresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("downloadresult")) {
                                String downloadresult = (String) temp2.get("downloadresult");

                                switch (downloadresult) {
                                    case "0":
                                        downloadProgress = (int) temp2.get("count");
                                        break;
                                    case "1":
                                        downloadSuccess = (int) temp2.get("count");
                                        break;
                                    case "2":
                                        downloadFail = (int) temp2.get("count");
                                        break;
                                    case "3":
                                        downloadEtc = (int) temp2.get("count");
                                        break;
                                }
                            }
                        }
                    }

                    if (temp1.containsKey("count")) {
//                        int total = (int) temp1.get("count");
                        int total = downloadProgress + downloadSuccess + downloadFail + downloadEtc;
                        // 2022.08.08 Add by SungJin Seo
                        // SonarQube 취약점 수정으로 if문 추가
                        if (total > 0) {
                            downloadFailRate = Math.round(((double) downloadFail / (double) total) * 1000) / (double) 10;
                        }
                    }
                }
            }

            if (temp.containsKey("updateData")) {
                List<Map<String, Object>> tempUpdateData = (List<Map<String, Object>>) temp.get("updateData");

                for (Map<String, Object> temp1 : tempUpdateData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("updateresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("updateresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("updateresult")) {
                                String updateresult = (String) temp2.get("updateresult");

                                switch (updateresult) {
                                    case "0":
                                        updateProgress = (int) temp2.get("count");
                                        break;
                                    case "1":
                                        updateSuccess = (int) temp2.get("count");
                                        break;
                                    case "2":
                                        updateFail = (int) temp2.get("count");
                                        break;
                                    case "3":
                                        updateEtc = (int) temp2.get("count");
                                        break;
                                }
                            }
                        }
                    }

                    if (temp1.containsKey("count")) {
//                        int total = (int) temp1.get("count");
                        int total = updateProgress + updateSuccess + updateFail + updateEtc;
                        // 2022.08.08 Add by SungJin Seo
                        // SonarQube 취약점 수정으로 if문 추가
                        if (total > 0) {
                            updateFailRate = Math.round(((double) updateFail / (double) total) * 1000) / (double) 10;
                        }
                    }
                }
            }

            if (temp.containsKey("partCount")) {
                List<Map<String, Object>> tempPartCount = (List<Map<String, Object>>) temp.get("partCount");

                for (Map<String, Object> temp1 : tempPartCount) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("partNoCount")) {
                        totalPartCnt = (int) temp1.get("partNoCount");
                    }
                }
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

    private List<Map<String, String>> parsePckgNmUpdateStateListNew(List<HMap> hMapList) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        for (HMap temp : hMapList) {
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

            if (temp.containsKey("downloadIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("downloadIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("downloadresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("downloadresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
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

            if (temp.containsKey("updateIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("updateIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("reportCodes")) {
                        List<Map<String, Object>> tempReportCodes = (List<Map<String, Object>>) temp1.get("reportCodes");

                        for (Map<String, Object> temp2 : tempReportCodes) {
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

            if (temp.containsKey("downloadData")) {
                List<Map<String, Object>> tempDownloadData = (List<Map<String, Object>>) temp.get("downloadData");

                for (Map<String, Object> temp1 : tempDownloadData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("srresultCodes")) {
                        List<Map<String, Object>> tempSrResultCodes = (List<Map<String, Object>>) temp1.get("srresultCodes");

                        for (Map<String, Object> temp2 : tempSrResultCodes) {
                            if (temp2.containsKey("srresultCode")) {
                                String srresultCode = (String) temp2.get("srresultCode");

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

            if (temp.containsKey("updateData")) {
                List<Map<String, Object>> tempUpdateData = (List<Map<String, Object>>) temp.get("updateData");

                for (Map<String, Object> temp1 : tempUpdateData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("isresultCodes")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("isresultCodes");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("isresultCode")) {
                                String isresultCode = (String) temp2.get("isresultCode");

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

            if (temp.containsKey("partCount")) {
                List<Map<String, Object>> tempPartCount = (List<Map<String, Object>>) temp.get("partCount");

                for (Map<String, Object> temp1 : tempPartCount) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
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

    private List<Map<String, String>> parsePckgNmUpdateStateListNew2(List<HMap> hMapList) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        for (HMap temp : hMapList) {
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

            if (temp.containsKey("downloadIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("downloadIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("downloadresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("downloadresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
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

            if (temp.containsKey("updateIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("updateIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("reportCodes")) {
                        List<Map<String, Object>> tempReportCodes = (List<Map<String, Object>>) temp1.get("reportCodes");

                        for (Map<String, Object> temp2 : tempReportCodes) {
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

            if (temp.containsKey("downloadData")) {
                List<Map<String, Object>> tempDownloadData = (List<Map<String, Object>>) temp.get("downloadData");

                for (Map<String, Object> temp1 : tempDownloadData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("srresultCodes")) {
                        List<Map<String, Object>> tempSrResultCodes = (List<Map<String, Object>>) temp1.get("srresultCodes");

                        for (Map<String, Object> temp2 : tempSrResultCodes) {
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

            if (temp.containsKey("updateData")) {
                List<Map<String, Object>> tempUpdateData = (List<Map<String, Object>>) temp.get("updateData");

                for (Map<String, Object> temp1 : tempUpdateData) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
                    }

                    if (temp1.containsKey("isresultCodes")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("isresultCodes");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
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

            if (temp.containsKey("partCount")) {
                List<Map<String, Object>> tempPartCount = (List<Map<String, Object>>) temp.get("partCount");

                for (Map<String, Object> temp1 : tempPartCount) {
                    if (pckgNm.isEmpty() && temp1.containsKey("_id")) {
                        pckgNm = (String) temp1.get("_id");
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

    private List<Map<String, String>> parsePartNoUpdateStateList(List<HMap> hMapList) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        Map<String, Map<String, Object>> tempDownloadMap = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> tempUpdateMap = new HashMap<String, Map<String, Object>>();
        Map<String, Integer> tempVinMap = new HashMap<String, Integer>();

        for (HMap temp : hMapList) {
            if (temp.containsKey("downloadData")) {
                List<Map<String, Object>> tempDownloadData = (List<Map<String, Object>>) temp.get("downloadData");

                for (Map<String, Object> temp1 : tempDownloadData) {
                    String partNo = "";
                    String targetEventID = "";

                    int downloadProgress = 0;
                    int downloadSuccess = 0;
                    int downloadFail = 0;
                    int downloadEtc = 0;
                    double downloadFailRate = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("downloadresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("downloadresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("downloadresult")) {
                                String downloadresult = (String) temp2.get("downloadresult");

                                switch (downloadresult) {
                                    case "0":
                                        downloadProgress = (int) temp2.get("count");
                                        break;
                                    case "1":
                                        downloadSuccess = (int) temp2.get("count");
                                        break;
                                    case "2":
                                        downloadFail = (int) temp2.get("count");
                                        break;
                                    case "3":
                                        downloadEtc = (int) temp2.get("count");
                                        break;
                                }
                            }
                        }
                    }

                    if (temp1.containsKey("count")) {
                        int total = (int) temp1.get("count");
                        // 2022.08.08 Add by SungJin Seo
                        // SonarQube 취약점 수정으로 if문 추가
                        if (total > 0) {
                            downloadFailRate = Math.round(((double) downloadFail / (double) total) * 1000) / (double) 10;
                        }
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempDownloadMap.containsKey(key)) {
                        Map<String, Object> tempValue = new HashMap<String, Object>();

                        tempValue.put("downloadProgress", downloadProgress);
                        tempValue.put("downloadSuccess", downloadSuccess);
                        tempValue.put("downloadFail", downloadFail);
                        tempValue.put("downloadEtc", downloadEtc);
                        tempValue.put("downloadFailRate", downloadFailRate);

                        tempDownloadMap.put(key, tempValue);
                    }
                }
            }

            if (temp.containsKey("updateData")) {
                List<Map<String, Object>> tempUpdateData = (List<Map<String, Object>>) temp.get("updateData");

                for (Map<String, Object> temp1 : tempUpdateData) {
                    String partNo = "";
                    String targetEventID = "";

                    int updateProgress = 0;
                    int updateSuccess = 0;
                    int updateFail = 0;
                    int updateEtc = 0;
                    double updateFailRate = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("updateresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("updateresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("updateresult")) {
                                String updateresult = (String) temp2.get("updateresult");

                                switch (updateresult) {
                                    case "0":
                                        updateProgress = (int) temp2.get("count");
                                        break;
                                    case "1":
                                        updateSuccess = (int) temp2.get("count");
                                        break;
                                    case "2":
                                        updateFail = (int) temp2.get("count");
                                        break;
                                    case "3":
                                        updateEtc = (int) temp2.get("count");
                                        break;
                                }
                            }
                        }
                    }

                    if (temp1.containsKey("count")) {
                        int total = (int) temp1.get("count");
                        // 2022.08.08 Add by SungJin Seo
                        // SonarQube 취약점 수정으로 if문 추가
                        if (total > 0) {
                            updateFailRate = Math.round(((double) updateFail / (double) total) * 1000) / (double) 10;
                        }
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempUpdateMap.containsKey(key)) {
                        Map<String, Object> tempValue = new HashMap<String, Object>();

                        tempValue.put("updateProgress", updateProgress);
                        tempValue.put("updateSuccess", updateSuccess);
                        tempValue.put("updateFail", updateFail);
                        tempValue.put("updateEtc", updateEtc);
                        tempValue.put("updateFailRate", updateFailRate);

                        tempUpdateMap.put(key, tempValue);
                    }
                }
            }

            if (temp.containsKey("vinCount")) {
                List<Map<String, Object>> tempVinCount = (List<Map<String, Object>>) temp.get("vinCount");

                for (Map<String, Object> temp1 : tempVinCount) {
                    String partNo = "";
                    String targetEventID = "";

                    int totalVinCnt = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("carCount")) {
                        totalVinCnt = (int) temp1.get("carCount");
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempVinMap.containsKey(key)) {
                        tempVinMap.put(key, totalVinCnt);
                    }
                }
            }

            Object[] mapkey = tempDownloadMap.keySet().toArray();
            Arrays.sort(mapkey);

            for (Object key : mapkey) {
                Map<String, String> resultMap = new HashMap<String, String>();

                String[] keySplit = key.toString().split("\\^");

                if (keySplit.length == 2) {
                    resultMap.put("partNo", keySplit[0]);
                    resultMap.put("targetEventID", keySplit[1]);
                } else if (keySplit.length == 1) {
                    resultMap.put("partNo", keySplit[0]);
                }

                if (tempVinMap.containsKey(key)) {
                    resultMap.put("totalCarCnt", String.valueOf(tempVinMap.get(key)));
                } else {
                    resultMap.put("totalCarCnt", "-");
                }

                Map<String, Object> tempDownloadMapValue = tempDownloadMap.get(key);

                resultMap.put("downloadProgress", String.valueOf(tempDownloadMapValue.get("downloadProgress")));
                resultMap.put("downloadSuccess", String.valueOf(tempDownloadMapValue.get("downloadSuccess")));
                resultMap.put("downloadFail", String.valueOf(tempDownloadMapValue.get("downloadFail")));
                resultMap.put("downloadEtc", String.valueOf(tempDownloadMapValue.get("downloadEtc")));
                resultMap.put("downloadFailRate", String.valueOf(tempDownloadMapValue.get("downloadFailRate")));

                if (tempUpdateMap.containsKey(key)) {
                    Map<String, Object> tempUpdateMapValue = tempUpdateMap.get(key);

                    resultMap.put("updateProgress", String.valueOf(tempUpdateMapValue.get("updateProgress")));
                    resultMap.put("updateSuccess", String.valueOf(tempUpdateMapValue.get("updateSuccess")));
                    resultMap.put("updateFail", String.valueOf(tempUpdateMapValue.get("updateFail")));
                    resultMap.put("updateEtc", String.valueOf(tempUpdateMapValue.get("updateEtc")));
                    resultMap.put("updateFailRate", String.valueOf(tempUpdateMapValue.get("updateFailRate")));
                } else {
                    resultMap.put("updateProgress", "-");
                    resultMap.put("updateSuccess", "-");
                    resultMap.put("updateFail", "-");
                    resultMap.put("updateEtc", "-");
                    resultMap.put("updateFailRate", "-");
                }

                result.add(resultMap);
            }
        }

        return result;
    }

    private List<Map<String, String>> parsePartNoUpdateStateListNew(List<HMap> hMapList) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        Map<String, Map<String, Object>> tempDownloadMap = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> tempUpdateMap = new HashMap<String, Map<String, Object>>();
        Map<String, Integer> tempVinMap = new HashMap<String, Integer>();
        Map<String, Integer> tempKeyAndDownloadProgressMap = new HashMap<String, Integer>();
        Map<String, Integer> tempKeyAndUpdateProgressMap = new HashMap<String, Integer>();

        for (HMap temp : hMapList) {
            if (temp.containsKey("downloadIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("downloadIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    String partNo = "";
                    String targetEventID = "";

                    int downloadProgress = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("downloadresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("downloadresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("downloadresult")) {
                                String downloadresult = (String) temp2.get("downloadresult");

                                if ("0".equals(downloadresult)) {
                                    downloadProgress = (int) temp2.get("count");
                                }
                            }
                        }
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempKeyAndDownloadProgressMap.containsKey(key)) {
                        tempKeyAndDownloadProgressMap.put(key, downloadProgress);
                    }
                }
            }

            if (temp.containsKey("updateIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("updateIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    String partNo = "";
                    String targetEventID = "";

                    int updateProgress = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("reportCodes")) {
                        List<Map<String, Object>> tempReportCodes = (List<Map<String, Object>>) temp1.get("reportCodes");

                        for (Map<String, Object> temp2 : tempReportCodes) {
                            if (temp2.containsKey("reportCode")) {
                                String reportCode = (String) temp2.get("reportCode");

                                if ("1002".equals(reportCode)) {
                                    updateProgress = (int) temp2.get("count");
                                }
                            }
                        }
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempKeyAndUpdateProgressMap.containsKey(key)) {
                        tempKeyAndUpdateProgressMap.put(key, updateProgress);
                    }
                }
            }

            if (temp.containsKey("downloadData")) {
                List<Map<String, Object>> tempDownloadData = (List<Map<String, Object>>) temp.get("downloadData");

                for (Map<String, Object> temp1 : tempDownloadData) {
                    String partNo = "";
                    String targetEventID = "";

                    int downloadSuccess = 0;
                    int downloadFail = 0;
                    int downloadEtc = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("srresultCodes")) {
                        List<Map<String, Object>> tempSrResultCodes = (List<Map<String, Object>>) temp1.get("srresultCodes");

                        for (Map<String, Object> temp2 : tempSrResultCodes) {
                            if (temp2.containsKey("srresultCode")) {
                                String srresultCode = (String) temp2.get("srresultCode");

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

                    String key = partNo + "^" + targetEventID;
                    if (!tempDownloadMap.containsKey(key)) {
                        Map<String, Object> tempValue = new HashMap<String, Object>();

                        int downloadProgress = 0;
                        if (tempKeyAndDownloadProgressMap.containsKey(key)) {
                            downloadProgress = tempKeyAndDownloadProgressMap.get(key);
                        }
                        tempValue.put("downloadProgress", downloadProgress);
                        tempValue.put("downloadSuccess", downloadSuccess);
                        tempValue.put("downloadFail", downloadFail);
                        tempValue.put("downloadEtc", downloadEtc);
                        int total = downloadProgress + downloadSuccess + downloadFail + downloadEtc;
                        double downloadFailRate = 0;
                        if (total > 0) {
                            downloadFailRate = Math.round(((double) downloadFail / (double) total) * 1000) / (double) 10;
                        }
                        tempValue.put("downloadFailRate", downloadFailRate);

                        tempDownloadMap.put(key, tempValue);
                    }
                }
            }

            if (temp.containsKey("updateData")) {
                List<Map<String, Object>> tempUpdateData = (List<Map<String, Object>>) temp.get("updateData");

                for (Map<String, Object> temp1 : tempUpdateData) {
                    String partNo = "";
                    String targetEventID = "";

                    int updateSuccess = 0;
                    int updateFail = 0;
                    int updateEtc = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("isresultCodes")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("isresultCodes");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("isresultCode")) {
                                String isresultCode = (String) temp2.get("isresultCode");

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

                    String key = partNo + "^" + targetEventID;
                    if (!tempUpdateMap.containsKey(key)) {
                        Map<String, Object> tempValue = new HashMap<String, Object>();

                        int updateProgress = 0;
                        if (tempKeyAndUpdateProgressMap.containsKey(key)) {
                            updateProgress = tempKeyAndUpdateProgressMap.get(key);
                        }
                        tempValue.put("updateProgress", updateProgress);
                        tempValue.put("updateSuccess", updateSuccess);
                        tempValue.put("updateFail", updateFail);
                        tempValue.put("updateEtc", updateEtc);
                        int total = updateProgress + updateSuccess + updateFail + updateEtc;
                        double updateFailRate = 0;
                        if (total > 0) {
                            updateFailRate = Math.round(((double) updateFail / (double) total) * 1000) / (double) 10;
                        }
                        tempValue.put("updateFailRate", updateFailRate);

                        tempUpdateMap.put(key, tempValue);
                    }
                }
            }

            if (temp.containsKey("vinCount")) {
                List<Map<String, Object>> tempVinCount = (List<Map<String, Object>>) temp.get("vinCount");

                for (Map<String, Object> temp1 : tempVinCount) {
                    String partNo = "";
                    String targetEventID = "";

                    int totalVinCnt = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("carCount")) {
                        totalVinCnt = (int) temp1.get("carCount");
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempVinMap.containsKey(key)) {
                        tempVinMap.put(key, totalVinCnt);
                    }
                }
            }

            Object[] mapkey = tempDownloadMap.keySet().toArray();
            Arrays.sort(mapkey);

            for (Object key : mapkey) {
                Map<String, String> resultMap = new HashMap<String, String>();

                String[] keySplit = key.toString().split("\\^");

                if (keySplit.length == 2) {
                    resultMap.put("partNo", keySplit[0]);
                    resultMap.put("targetEventID", keySplit[1]);
                } else if (keySplit.length == 1) {
                    resultMap.put("partNo", keySplit[0]);
                }

                if (tempVinMap.containsKey(key)) {
                    resultMap.put("totalCarCnt", String.valueOf(tempVinMap.get(key)));
                } else {
                    resultMap.put("totalCarCnt", "-");
                }

                Map<String, Object> tempDownloadMapValue = tempDownloadMap.get(key);

                resultMap.put("downloadProgress", String.valueOf(tempDownloadMapValue.get("downloadProgress")));
                resultMap.put("downloadSuccess", String.valueOf(tempDownloadMapValue.get("downloadSuccess")));
                resultMap.put("downloadFail", String.valueOf(tempDownloadMapValue.get("downloadFail")));
                resultMap.put("downloadEtc", String.valueOf(tempDownloadMapValue.get("downloadEtc")));
                resultMap.put("downloadFailRate", String.valueOf(tempDownloadMapValue.get("downloadFailRate")));

                if (tempUpdateMap.containsKey(key)) {
                    Map<String, Object> tempUpdateMapValue = tempUpdateMap.get(key);

                    resultMap.put("updateProgress", String.valueOf(tempUpdateMapValue.get("updateProgress")));
                    resultMap.put("updateSuccess", String.valueOf(tempUpdateMapValue.get("updateSuccess")));
                    resultMap.put("updateFail", String.valueOf(tempUpdateMapValue.get("updateFail")));
                    resultMap.put("updateEtc", String.valueOf(tempUpdateMapValue.get("updateEtc")));
                    resultMap.put("updateFailRate", String.valueOf(tempUpdateMapValue.get("updateFailRate")));
                } else {
                    resultMap.put("updateProgress", "-");
                    resultMap.put("updateSuccess", "-");
                    resultMap.put("updateFail", "-");
                    resultMap.put("updateEtc", "-");
                    resultMap.put("updateFailRate", "-");
                }

                result.add(resultMap);
            }
        }

        return result;
    }

    private List<Map<String, String>> parsePartNoUpdateStateListNew2(List<HMap> hMapList) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        Map<String, Map<String, Object>> tempDownloadMap = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> tempUpdateMap = new HashMap<String, Map<String, Object>>();
        Map<String, Integer> tempVinMap = new HashMap<String, Integer>();
        Map<String, Integer> tempKeyAndDownloadProgressMap = new HashMap<String, Integer>();
        Map<String, Integer> tempKeyAndUpdateProgressMap = new HashMap<String, Integer>();

        for (HMap temp : hMapList) {
            if (temp.containsKey("downloadIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("downloadIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    String partNo = "";
                    String targetEventID = "";

                    int downloadProgress = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("downloadresults")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("downloadresults");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
                            if (temp2.containsKey("downloadresult")) {
                                String downloadresult = (String) temp2.get("downloadresult");

                                if ("0".equals(downloadresult)) {
                                    downloadProgress = (int) temp2.get("count");
                                }
                            }
                        }
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempKeyAndDownloadProgressMap.containsKey(key)) {
                        tempKeyAndDownloadProgressMap.put(key, downloadProgress);
                    }
                }
            }

            if (temp.containsKey("updateIngData")) {
                List<Map<String, Object>> tempDownloadIngData = (List<Map<String, Object>>) temp.get("updateIngData");

                for (Map<String, Object> temp1 : tempDownloadIngData) {
                    String partNo = "";
                    String targetEventID = "";

                    int updateProgress = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("reportCodes")) {
                        List<Map<String, Object>> tempReportCodes = (List<Map<String, Object>>) temp1.get("reportCodes");

                        for (Map<String, Object> temp2 : tempReportCodes) {
                            if (temp2.containsKey("reportCode")) {
                                String reportCode = (String) temp2.get("reportCode");

                                if ("1002".equals(reportCode)) {
                                    updateProgress = (int) temp2.get("count");
                                }
                            }
                        }
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempKeyAndUpdateProgressMap.containsKey(key)) {
                        tempKeyAndUpdateProgressMap.put(key, updateProgress);
                    }
                }
            }

            if (temp.containsKey("downloadData")) {
                List<Map<String, Object>> tempDownloadData = (List<Map<String, Object>>) temp.get("downloadData");

                for (Map<String, Object> temp1 : tempDownloadData) {
                    String partNo = "";
                    String targetEventID = "";

                    int downloadSuccess = 0;
                    int downloadFail = 0;
                    int downloadEtc = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("srresultCodes")) {
                        List<Map<String, Object>> tempSrResultCodes = (List<Map<String, Object>>) temp1.get("srresultCodes");

                        for (Map<String, Object> temp2 : tempSrResultCodes) {
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

                    String key = partNo + "^" + targetEventID;
                    if (!tempDownloadMap.containsKey(key)) {
                        Map<String, Object> tempValue = new HashMap<String, Object>();

                        int downloadProgress = 0;
                        if (tempKeyAndDownloadProgressMap.containsKey(key)) {
                            downloadProgress = tempKeyAndDownloadProgressMap.get(key);
                        }
                        tempValue.put("downloadProgress", downloadProgress);
                        tempValue.put("downloadSuccess", downloadSuccess);
                        tempValue.put("downloadFail", downloadFail);
                        tempValue.put("downloadEtc", downloadEtc);
                        int total = downloadProgress + downloadSuccess + downloadFail + downloadEtc;
                        double downloadFailRate = 0;
                        if (total > 0) {
                            downloadFailRate = Math.round(((double) downloadFail / (double) total) * 1000) / (double) 10;
                        }
                        tempValue.put("downloadFailRate", downloadFailRate);

                        tempDownloadMap.put(key, tempValue);
                    }
                }
            }

            for (String tempKey : tempKeyAndDownloadProgressMap.keySet()) {
                if (!tempDownloadMap.containsKey(tempKey)) {
                    Map<String, Object> tempValue = new HashMap<String, Object>();

                    int downloadProgress = tempKeyAndDownloadProgressMap.get(tempKey);

                    tempValue.put("downloadProgress", downloadProgress);
                    tempValue.put("downloadSuccess", 0);
                    tempValue.put("downloadFail", 0);
                    tempValue.put("downloadEtc", 0);
                    tempValue.put("downloadFailRate", (double) 0.0);

                    tempDownloadMap.put(tempKey, tempValue);
                }
            }

            if (temp.containsKey("updateData")) {
                List<Map<String, Object>> tempUpdateData = (List<Map<String, Object>>) temp.get("updateData");

                for (Map<String, Object> temp1 : tempUpdateData) {
                    String partNo = "";
                    String targetEventID = "";

                    int updateSuccess = 0;
                    int updateFail = 0;
                    int updateEtc = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("isresultCodes")) {
                        List<Map<String, Object>> tempDownloadResults = (List<Map<String, Object>>) temp1.get("isresultCodes");

                        for (Map<String, Object> temp2 : tempDownloadResults) {
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

                    String key = partNo + "^" + targetEventID;
                    if (!tempUpdateMap.containsKey(key)) {
                        Map<String, Object> tempValue = new HashMap<String, Object>();

                        int updateProgress = 0;
                        if (tempKeyAndUpdateProgressMap.containsKey(key)) {
                            updateProgress = tempKeyAndUpdateProgressMap.get(key);
                        }
                        tempValue.put("updateProgress", updateProgress);
                        tempValue.put("updateSuccess", updateSuccess);
                        tempValue.put("updateFail", updateFail);
                        tempValue.put("updateEtc", updateEtc);
                        int total = updateProgress + updateSuccess + updateFail + updateEtc;
                        double updateFailRate = 0;
                        if (total > 0) {
                            updateFailRate = Math.round(((double) updateFail / (double) total) * 1000) / (double) 10;
                        }
                        tempValue.put("updateFailRate", updateFailRate);

                        tempUpdateMap.put(key, tempValue);
                    }
                }
            }

            for (String tempKey : tempKeyAndUpdateProgressMap.keySet()) {
                if (!tempUpdateMap.containsKey(tempKey)) {
                    Map<String, Object> tempValue = new HashMap<String, Object>();

                    int updateProgress = tempKeyAndUpdateProgressMap.get(tempKey);

                    tempValue.put("updateProgress", updateProgress);
                    tempValue.put("updateSuccess", 0);
                    tempValue.put("updateFail", 0);
                    tempValue.put("updateEtc", 0);
                    tempValue.put("updateFailRate", (double) 0.0);

                    tempUpdateMap.put(tempKey, tempValue);
                }
            }

            if (temp.containsKey("vinCount")) {
                List<Map<String, Object>> tempVinCount = (List<Map<String, Object>>) temp.get("vinCount");

                for (Map<String, Object> temp1 : tempVinCount) {
                    String partNo = "";
                    String targetEventID = "";

                    int totalVinCnt = 0;

                    if (temp1.containsKey("_id")) {
                        Map<String, Object> _id = (Map<String, Object>) temp1.get("_id");

                        if (_id.containsKey("partNo")) {
                            partNo = (String) _id.get("partNo");
                        }

                        if (_id.containsKey("targetEventID")) {
                            targetEventID = (String) _id.get("targetEventID");
                        }
                    }

                    if (temp1.containsKey("carCount")) {
                        totalVinCnt = (int) temp1.get("carCount");
                    }

                    String key = partNo + "^" + targetEventID;
                    if (!tempVinMap.containsKey(key)) {
                        tempVinMap.put(key, totalVinCnt);
                    }
                }
            }

            Object[] mapkey = tempDownloadMap.keySet().toArray();
            Arrays.sort(mapkey);

            for (Object key : mapkey) {
                Map<String, String> resultMap = new HashMap<String, String>();

                String[] keySplit = key.toString().split("\\^");

                if (keySplit.length == 2) {
                    resultMap.put("partNo", keySplit[0]);
                    resultMap.put("targetEventID", keySplit[1]);
                } else if (keySplit.length == 1) {
                    resultMap.put("partNo", keySplit[0]);
                }

                if (tempVinMap.containsKey(key)) {
                    resultMap.put("totalCarCnt", String.valueOf(tempVinMap.get(key)));
                } else {
                    resultMap.put("totalCarCnt", "-");
                }

                Map<String, Object> tempDownloadMapValue = tempDownloadMap.get(key);

                resultMap.put("downloadProgress", String.valueOf(tempDownloadMapValue.get("downloadProgress")));
                resultMap.put("downloadSuccess", String.valueOf(tempDownloadMapValue.get("downloadSuccess")));
                resultMap.put("downloadFail", String.valueOf(tempDownloadMapValue.get("downloadFail")));
                resultMap.put("downloadEtc", String.valueOf(tempDownloadMapValue.get("downloadEtc")));
                resultMap.put("downloadFailRate", String.valueOf(tempDownloadMapValue.get("downloadFailRate")));

                if (tempUpdateMap.containsKey(key)) {
                    Map<String, Object> tempUpdateMapValue = tempUpdateMap.get(key);

                    resultMap.put("updateProgress", String.valueOf(tempUpdateMapValue.get("updateProgress")));
                    resultMap.put("updateSuccess", String.valueOf(tempUpdateMapValue.get("updateSuccess")));
                    resultMap.put("updateFail", String.valueOf(tempUpdateMapValue.get("updateFail")));
                    resultMap.put("updateEtc", String.valueOf(tempUpdateMapValue.get("updateEtc")));
                    resultMap.put("updateFailRate", String.valueOf(tempUpdateMapValue.get("updateFailRate")));
                } else {
                    resultMap.put("updateProgress", "-");
                    resultMap.put("updateSuccess", "-");
                    resultMap.put("updateFail", "-");
                    resultMap.put("updateEtc", "-");
                    resultMap.put("updateFailRate", "-");
                }
                result.add(resultMap);


            }
        }

        return result;
    }

    public void settingDownloadAndUpdateFailCode(EvntVersionUpdateStatusVO vo) {
        //다운로드 실패코드 세팅
        List<String> downloadFailCode = new ArrayList<>();
        //다운로드 실패코드가 901 903 905 951~ 954
        downloadFailCode.add("901");
        downloadFailCode.add("903");
        downloadFailCode.add("905");
        for (int i = 951; i < 955; i++) {
            downloadFailCode.add(Integer.toString(i));
        }
        vo.setDownloadFailCodeArr(downloadFailCode.toArray(new String[0]));

        //업데이트 실패코드 세팅
        List<String> updateFailCode = new ArrayList<>();
        //업데이트 실패코드 1400, 1402~1413(12), 1450~1501(52), 1550~1599(50)
        updateFailCode.add("1400");
        for (int i = 1402; i < 1414; i++) {
            updateFailCode.add(Integer.toString(i));
        }
        for (int i = 1450; i < 1502; i++) {
            updateFailCode.add(Integer.toString(i));
        }
        for (int i = 1550; i < 1600; i++) {
            updateFailCode.add(Integer.toString(i));
        }
        vo.setUpdateFailCodeArr(updateFailCode.toArray(new String[0]));
    }

    private List<Map<String, String>> selectEvntFailDetailList2(List<HMap> hMapList, EvntVersionUpdateStatusVO vo) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        //List<HMap> downloadFailData = (List<HMap>) evntFailDetailList.get(0).get("downloadFailData");
        for (HMap temp : hMapList) {
            if (vo.getDownloadOrUpdate().equals("download")) {
                if (temp.containsKey("downloadFailData")) {
                    List<Map<String, Object>> downloadFailData = (List<Map<String, Object>>) temp.get("downloadFailData");

                    for (Map<String, Object> temp1 : downloadFailData) {
                        if (temp1.containsKey("matchdata")) {
                            Map<String, String> matchdata = (Map<String, String>) temp1.get("matchdata");
                            result.add(matchdata);
                        }
                    }
                }
            } else if (vo.getDownloadOrUpdate().equals("update")) {
                if (temp.containsKey("updateFailData")) {
                    List<Map<String, Object>> updateFailData = (List<Map<String, Object>>) temp.get("updateFailData");

                    for (Map<String, Object> temp1 : updateFailData) {
                        if (temp1.containsKey("matchdata")) {
                            Map<String, String> matchdata = (Map<String, String>) temp1.get("matchdata");
                            result.add(matchdata);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public List<Map<String, String>> selectEvntDownloadSuccessDetailList(EvntVersionUpdateStatusVO vo) {
        Criteria criteria = new Criteria();
        Criteria criteriaDownloading = new Criteria();
        List<Criteria> criteriaList = setBasicRoleCriteria(vo, "requesttime");
        List<Criteria> criteriaDownloadSuccessList = new ArrayList<>();

        // package명 으로 검색
        if (vo.getSelectPckgNm() != null && !vo.getSelectPckgNm().isEmpty()) {
            String packagename = vo.getSelectPckgNm();
            criteriaList.add(Criteria.where("packagename").in(packagename));
        } else {
            if (!"A".equals(vo.getAuthGrpCd())) {
                if (vo.getPackagenameArr() != null && vo.getPackagenameArr().length > 0) {
                    criteriaList.add(Criteria.where("packagename").in(vo.getPackagenameArr()));
                } else {
                    criteriaList.add(Criteria.where("packagename").regex(""));
                }
            } else {
                criteriaList.add(Criteria.where("packagename").regex(""));
            }
        }

        if (vo.getTargetEventID() != null && !vo.getTargetEventID().isEmpty()) {
            String targetEventId = vo.getTargetEventID();
            criteriaList.add(Criteria.where("targetEventID").in(targetEventId));
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.targetEventID").in(targetEventId)); //실패 코드를 group화를 위한 match리스트
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaList.add(Criteria.where("targetEventID").regex(""));
            criteriaDownloadSuccessList.add(Criteria.where("targetEventID").regex("")); //실패 코드를 group화를 위한 match리스트
        }
        // 이벤트 타입 명으로 검색
        if (vo.getEvntType() != null && !vo.getEvntType().isEmpty()) {
            String evntType = vo.getEvntType();
            criteriaList.add(Criteria.where("testDv").in(evntType));
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaList.add(Criteria.where("testDv").regex(""));
        }

        criteria.andOperator(criteriaList.toArray(new Criteria[0]));

        // 실패 코드만 match
        if (vo.getPartNo() != null && !vo.getPartNo().isEmpty()) {
            String partNo = vo.getPartNo();
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.partNo").is(partNo));
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.partNo").regex(""));
        }
        if ("download".equals(vo.getDownloadOrUpdate())) {
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.srresultCode").is("900"));
        } else {
            List<String> allList = new ArrayList<>();
            allList.add("1200");
            allList.addAll(IntStream.range(1251, 1300).mapToObj(Integer::toString).toList());
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.isresultCode").in(allList));
        }
        criteriaDownloading.andOperator(criteriaDownloadSuccessList.toArray(new Criteria[0]));

        AggregationOperation match = Aggregation.match(criteria);
        AggregationOperation match2 = Aggregation.match(criteriaDownloading);
        FacetOperation facet = new FacetOperation();

        // match
        // group
        AggregationOperation group = Aggregation.group(Fields.fields().and("vin").and("partNo")).max("createdAt").as("maxCreatedAt");
        // lookup
        String expression = "\"$expr\" : { \"$and\" : [{ \"$eq\" : [\"$vin\", \"$$vin\"]}, { \"$eq\" : [\"$partNo\", \"$$partNo\"]}, { \"$eq\" : [\"$createdAt\", \"$$createdAt\"]}]}";
        BooleanOperators.And andExpression = BooleanOperators.And.and(
                ComparisonOperators.valueOf("$vin").equalTo("$$vin"),
                ComparisonOperators.valueOf("$partNo").equalTo("$$partNo"),
                ComparisonOperators.valueOf("$createdAt").equalTo("$$createdAt"));
        LookupOperation lookup = Aggregation.lookup().from("StatDeviceDownload").let(
                        VariableOperators.Let.ExpressionVariable.newVariable("vin").forField("$_id.vin"),
                        VariableOperators.Let.ExpressionVariable.newVariable("partNo").forField("$_id.partNo"),
                        VariableOperators.Let.ExpressionVariable.newVariable("createdAt").forField("$maxCreatedAt"))
                .pipeline(Aggregation.match(EvaluationOperators.Expr.valueOf(andExpression)))
                .as("matchdata");
        UnwindOperation unwind = Aggregation.unwind("$matchdata");
        // sort
        AggregationOperation sort = Aggregation.sort(Sort.Direction.ASC, "maxCreatedAt");
        AggregationOperation skip = Aggregation.skip((long) vo.getLimit() * (vo.getCurrPage() - 1));
        AggregationOperation limit = Aggregation.limit(vo.getLimit());

        Aggregation agg = Aggregation.newAggregation(match, group, lookup, unwind,
                        match2, sort, skip, limit)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

        logger.info("**************************************");
        logger.info("agg NOSQL ::: " + agg.toString());
        logger.info("**************************************");

        AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

        logger.info("**************************************");
        logger.info("HMap Result ::: " + aggregate.getMappedResults());
        logger.info("**************************************");
        List<Map<String, String>> result = new ArrayList<>();
        for (HMap temp : aggregate.getMappedResults()) {
            if (temp.containsKey("matchdata")) {
                Map<String, String> matchdata = (Map<String, String>) temp.get("matchdata");
                result.add(matchdata);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> selectEvntDownloadingOrUpdatingDetailList(EvntVersionUpdateStatusVO vo) {
        Criteria criteria = new Criteria();
        Criteria criteriaDownloading = new Criteria();
        List<Criteria> criteriaList = setBasicRoleCriteria(vo, "requesttime");
        List<Criteria> criteriaDownloadSuccessList = new ArrayList<>();

        // package명 으로 검색
        if (vo.getSelectPckgNm() != null && !vo.getSelectPckgNm().isEmpty()) {
            String packagename = vo.getSelectPckgNm();
            criteriaList.add(Criteria.where("packagename").in(packagename));
        } else {
            if (!"A".equals(vo.getAuthGrpCd())) {
                if (vo.getPackagenameArr() != null && vo.getPackagenameArr().length > 0) {
                    criteriaList.add(Criteria.where("packagename").in(vo.getPackagenameArr()));
                } else {
                    criteriaList.add(Criteria.where("packagename").regex(""));
                }
            } else {
                criteriaList.add(Criteria.where("packagename").regex(""));
            }
        }

        if (vo.getTargetEventID() != null && !vo.getTargetEventID().isEmpty()) {
            String targetEventId = vo.getTargetEventID();
            criteriaList.add(Criteria.where("targetEventID").in(targetEventId));
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.targetEventID").in(targetEventId)); //실패 코드를 group화를 위한 match리스트
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaList.add(Criteria.where("targetEventID").regex(""));
            criteriaDownloadSuccessList.add(Criteria.where("targetEventID").regex("")); //실패 코드를 group화를 위한 match리스트
        }
        // 이벤트 타입 명으로 검색
        if (vo.getEvntType() != null && !vo.getEvntType().isEmpty()) {
            String evntType = vo.getEvntType();
            criteriaList.add(Criteria.where("testDv").in(evntType));
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaList.add(Criteria.where("testDv").regex(""));
        }

        criteria.andOperator(criteriaList.toArray(new Criteria[0]));

        // 실패 코드만 match
        if (vo.getPartNo() != null && !vo.getPartNo().isEmpty()) {
            String partNo = vo.getPartNo();
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.partNo").is(partNo));
            // 2022.07.26 Modify by SungJin Seo - MongoDB Index 추가로 regex 로직 추가
        } else {
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.partNo").regex(""));
        }
        if ("download".equals(vo.getDownloadOrUpdate())) {
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.downloadresult").is("0"));
        } else {
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.reportCode").is("1002"));
            criteriaDownloadSuccessList.add(Criteria.where("matchdata.isresultCode").is(""));
        }
        criteriaDownloading.andOperator(criteriaDownloadSuccessList.toArray(new Criteria[0]));

        AggregationOperation match = Aggregation.match(criteria);
        AggregationOperation match2 = Aggregation.match(criteriaDownloading);

        // match
        // group
        AggregationOperation group = Aggregation.group(Fields.fields().and("vin").and("partNo")).max("createdAt").as("maxCreatedAt");
        // lookup
        String expression = "\"$expr\" : { \"$and\" : [{ \"$eq\" : [\"$vin\", \"$$vin\"]}, { \"$eq\" : [\"$partNo\", \"$$partNo\"]}, { \"$eq\" : [\"$createdAt\", \"$$createdAt\"]}]}";
        LookupOperation lookup = Aggregation.lookup().from("StatDeviceDownload").let(
                        VariableOperators.Let.ExpressionVariable.newVariable("vin").forField("$_id.vin"),
                        VariableOperators.Let.ExpressionVariable.newVariable("partNo").forField("$_id.partNo"),
                        VariableOperators.Let.ExpressionVariable.newVariable("createdAt").forField("$maxCreatedAt"))
                .pipeline(Aggregation.match(AggregationExpression.from(MongoExpression.create(expression))))
                .as("matchdata");
        UnwindOperation unwind = Aggregation.unwind("$matchdata");
        // sort
        AggregationOperation sort = Aggregation.sort(Sort.Direction.ASC, "maxCreatedAt");
        AggregationOperation skip = Aggregation.skip(vo.getLimit() * (vo.getCurrPage() - 1));
        AggregationOperation limit = Aggregation.limit(vo.getLimit());

        Aggregation agg = Aggregation.newAggregation(match, group, lookup, unwind,
                        match2, sort, skip, limit)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

        logger.info("**************************************");
        logger.info("agg NOSQL ::: " + agg.toString());
        logger.info("**************************************");

        AggregationResults<HMap> aggregate = mongoTemplate.aggregate(agg, "StatDeviceDownload", HMap.class);

        logger.info("**************************************");
        logger.info("HMap Result ::: " + aggregate.getMappedResults());
        logger.info("**************************************");
        List<Map<String, String>> result = new ArrayList<>();
        for (HMap temp : aggregate.getMappedResults()) {
            if (temp.containsKey("matchdata")) {
                Map<String, String> matchdata = (Map<String, String>) temp.get("matchdata");
                result.add(matchdata);
            }
        }
        return result;
    }
}
