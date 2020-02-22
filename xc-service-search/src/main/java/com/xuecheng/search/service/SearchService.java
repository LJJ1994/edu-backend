package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-09 08:12:12
 * @Modified By:
 */
@Service
public class SearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Value("${xuecheng.elasticsearch.course.index}")
    String es_index;

    @Value("${xuecheng.elasticsearch.media.index}")
    String media_index;

    @Value("${xuecheng.elasticsearch.course.type}")
    String es_type;

    @Value("${xuecheng.elasticsearch.media.type}")
    String media_type;

    @Value("${xuecheng.elasticsearch.course.source_field}")
    String source_field;

    @Value("${xuecheng.elasticsearch.media.source_field}")
    String media_source_field;

    public QueryResponseResult list(int page, int size, CourseSearchParam courseSearchParam) {
        // 获取ES请求对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        searchRequest.types(es_type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields, new String[]{});
        // 如果搜索对象中有关键字
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(),
                    "name", "description", "teachplan");
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            multiMatchQueryBuilder.field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        // 过滤
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
        searchSourceBuilder.query(boolQueryBuilder);
        // 分页
        if (page<=0) {
            page = 1;
        }
        if (size<=0) {
            size = 20;
        }
        int start = (page-1)*size;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);
        // 高亮，暂时实现name高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("xuecheng search error....{}", e.getMessage());
            return new QueryResponseResult(CommonCode.SUCCESS, new QueryResult());
        }
        // 结果集处理
        SearchHits hits = searchResponse.getHits();
        // 结果总记录数
        long total = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        List<CoursePub> list = new ArrayList<>();
        for (SearchHit hit:searchHits) {
            CoursePub coursePub = new CoursePub();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 课程id
            String courseId = (String) sourceAsMap.get("id");
            coursePub.setId(courseId);
            // 名称
            String name = (String) sourceAsMap.get("name");
            coursePub.setName(name);
            // 高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameField = highlightFields.get("name");
                if (nameField !=  null) {
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text text:fragments) {
                        stringBuffer.append(text.toString());
                    }
                    name = stringBuffer.toString();
                }
            }

            // 图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            // 价格
            Float price = null;
            try {
                if (sourceAsMap.get("price")!=null) {
                    price = Float.parseFloat(sourceAsMap.get("price").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice(price);
            // 旧价格
            Float price_old = null;
            try {
                if (sourceAsMap.get("price_old")!=null) {
                    price_old = Float.parseFloat(sourceAsMap.get("price_old").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice_old(price_old);
            list.add(coursePub);
        }
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        queryResult.setTotal(total);
        queryResult.setList(list);
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    // 根据课程id查询课程计划信息（在课程播放页面查询）
    public Map<String, CoursePub> getall(String id) {
        // 定义ES搜索对象和源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id", id));
        SearchRequest searchRequest = new SearchRequest(es_index);
        searchRequest.types(es_type);
        searchRequest.source(searchSourceBuilder);

        // 处理搜索结果
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        Map<String, CoursePub> map = new HashMap<>();
        for (SearchHit hit : searchHits) {
            CoursePub coursePub = new CoursePub();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String courseId = (String) sourceAsMap.get("id");
            String name = (String) sourceAsMap.get("name");
            String teachplan = (String) sourceAsMap.get("teachplan");
            String grade = (String) sourceAsMap.get("grade");
            String charge = (String) sourceAsMap.get("charge");
            String description = (String) sourceAsMap.get("description");
            String pic = (String) sourceAsMap.get("pic");

            coursePub.setName(name);
            coursePub.setTeachplan(teachplan);
            coursePub.setCharge(charge);
            coursePub.setGrade(grade);
            coursePub.setId(courseId);
            coursePub.setDescription(description);
            coursePub.setPic(pic);

            map.put(courseId, coursePub);
        }
        return map;
    }

    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanId) {
        // 定义ES搜索对象和源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id", teachplanId));
        SearchRequest searchRequest = new SearchRequest(media_index);
        searchRequest.types(media_type);
        String[] source_fields = media_source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields, new String[]{});
        searchRequest.source(searchSourceBuilder);

        // 处理搜索结果
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long total = 0;
        try {
            SearchHits responseHits = searchResponse.getHits();
            SearchHit[] searchHits = responseHits.getHits();
            total = responseHits.getTotalHits();
            for(SearchHit searchHit : searchHits) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                // 取出ES课程计划媒资信息
                String courseId = (String) sourceAsMap.get("courseid");
                String teachplanId1 = (String) sourceAsMap.get("teachplan_id");
                String mediaId = (String) sourceAsMap.get("media_id");
                String mediaFileOriginalName = (String) sourceAsMap.get("media_fileoriginalname");
                String mediaUrl = (String) sourceAsMap.get("media_url");

                teachplanMediaPub.setCourseId(courseId);
                teachplanMediaPub.setMediaId(mediaId);
                teachplanMediaPub.setMediaUrl(mediaUrl);
                teachplanMediaPub.setMediaFileOriginalName(mediaFileOriginalName);
                teachplanMediaPub.setTeachplanId(teachplanId1);
                teachplanMediaPubList.add(teachplanMediaPub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        queryResult.setTotal(total);

        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }
}
