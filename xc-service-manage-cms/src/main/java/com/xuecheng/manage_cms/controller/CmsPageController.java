package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: CmsPageController实现类
 * @Create: 2020-01-20 18:10:10
 * @Modified By:
 */
@RestController
@RequestMapping("/cms")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    PageService pageService;

    @Override
    @GetMapping("/page/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {

        return pageService.findList(page, size, queryPageRequest);
    }

    @Override
    @PostMapping("/page/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return pageService.add(cmsPage);
    }

    @Override
    @GetMapping("/page/get/{id}")
    public CmsPage findById(@PathVariable("id") String id) {
        return pageService.findById(id);
    }

    @Override
    @PutMapping("/page/edit/{id}")
    public CmsPageResult edit(@PathVariable("id") String id, @RequestBody CmsPage cmsPage) {
        return pageService.edit(id, cmsPage);
    }

    @Override
    @DeleteMapping("/page/del/{id}")
    public ResponseResult delete(@PathVariable("id") String id) {
        return pageService.delete(id);
    }

    @Override
    @PostMapping("/page/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {
        return pageService.postPage(pageId);
    }

    @Override
    @PostMapping("/page/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {

        return pageService.save(cmsPage);
    }

    @Override
    @PostMapping("/page/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return pageService.postPageQuick(cmsPage);
    }
}
