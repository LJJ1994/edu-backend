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
 * @Description: Topics
 * @Create: 2020-01-26 12:04:04
 * @Modified By:
 */
public class ProduceTopics {
    private static final String QUEUE_INFO_EMAIL = "queue_info_email";
    private static final String QUEUE_INFO_SMS = "queue_info_sms";
    private  static final String TOPICS_KEY_EMAIL = "inform.#.email.#";
    private  static final String TOPICS_KEY_SMS = "inform.#.sms.#";
    private static final String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";

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
            channel.exchangeDeclare(EXCHANGE_TOPICS_INFORM, BuiltinExchangeType.TOPIC);
            channel.queueDeclare(QUEUE_INFO_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFO_SMS, true, false, false, null);

            // 交换机与队列绑定，由于不需要路由键，设置其为空串
            channel.queueBind(QUEUE_INFO_SMS, EXCHANGE_TOPICS_INFORM, TOPICS_KEY_SMS);
            channel.queueBind(QUEUE_INFO_EMAIL, EXCHANGE_TOPICS_INFORM, TOPICS_KEY_EMAIL);

            for (int i=0; i < 5; i++) {
                String informMessage = "Send sms and email message to user ";

                // 消息发布
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.email.sms",null, informMessage.getBytes("utf-8"));
                System.out.println("the inform message is: " + informMessage);
            }
            for (int i=0; i < 5; i++) {
                String smsMessage = "Send sms message to user ";

                // 消息发布
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.sms", null, smsMessage.getBytes("utf-8"));
                System.out.println("the sms message is: " + smsMessage);
            }
            for (int i=0; i < 5; i++) {
                String emailMessage = "Send email message to user ";

                // 消息发布
                channel.basicPublish(EXCHANGE_TOPICS_INFORM, "inform.email", null, emailMessage.getBytes("utf-8"));
                System.out.println("the email message is: " + emailMessage);
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
