package com.xuecheng.manage_course.client;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
* @Description: CmsPageClient 远程调用cms服务的接口
* @Author: LJJ
* @Date: 2020/1/29 22:25
* @Modified By:
*/
@FeignClient(value = "XC-SERVICE-MANAGE-CMS")
public interface CmsPageClient {
    // 获取页面
    @GetMapping("/cms/page/get/{id}")
    public CmsPage findCmsPageById(@PathVariable("id") String id);

    //保存页面
    @PostMapping("/cms/page/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage);

    // 发布页面
    @PostMapping("/cms/page/postPageQuick")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
