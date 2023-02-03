package com.liuche.mq.provide;

import com.liuche.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class provider02 {
    public static void main(String[] args) throws IOException, TimeoutException {
        // 1.获取链接信息
        Connection connection = RabbitMqUtils.getConnection();
        // 2.得到信道channel
        Channel channel = connection.createChannel();
        // 3.创建消息队列(1,2,3,4,5)
        /* TODO
            参数1:队列的名称
            参数2:队列中的数据是否持久化
            参数3:是否排外（是否支持扩展，当前队列只能自己用，不能给别人用）
            参数4:是否自动删除（当队列的连接数为0时，队列会销毁，不管队列是否还存保存数据）
            参数5:队列参数（没有参数为null）
        */
        channel.queueDeclare("queue2", false, false, false, null);
        // 从终端输入信息然后发送
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String message = scanner.next();
            if (message.equals("exit")) {
                channel.close();
                connection.close();
            }
            channel.basicPublish("","queue2",null,message.getBytes());
            System.out.println("发送消息完成:"+message);
        }
    }
}
