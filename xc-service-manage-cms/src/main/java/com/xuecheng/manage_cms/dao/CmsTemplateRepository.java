package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
* @Description: Cmstemplate inteface
* @Author: LJJ
* @Date: 2020/1/25 14:00
* @Modified By:
*/
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate, String> {
}
