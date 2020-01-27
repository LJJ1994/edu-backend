package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseTeachplanRepository extends JpaRepository<Teachplan, String> {
    /**
     * @Description: 通过课程id和父节点id查询根节点列表
     *
     * @param courseId 课程id
     * @param parentId 父节点id
     * @return: java.util.List<com.xuecheng.framework.domain.course.ext.TeachplanNode>
     * @Author: LJJ
     * @Date: 2020/1/27 12:30
     */
    public List<Teachplan> findByCourseidAndParentid(String courseId, String parentId);
}
