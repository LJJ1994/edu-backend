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
 * @Description: Routing模式
 * @Create: 2020-01-26 12:04:04
 * @Modified By:
 */
public class ProduceRouting {
    private static final String QUEUE_INFO_EMAIL = "queue_info_email";
    private static final String QUEUE_INFO_SMS = "queue_info_sms";
    private  static final String ROUTING_KEY_EMAIL = "routing_key_email";
    private  static final String ROUTING_KEY_SMS = "routing_key_sms";
    private static final String ROUTING_KEY_INFORM = "routing_key_inform";
    private static final String EXCHANGE_ROUTING_INFORM = "exchange_routing_inform";

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
            channel.exchangeDeclare(EXCHANGE_ROUTING_INFORM, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(QUEUE_INFO_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFO_SMS, true, false, false, null);

            // 交换机与队列绑定，由于不需要路由键，设置其为空串
            channel.queueBind(QUEUE_INFO_SMS, EXCHANGE_ROUTING_INFORM, ROUTING_KEY_SMS);
            channel.queueBind(QUEUE_INFO_EMAIL, EXCHANGE_ROUTING_INFORM, ROUTING_KEY_EMAIL);
            channel.queueBind(QUEUE_INFO_SMS, EXCHANGE_ROUTING_INFORM, ROUTING_KEY_INFORM);
            channel.queueBind(QUEUE_INFO_EMAIL, EXCHANGE_ROUTING_INFORM, ROUTING_KEY_INFORM);

            for (int i=0; i < 5; i++) {
                String informMessage = "Send inform message to user ";

                // 消息发布
                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTING_KEY_INFORM,null, informMessage.getBytes("utf-8"));
                System.out.println("the inform message is: " + informMessage);
            }
//            for (int i=0; i < 5; i++) {
//                String smsMessage = "Send sms message to user ";
//
//                // 消息发布
//                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTING_KEY_SMS, null, smsMessage.getBytes("utf-8"));
//                System.out.println("the sms message is: " + smsMessage);
//            }
//            for (int i=0; i < 5; i++) {
//                String emailMessage = "Send email message to user ";
//
//                // 消息发布
//                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTING_KEY_EMAIL, null, emailMessage.getBytes("utf-8"));
//                System.out.println("the email message is: " + emailMessage);
//            }
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        } finally {
            assert channel != null;
            channel.close();
            connection.close();
        }
    }
}
