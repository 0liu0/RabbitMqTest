package com.liuche.mq02;

import com.liuche.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class provider {
    private static final String QUEUE_NAME="queue03";
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = RabbitMqUtils.getConnection();
        Channel channel = connection.createChannel();
        // TODO 第二个参数 队列是否持久化？ 注意：重新定义为持久化后要把之前的删掉否则会冲突导致报错
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        Scanner scanner = new Scanner(System.in);
        String msg;
        while (scanner.hasNext()) {
            msg = scanner.next();
            // TODO MessageProperties.PERSISTENT_TEXT_PLAIN 这个参数可以实现消息持久化(不是很完美可能还有数据丢失详细解决版发在后面)
            channel.basicPublish("",QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN,msg.getBytes(StandardCharsets.UTF_8));
            System.out.println("发送消息完成："+msg);
        }

    }
}
