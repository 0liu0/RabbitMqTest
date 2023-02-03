package com.liuche.mq02;

import com.liuche.utils.RabbitMqUtils;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;

public class consumer02 {
    private static final String QUEUE_NAME = "queue03";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = RabbitMqUtils.getConnection();
        Channel channel = connection.createChannel();
        System.out.println("这里是接收者二 较慢");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                sleep(30000); // 模拟延迟
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String msg = new String(delivery.getBody());
            System.out.println(msg);
            // TODO 手动提交
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        CancelCallback cancelCallback = consumerTag -> System.out.println("消息被拦截！");

        /*
         * 消费者消费消息
         * 1.消费哪个队列
         * TODO 2.消费成功之后是否要自动应答 true 代表自动应答 false 手动应答
         * 3.消费者未成功消费的回调
         */
        int prefetchCount = 1;
        channel.basicQos(prefetchCount); // TODO 这个将不在进行轮巡操作，谁有空谁就处理消息(注意：所有的消费者都要加)
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);
    }
}
