package com.xuecheng.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 发布/订阅模式下的发布者
 * @Create: 2020-01-25 23:31:31
 * @Modified By:
 */
public class ProducerPublish {
    private static final String QUEUE_INFO_EMAIL = "queue_info_email";
    private static final String QUEUE_INFO_SMS = "queue_info_sms";
    private static final String EXCHANGE_FANOUT_INFORM = "exchange_fanout_inform";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("127.0.0.1");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");
            // rabbitmq默认虚拟机为"/", 虚拟机相当于一个独立的服务
            factory.setVirtualHost("/");

            //创建与rabbitmq服务的TCP连接
            connection = factory.newConnection();
            // 创建与exchange交换器的连接，每个连接可以创建多个通道，每个通道代表一个会话任务
            channel = connection.createChannel();
            // 声明交换机
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            channel.queueDeclare(QUEUE_INFO_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFO_SMS, true, false, false, null);

            // 交换机与队列绑定，由于不需要路由键，设置其为空串
            channel.queueBind(QUEUE_INFO_SMS, EXCHANGE_FANOUT_INFORM, "");
            channel.queueBind(QUEUE_INFO_EMAIL, EXCHANGE_FANOUT_INFORM, "");
            for (int i=0; i < 5; i++) {
                String message = "Send message to user ";

                // 消息发布
                channel.basicPublish(EXCHANGE_FANOUT_INFORM, "", null, message.getBytes("utf-8"));
                System.out.println("the message is: " + message);
            }
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        } finally {
            assert channel != null;
            channel.close();
            connection.close();
        }
    }
}
