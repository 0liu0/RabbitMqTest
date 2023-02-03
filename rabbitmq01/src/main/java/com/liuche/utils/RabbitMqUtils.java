package com.liuche.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqUtils {
    public static Connection getConnection() throws IOException, TimeoutException {
        // 创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 在工厂对象中设置MQ的连接信息 （ip,port,vhost,username,password）
        factory.setHost("192.168.255.128");
        factory.setPort(5672);
        factory.setVirtualHost("/test01");
        factory.setUsername("liushuai");
        factory.setPassword("root");
        // 通过工厂获得与MQ的连接
        return factory.newConnection();
    }
    // 测试链接是否成功
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = getConnection();
        System.out.println(connection);
        connection.close();
    }
}
