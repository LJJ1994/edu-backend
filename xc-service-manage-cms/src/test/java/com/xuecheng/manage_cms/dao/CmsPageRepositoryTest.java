package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: Test
 * @Create: 2020-01-21 08:37:37
 * @Modified By:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    // 查询所有
    @Test
    public void testFindAll() {
        List<CmsPage> list = cmsPageRepository.findAll();
        System.out.println(list);
    }

    // 分页查询
    @Test
    public void testFindPage() {
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    // 添加数据
    @Test
    public void testInsert() {
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("s2");
        cmsPage.setTemplateId("t2");
        cmsPage.setPageName("测试2");
        cmsPage.setPageAliase("测试别名2");
        cmsPage.setPageWebPath("/cms/test2");
        List<CmsPageParam> list = new ArrayList<>();
        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param2");
        cmsPageParam.setPageParamValue("value2");
        list.add(cmsPageParam);
        cmsPage.setPageParams(list);
        CmsPage save = cmsPageRepository.save(cmsPage);
        System.out.println(save);
    }

    // 修改数据
    @Test
    public void testUpdate() {
        Optional<CmsPage> optional = cmsPageRepository.findById("5abefd525b05aa293098fca6");
        if (optional.isPresent()) {
            CmsPage cmsPage = optional.get();
            cmsPage.setPageAliase("ccc");
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }

    // 删除数据
    @Test
    public void testDelete() {
        cmsPageRepository.deleteById("5e265052c234263bdc354b45");
    }

    // 测试自定义dao方法

    // 通过页面名称查询
    @Test
    public void testByPageName() {
        String pageName = "测试1";
        CmsPage byPageName = cmsPageRepository.findByPageName(pageName);
        System.out.println(byPageName);
    }

    // 页面名称和页面类型
    @Test
    public void testFindByPageNameAndPageType() {
        String pageName = "index_banner.html";
        String pageType = "0";
        CmsPage byPageNameAndPageType = cmsPageRepository.findByPageNameAndPageType(pageName, pageType);
        System.out.println(byPageNameAndPageType);
    }

    // 通过站点和页面类型查询记录数
    @Test
    public void testCountFindBySiteIdAndPageType() {
        String siteId = "5a751fab6abb5044e0d19ea1";
        String pageType = "1";
        int bySiteIdAndPageType = cmsPageRepository.findBySiteIdAndPageType(siteId, pageType);
    }

    //
    @Test
    public void testFindBySiteIdAndPageType() {
        String siteId = "5a751fab6abb5044e0d19ea1";
        String pageType = "1";
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findBySiteIdAndPageType(siteId, pageType, pageable);
        System.out.println(all);
    }
    // 自定义条件匹配器
    @Test
    public void testDefindFindAdd() {
        // 条件值对象
        CmsPage cmsPage = new CmsPage();
//        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
//        cmsPage.setTemplateId("5a925be7b00ffc4b3c1578b5");
        cmsPage.setPageAliase("轮播");
        // 分页对象
        Pageable pageable = new PageRequest(0, 10);
        // 条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        // 通过别名模糊查询
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        // 创建条件实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        System.out.println(all.getContent());
    }
}
