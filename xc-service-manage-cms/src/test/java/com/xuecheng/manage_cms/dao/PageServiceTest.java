package com.xuecheng.manage_cms.dao;

import com.xuecheng.manage_cms.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: a
 * @Create: 2020-01-25 14:31:31
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PageServiceTest {
    @Autowired
    PageService pageService;

    @Test
    public void testGetPageHtml() {
        String html = pageService.getPageHtml("5e2c045826d7a44a5073f524");
        System.out.println(html);
    }
}
