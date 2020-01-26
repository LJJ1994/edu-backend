package com.xuecheng.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: Rabbitmq配置类
 * @Create: 2020-01-26 13:47:47
 * @Modified By:
 */
@Configuration
public class RabbitmqConfig {
    public static final String QUEUE_INFO_EMAIL = "queue_info_email";
    public static final String QUEUE_INFO_SMS = "queue_info_sms";
    public static final String TOPICS_KEY_EMAIL = "inform.#.email.#";
    public static final String TOPICS_KEY_SMS = "inform.#.sms.#";
    public static final String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";
    
    /**
     * @Description: 交换机配置
     *  durable() true为持久化
     * @param 	
     * @return: org.springframework.amqp.core.Exchange
     * @Author: LJJ
     * @Date: 2020/1/26 13:49
     */
    @Bean(EXCHANGE_TOPICS_INFORM)
    public Exchange EXCHANGE_TOPICS_INFORM() {
        return ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_INFORM).durable(true).build();
    }

    /**
     * @Description: 声明SMS队列
     *
     * @param
     * @return: org.springframework.amqp.core.Queue
     * @Author: LJJ
     * @Date: 2020/1/26 13:52
     */
    @Bean(QUEUE_INFO_SMS)
    public Queue QUEUE_INFO_SMS() {
        return new Queue(QUEUE_INFO_SMS);
    }

    /**
     * @Description: 声明EMAIL队列
     *
     * @param
     * @return: org.springframework.amqp.core.Queue
     * @Author: LJJ
     * @Date: 2020/1/26 13:52
     */
    @Bean(QUEUE_INFO_EMAIL)
    public Queue QUEUE_INFO_EMAIL() {
        return new Queue(QUEUE_INFO_EMAIL);
    }

    /**
     * @Description: 绑定SMS, EMAIL队列到交换机
     *
     * @param queue	
     * @param exchange
     * @return: org.springframework.amqp.core.Binding
     * @Author: LJJ
     * @Date: 2020/1/26 13:55
     */
    @Bean
    public Binding BINDING_SMS_QUEUE_TO_EXCHANGE(@Qualifier(QUEUE_INFO_SMS) Queue queue,
                                             @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TOPICS_KEY_SMS).noargs();
    }

    @Bean
    public Binding BINDING_EMAIL_QUEUE_TO_EXCHANGE(@Qualifier(QUEUE_INFO_EMAIL) Queue queue,
                                             @Qualifier(EXCHANGE_TOPICS_INFORM) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(TOPICS_KEY_EMAIL).noargs();
    }
}
