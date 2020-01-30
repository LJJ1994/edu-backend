package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-01-30 20:35:35
 * @Modified By:
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite, String> {
}
