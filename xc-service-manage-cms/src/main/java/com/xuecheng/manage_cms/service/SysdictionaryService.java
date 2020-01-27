package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-01-27 16:00:00
 * @Modified By:
 */
@Service
public class SysdictionaryService {
    @Autowired
    SysDictionaryDao sysDictionaryDao;

    public SysDictionary findDictionaryByType(String type) {
        return sysDictionaryDao.findByDType(type);
    }
}
