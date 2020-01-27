package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-01-27 15:47:47
 * @Modified By:
 */
@Service
public class CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    public CategoryNode findList() {
        return categoryMapper.selectList();
    }
}
