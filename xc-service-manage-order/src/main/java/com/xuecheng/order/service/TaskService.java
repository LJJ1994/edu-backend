package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-19 15:46:46
 * @Modified By:
 */
@Service
public class TaskService {
    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * @Description: findTaskList 取回指定时间之前的n条记录
     * @param updateTime task表中的更新时间
     * @param n 记录数
     * @return: java.util.List<com.xuecheng.framework.domain.task.XcTask>
     */
    public List<XcTask> findTaskList(Date updateTime, int n) {
        Pageable pageable = new PageRequest(0, n);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks.getContent();
    }

    /**
     * @Description: publish 发布添加选课消息给mq
     * @param xcTask
     * @param ex
     * @param key
     * @return: void
     */
    @Transactional
    public void publish(XcTask xcTask, String ex, String key) {
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if (optional.isPresent()) {
            XcTask one = optional.get();
            String mqExchange = one.getMqExchange();
            String mqRoutingkey = one.getMqRoutingkey();
            rabbitTemplate.convertAndSend(mqExchange, mqRoutingkey, xcTask);
            // 更新时间
            one.setUpdateTime(new Date());
            xcTaskRepository.save(one);
        }
    }

    @Transactional
    public int getTask(String taskId, int version) {
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }

    // 删除任务
    @Transactional
    public void finishTask(String taskId) {
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if (taskOptional.isPresent()) {
            XcTask xcTask = taskOptional.get();
            xcTask.setDeleteTime(new Date());
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }

    // 向xc_task表中插入选课记录
    @Transactional
    public void saveXcTask(XcTask xcTask) {
        try {
            xcTaskRepository.save(xcTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
