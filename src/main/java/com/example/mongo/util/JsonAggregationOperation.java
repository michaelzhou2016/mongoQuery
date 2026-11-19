package com.example.mongo.util;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;


/**
 * <pre>
 * Statements
 * </pre>
 *
 * @author SungJin Seo
 * @version 1.0
 * @ClassName : JsonAggregationOperation.java
 * @Description : 클래스 설명을 기술합니다.
 * @Modification Information
 * <pre>
 *     since          author              description
 *  ===========    =============    ===========================
 *  2022. 9. 27.     SungJin Seo     	최초 생성
 * </pre>
 * @see
 * @since 2022. 9. 27.
 */

public class JsonAggregationOperation implements AggregationOperation {

    private final String jsonOperation;

    public JsonAggregationOperation(String jsonOperation) {
        this.jsonOperation = jsonOperation;
    }

    /*
     * @see org.springframework.data.mongodb.core.aggregation.AggregationOperation#toDBObject(org.springframework.data.mongodb.core.aggregation.AggregationOperationContext)
     */
//    @Override
//    public Document toDocument(AggregationOperationContext context) {
//        return context.getMappedObject(Document.parse(jsonOperation));
//    }

    /*
     * @see org.springframework.data.mongodb.core.aggregation.AggregationOperation#toDBObject(org.springframework.data.mongodb.core.aggregation.AggregationOperationContext)
     */
//    @Override
//    public DBObject toDBObject(AggregationOperationContext context) {
//        return context.getMappedObject((DBObject) JSON.parse(jsonOperation));
//    }

    @Override
    public Document toDocument(AggregationOperationContext aggregationOperationContext) {
        return Document.parse(jsonOperation);
    }


}
