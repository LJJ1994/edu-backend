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
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
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
public class SearchTest {

    @Autowired
    RestClient restClient;

    @Autowired
    RestHighLevelClient client;

    // 创建索引库
    @Test
    public void testCreateIndex() throws IOException {
        // 创建索引请求对象，并创建索引名称
        CreateIndexRequest indexRequest = new CreateIndexRequest("xc_course");
        //设置索引参数，如分片，副本
        indexRequest.settings(Settings.builder().put("number_of_shards", "1").put("number_of_replicas","0"));
        // 创建映射
        indexRequest.mapping("doc"," {\n" +
                " \t\"properties\": {\n" +
                " \"name\": {\n" +
                " \"type\": \"text\",\n" +
                " \"analyzer\":\"ik_max_word\",\n" +
                " \"search_analyzer\":\"ik_smart\"\n" +
                " },\n" +
                " \"description\": {\n" +
                " \"type\": \"text\",\n" +
                " \"analyzer\":\"ik_max_word\",\n" +
                " \"search_analyzer\":\"ik_smart\"\n" +
                " },\n" +
                " \"studymodel\": {\n" +
                " \"type\": \"keyword\"\n" +
                " },\n" +
                " \"price\": {\n" +
                " \"type\": \"float\"\n" +
                " }\n" +
                " }\n" +
                "}", XContentType.JSON);
        // 创建索引操作客户端
        IndicesClient indices = client.indices();
        // 创建响应对象
        CreateIndexResponse createIndexResponse = indices.create(indexRequest);
        // 获取响应结果
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    // 删除索引库
    @Test
    public void testDeleteIndex() throws IOException {
        DeleteIndexRequest indexRequest = new DeleteIndexRequest("xc_course");
        DeleteIndexResponse delete = client.indices().delete(indexRequest);
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);
    }

    // 添加文档
    @Test
    public void testAdd() throws IOException{
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "测试");
        hashMap.put("description", "测试");
        hashMap.put("studymodel", "201001");
        hashMap.put("price", 5.6f);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS");
        hashMap.put("timestamp", simpleDateFormat.format(new Date()));
        IndexRequest indexRequest = new IndexRequest("xc_course","doc");
        indexRequest.source(hashMap);
        IndexResponse indexResponse = client.index(indexRequest);
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);

    }

    // 查询文档
    @Test
    public void testFind() throws IOException {
        GetRequest getRequest = new GetRequest("xc_course", "doc", "nDJhIXABXjIElYJOdlw-");
        GetResponse getResponse = client.get(getRequest);
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    // 更新文档
    @Test
    public void testUpdate() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_course", "doc", "nDJhIXABXjIElYJOdlw-");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", "Spring实战");
        updateRequest.doc(hashMap);
        UpdateResponse updateResponse = client.update(updateRequest);
        RestStatus status = updateResponse.status();
        System.out.println(status);
    }

    // 删除文档
    @Test
    public void testDelete() throws IOException {
        DeleteRequest deleteRequest = new org.elasticsearch.action.delete.DeleteRequest("xc_course", "doc", "nTJkIXABXjIElYJOHVwi");
        DeleteResponse delete = client.delete(deleteRequest);
        DocWriteResponse.Result result = delete.getResult();
        System.out.println(result);
    }
}
