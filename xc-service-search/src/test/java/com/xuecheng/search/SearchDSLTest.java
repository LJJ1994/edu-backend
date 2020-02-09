package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-08 04:03:03
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchDSLTest {

    @Autowired
    RestClient restClient;

    @Autowired
    RestHighLevelClient client;

    @Test
    public void testSearchAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
//            String id = hit.getId();
//            String type = hit.getType();
//            float score = hit.getScore();
//            String index = hit.getIndex();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(price);
        }

    }

    // 分页查询
    @Test
    public void testSearchPage() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int page = 2; // 第二页
        int size = 1; // 每页显示1个记录
        int from = (page-1)*size; //记录开始的下标
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(price);
        }

    }

    // 精确查询
    @Test
    public void testSearchTerm() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 精确查询
        searchSourceBuilder.query(QueryBuilders.termQuery("name", "spring开发"));
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(name);
        }

    }

    // ids精确查询
    @Test
    public void testSearchTermByids() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String[] ids = new String[]{"1","2"};
        // 精确查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", ids));
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(name);
        }
    }

    // match query查询
    @Test
    public void testMatchQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "spring开发").minimumShouldMatch("80%"));
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(name);
        }
    }

    // multi match query查询
    @Test
    public void testMultiMatchQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", "name", "description").minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);
        searchSourceBuilder.query(multiMatchQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(name);
        }
    }

    // bool query查询
    @Test
    public void testBoolQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // multi query
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("Bootstrap开发", "name", "description").minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);
        // term query
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201002");
        // bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(name);
        }
    }

    // bool filter query查询
    @Test
    public void testBoolAndFilterQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel", "description"}, new String[]{});
        // multi query
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("Bootstrap开发", "name", "description");
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);

        // bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // bool query在以上multi query 基础上查询
        boolQueryBuilder.must(multiMatchQueryBuilder);
        // 过滤器
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201002"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(30).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(name);
        }
    }

    // bool filter sort query查询
    @Test
    public void testSort() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel", "description"}, new String[]{});

        // bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(30).lte(100));
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(name);
        }
    }

    // 高亮查询
    @Test
    public void testHighlight() throws IOException {
        SearchRequest searchRequest = new SearchRequest("xc_course");
        searchRequest.types("doc");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "price", "studymodel", "description"}, new String[]{});
        // multi query
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发", "name", "description");
        multiMatchQueryBuilder.minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name", 10);

        // bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // bool query在以上multi query 基础上查询
        boolQueryBuilder.must(multiMatchQueryBuilder);
        // 过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(30).lte(100));
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.ASC));

        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag1>");
        highlightBuilder.postTags("</tag1>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse search = client.search(searchRequest);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();

        for (SearchHit hit : hits1) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField highlightField = highlightFields.get("name");
                Text[] fragments = highlightField.fragments();
                StringBuffer stringBuffer = new StringBuffer();
                for (Text text:fragments) {
                    stringBuffer.append(text);
                }
                name = stringBuffer.toString();
                System.out.println(name);
            }
        }
    }
}
