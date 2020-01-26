package com.xuecheng.manage_cms.service;

import com.xuecheng.api.cms.CmsConfigControllerApi;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: CmsConfig Service
 * @Create: 2020-01-23 09:52:52
 * @Modified By:
 */
@Service
public class CmsConfigService {
    @Autowired
    CmsConfigRepository cmsConfigRepository;

    // 根据Id查询配置信息
    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
