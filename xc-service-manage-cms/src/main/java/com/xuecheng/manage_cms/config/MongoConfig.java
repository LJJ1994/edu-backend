package com.xuecheng.manage_cms.config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: mongoDB 配置类
 * @Create: 2020-01-23 14:18:18
 * @Modified By:
 */
@Configuration
public class MongoConfig {
    @Value("${spring.data.mongodb.database}")
    String db;

    /**
    * @Description: getGridFSBucket 生成一个bucket下载流对象
    * @Param: [mongoClient]
    * @return: com.mongodb.client.gridfs.GridFSBucket
    * @Author: LJJ
    * @Date: 2020/1/23 14:21
    */
    @Bean
    public GridFSBucket getGridFSBucket(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(db);
        GridFSBucket gridFSBucket = GridFSBuckets.create(database);
        return gridFSBucket;
    }
}
