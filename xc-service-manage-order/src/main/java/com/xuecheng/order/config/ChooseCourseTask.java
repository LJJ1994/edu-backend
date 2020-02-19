package com.xuecheng.order.config;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-02-19 15:20:20
 * @Modified By:
 */
@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

//    @Scheduled(cron = "0/3 * * * * *")
//    public void test1() {
//        LOGGER.info("++++++++++++开始测试任务1+++++++++++++++++");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        LOGGER.info("+++++++++++++结束测试任务1++++++++++++++++");
//    }
//
//    @Scheduled(cron = "0/3 * * * * *")
//    public void test2() {
//        LOGGER.info("++++++++++++开始测试任务2+++++++++++++++++");
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        LOGGER.info("+++++++++++++结束测试任务2++++++++++++++++");
//    }

    // 每隔一分钟向mq发送消息
//    @Scheduled(fixedDelay = 60*1000)
    @Scheduled(cron = "0/10 * * * * *")
    public void sendChooseCourseTask() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE, -1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(time, 20);

        for(XcTask xcTask:taskList) {
            String taskId = xcTask.getId();
            int version = xcTask.getVersion();
            if (taskService.getTask(taskId, version)>0) {
                taskService.publish(xcTask, xcTask.getMqExchange(), xcTask.getMqRoutingkey());
                LOGGER.info("send choose course task id: {}", taskId);
            }
        }
    }

    // 接收选课结果
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveChoosecourseTask(XcTask xcTask) {
        LOGGER.info("receiveChoosecourseTask...{}",xcTask.getId());
        String id = xcTask.getId();
        taskService.finishTask(id);
    }
}
