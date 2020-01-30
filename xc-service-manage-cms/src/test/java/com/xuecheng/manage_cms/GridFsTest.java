package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Author: LJJ
 * @Program:
 * @Description:eduBackend
 * @Create: 2020-01-23 13:34:34
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFsTest {
    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    // 存储index_banner文件
    @Test
    public void testSaveIndexBannerTemplate() throws FileNotFoundException {
        File file = new File("e:/index_banner.ftl");
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectId id = gridFsTemplate.store(fileInputStream, "轮播图测试test001");
        String fileId = id.toString();
        System.out.println(id);
    }

    // 读取index_banner文件
    @Test
    public void testGetIndexBannerTemplate() throws IOException {
        String fileId = "5e2bfb3f26d7a436483692df";
        // 根据id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        // 创建GridFsResource文件流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
        System.out.println(s);
    }

    // 删除文件
    @Test
    public void testDeleteIndexBannerFSFile() {
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is("5e2a5830c234262970ecb860")));
    }

    // 存储course文件
    @Test
    public void testSaveCourseTemplate() throws FileNotFoundException {
        File file = new File("E:/program/java/eduBackend/freemarkertest/src/main/resources/templates/course.ftl");
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectId id = gridFsTemplate.store(fileInputStream, "课程详情页面00000001");
        String fileId = id.toString();
        System.out.println(id);
    }

    // 读取course文件
    @Test
    public void testGetCourseTemplate() throws IOException {
        String fileId = "5e3279b926a07b04e033e1d1";
        // 根据id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        // 创建GridFsResource文件流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
        System.out.println(s);
    }
}
