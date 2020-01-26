package com.xuecheng.rabbitmq.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.rabbitmq.config.RabbitmqConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description:
 * @Create: 2020-01-26 14:35:35
 * @Modified By:
 */
@Component
public class ReceiveHandler {
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFO_EMAIL})
    public void receive_email(String msg, Message message, Channel channel) {
//        System.out.println(msg);
        System.out.println(message.getBody());
    }
}
