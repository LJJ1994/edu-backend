package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-01-29 21:44:44
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFeign {
    @Autowired
    CmsPageClient cmsPageClient;

    @Test
    public void testRibbon() {
        // 使用Feign调用远程接口
        CmsPage cmsPageById = cmsPageClient.findCmsPageById("5a795ac7dd573c04508f3a56");
        System.out.println(cmsPageById);
    }
}
