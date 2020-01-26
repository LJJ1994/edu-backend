package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
* @Description: CmsConfigRepository
* @Author: LJJ
* @Date: 2020/1/23 9:51
* @Modified By: 
*/
public interface CmsConfigRepository extends MongoRepository<CmsConfig, String> {
}
