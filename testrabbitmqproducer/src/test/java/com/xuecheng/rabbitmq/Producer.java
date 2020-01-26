package com.xuecheng.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 测试工程生产者程序
 * @Create: 2020-01-25 21:59:59
 * @Modified By:
 */
public class Producer {
    // 队列名称
    private static final String QUEUE = "helloworld";

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
            channel.queueDeclare(QUEUE, true, false, false, null);
            String message = "hello world! " + System.currentTimeMillis();

            // 消息发布
            channel.basicPublish("", QUEUE, null, message.getBytes("utf-8"));
            System.out.println("send message: " + message);
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        } finally {
            assert channel != null;
            channel.close();
            connection.close();
        }
    }
}
