package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
* @Description: CmsPageRepository inteface
* @Author: LJJ
* @Date: 2020/1/21 8:37
* @Modified By:
*/
public interface CmsPageRepository extends MongoRepository<CmsPage, String> {
    // 根据页面名称查询
    CmsPage findByPageName(String pageName);

    // 根据页面名称和页面类型查询
    CmsPage findByPageNameAndPageType(String pageName, String pageType);

    // 根据站点和页面类型查询记录数
    int findBySiteIdAndPageType(String siteId, String pageType);

    // 根据站点和页面类型分页查询
    Page<CmsPage> findBySiteIdAndPageType(String siteId, String pageType, Pageable pageable);

    // 根据页面名称、站点id、webpath路径判断是否存在该页面
    public CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName, String siteId, String pageWebPath);
}
