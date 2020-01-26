package com.xuecheng.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 发布/订阅模式中的sms订阅者
 * @Create: 2020-01-25 23:39:39
 * @Modified By:
 */
public class ConsumerSubscribeEmail {
    private static final String QUEUE_INFO_EMAIL = "queue_info_email";
    private static final String EXCHANGE_FANOUT_INFORM = "exchange_fanout_inform";

    public static void main(String[] args) {
        Connection connection = null;
        Channel channel = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("127.0.0.1");
            factory.setPort(5672);

            connection = factory.newConnection();
            channel = connection.createChannel();

            // 声明交换机
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            // 声明队列
            channel.queueDeclare(QUEUE_INFO_EMAIL, true, false, false, null);
            // 交换机与队列绑定
            channel.queueBind(QUEUE_INFO_EMAIL, EXCHANGE_FANOUT_INFORM, "");
            // 定义消费方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                /**
                 * @Description: 消费者接收消息时，此方法会被调用
                 *
                 * @param consumerTag 消费者的标签,在channel.basicConsume()指定
                 * @param envelope 消息包的内容，可以从中获取消息id，消息routerKey，交换机，消息和重传标志
                 * @param properties
                 * @param body
                 * @return: void
                 * @Author: LJJ
                 * @Date: 2020/1/25 22:36
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String exchange = envelope.getExchange();
                    String routeKey = envelope.getRoutingKey();
                    //消息id
                    long id = envelope.getDeliveryTag();
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("receive message: " + message);
                }
            };
            /**
             * @Description: 监听队列: String queue, boolean autoack, consumer callback
             * @param queue 队列名称
             * @param autoack 是否自动回复
             * @param callback 消费者回调函数
             * @return: void
             * @Author: LJJ
             * @Date: 2020/1/25 22:42
             */
            channel.basicConsume(QUEUE_INFO_EMAIL, true, defaultConsumer);
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }
}
