package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
* @Description: TeachplanMapper
* @Author: LJJ
* @Date: 2020/1/26 22:35
* @Modified By:
*/
@Mapper
public interface TeachplanMapper {
    public TeachplanNode selectList(String courseId);
}
