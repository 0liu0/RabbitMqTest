package com.liuche.mq01.consume;

import com.liuche.utils.RabbitMqUtils;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class consumer01 {
    public static void main(String[] args) throws IOException, TimeoutException {
        // 1.获取连接
        Connection connection = RabbitMqUtils.getConnection();
        // 2.创建信道channel
        Channel channel = connection.createChannel();
        // 3.推送的消息如何进行消费者的接口回调
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String msg = new String(delivery.getBody());
            System.out.println(msg);
        };
        // 4.取消消费的一个回调接口 如在消费的时候被队列删除掉了
        CancelCallback cancelCallback = consumerTag -> System.out.println("消息被拦截！");

        /*
         * 消费者消费消息
         * 1.消费哪个队列
         * 2.消费成功之后是否要自动应答 true 代表自动应答 false 手动应答
         * 3.消费者未成功消费的回调
         */
        channel.basicConsume("queue1",true,deliverCallback,cancelCallback);
    }
}
