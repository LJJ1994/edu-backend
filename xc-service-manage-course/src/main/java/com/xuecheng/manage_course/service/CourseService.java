package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.manage_course.dao.TeachplanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 课程查询service层
 * @Create: 2020-01-26 22:54:54
 * @Modified By:
 */
@Service
public class CourseService{
    @Autowired
    TeachplanMapper teachplanMapper;
    
    /**
     * @Description: findTeachplanList 查询课程计划
     *
     * @param courseId	
     * @return: com.xuecheng.framework.domain.course.ext.TeachplanNode
     * @Author: LJJ
     * @Date: 2020/1/26 22:57
     */
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }
}
