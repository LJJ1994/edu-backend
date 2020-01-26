package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: CmsPageService
 * @Create: 2020-01-21 10:09:09
 * @Modified By:
 */
@Service
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
    * @Description: findList 分页查询页面信息，返回一个List<CmsPage>
    * @Param: [page, size, queryPageRequest]
    * @return: com.xuecheng.framework.model.response.QueryResponseResult
    * @Author: LJJ
    * @Date: 2020/1/21 19:43
    */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }
        // 设置条件器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        // 设置条件值对象
        CmsPage cmsPage = new CmsPage();
        // 设置站点id
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        // 设置模板id
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        // 设置别名
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        if (size <= 0) {
            size = 10;
        }
        // 获取分页对象
        Pageable pageable = new PageRequest(page, size);
        // 设置条件对象
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        // 将条件对象和分页对象填充到findAll()方法中
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());

        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
    * @Description: add 添加页面信息
    * @Param: [cmsPage]
    * @return: com.xuecheng.framework.domain.cms.response.CmsPageResult
    * @Author: LJJ
    * @Date: 2020/1/21 19:43
    */
    public CmsPageResult add(CmsPage cmsPage) {
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
                cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        // 抛出 自定义异常
        if (cmsPage1 != null) {
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        // 先设置cmsPage主键为空，插入到mongodb时自动生成
        cmsPage.setPageId(null);
        CmsPage save = cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, save);
    }

    /**
    * @Description: findById 通过id查询页面信息
    * @Param: [id]
    * @return: com.xuecheng.framework.domain.cms.CmsPage
    * @Author: LJJ
    * @Date: 2020/1/22 9:08
    */
    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
    * @Description: edit 通过id和CmsPage更新页面信息
    * @Param: [id, cmsPage]
    * @return: com.xuecheng.framework.domain.cms.response.CmsPageResult
    * @Author: LJJ
    * @Date: 2020/1/22 9:08
    */
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        CmsPage one = this.findById(id);
        if (one != null) {
            one.setTemplateId(cmsPage.getTemplateId());
            one.setSiteId(cmsPage.getSiteId());
            one.setPageName(cmsPage.getPageName());
            one.setPageAliase(cmsPage.getPageAliase());
            one.setPageWebPath(cmsPage.getPageWebPath());
            one.setDataUrl(cmsPage.getDataUrl());
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            CmsPage save = cmsPageRepository.save(one);
            if (save != null) {
                CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS, save);
                return cmsPageResult;
            }
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
    * @Description: delete 通过id删除页面信息
    * @Param: [id]
    * @return: com.xuecheng.framework.model.response.ResponseResult
    * @Author: LJJ
    * @Date: 2020/1/25 13:52
    */
    public ResponseResult delete(String id) {
        CmsPage cmsPage = this.findById(id);
        if (cmsPage != null) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
    * @Description: getPageHtml 调用子程序生成静态页面
    * @Param: [pageId]
    * @return: java.lang.String
    * @Author: LJJ
    * @Date: 2020/1/25 14:26
    */
    public String getPageHtml(String pageId) {
        // 获取页面模型信息
        Map model = this.getModelByPageId(pageId);
        if (model == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        // 获取页面模板
        String template = this.getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        // 执行静态化
        String html = generateHTML(template, model);
        if (StringUtils.isEmpty(html)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    /**
    * @Description: generateHTML 通过template和model生成html字符串
    * @Param: [template, model]
    * @return: java.lang.String
    * @Author: LJJ
    * @Date: 2020/1/25 14:21
    */
    public String generateHTML(String template, Map model) {
        try {
            // 生成配置类
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 模板加载器
            StringTemplateLoader templateLoader = new StringTemplateLoader();
            templateLoader.putTemplate("template", template);
            // 配置模板加载器
            configuration.setTemplateLoader(templateLoader);
            // 获取模板
            Template template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
    * @Description: getTemplateByPageId 通过id查询获取页面模板
    * @Param: [pageId]
    * @return: java.lang.String
    * @Author: LJJ
    * @Date: 2020/1/25 13:54
    */
    public String getTemplateByPageId(String pageId) {
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            // 页面信息为空
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOT_EXITS);
        }
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            //模板信息为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(templateId);
        if (byId.isPresent()) {
            CmsTemplate cmsTemplate = byId.get();
            // 获取模板文件id templateFileId
            String templateFileId = cmsTemplate.getTemplateFileId();
            // 取出模板文件内容
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            // 打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            // 创建GridFSResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
    * @Description: getModelByPageId 获取页面模型数据
    * @Param: [pageId]
    * @return: java.util.Map
    * @Author: LJJ
    * @Date: 2020/1/25 14:17
    */
    public Map getModelByPageId(String pageId) {
        // 查询页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOT_EXITS);
        }
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        // 通过restTemplate获取模型信息
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    /**
     * @Description: postPage CMS系统管理端，用户点击发布页面按钮，对应的controller调用该service方法
     *
     * @param pageId
     * @return: com.xuecheng.framework.model.response.ResponseResult
     * @Author: LJJ
     * @Date: 2020/1/26 17:08
     */
    public ResponseResult postPage(String pageId) {
        // 执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        // 保存静态文件
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        // 发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * @Description: sendPostPage 发送消息给Rabbitmq
     *
     * @param pageId
     * @return: void
     * @Author: LJJ
     * @Date: 2020/1/26 17:01
     */
    private void sendPostPage(String pageId) {
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOT_EXITS);
        }
        Map<String, String> map = new HashMap<>();
        map.put("pageId", pageId);
        // 将map转换为json
        String msg = JSON.toJSONString(map);
        // 获取站点id作为routeKey
        String routeKey = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, routeKey, msg);
    }

    /**
     * @Description: saveHtml 保存静态文件内容
     *
     * @param pageId
      * @param content
     * @return: com.xuecheng.framework.domain.cms.CmsPage
     * @Author: LJJ
     * @Date: 2020/1/26 16:53
     */
    public CmsPage saveHtml(String pageId, String content) {
        // 查询页面
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOT_EXITS);
        }
        CmsPage cmsPage = optional.get();
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotEmpty(htmlFileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存html文件到GridFS
        InputStream inputStream = IOUtils.toInputStream(content);
        ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        String fileId = objectId.toString();
        // 将文件id保存到cmsPage
        cmsPage.setHtmlFileId(fileId);
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

}
