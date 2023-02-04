package com.liuche.mq03;

import com.liuche.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeoutException;

/*TODO
 *   这个模块是发布确认练习
 *   1. 单个确认发布
 *   2. 批量确认发布
 *   3. 异步确认发布（性能最好，最重要）
 * */
public class Provider {
    private static final int TIMES = 1000;

    public static void main(String[] args) throws Exception {
        // 单个确认发布
        // publishMessageIndividually(); // 用时950ms
        // 批量确认发布
        // publishMessageBatch(); // 用时108ms
        // 异步确认发布
        publishMessageAsync(); // 用时45ms


    }

    public static void publishMessageIndividually() throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = RabbitMqUtils.getConnection();
        // 创建信道
        Channel channel = connection.createChannel();
        // 声明队列
        String QUEUE_NAME = UUID.randomUUID().toString();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // TODO 开启发布确认
        channel.confirmSelect();
        // 记录开始时间
        long start = System.currentTimeMillis();
        for (int i = 1; i <= TIMES; i++) {
            String msg = i + "";
            // 发布消息
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));
            // 服务端返回false或超时事件内未返回，生产者可以消息重发
            boolean flag = channel.waitForConfirms();
            if (flag) System.out.println("消息发送成功！");
        }
        long end = System.currentTimeMillis();
        System.out.println("发布" + TIMES + "个单独确认消息,耗时" + (end - start) + "ms");
        channel.close();
        connection.close();

    }

    public static void publishMessageBatch() throws Exception {
        Connection connection = RabbitMqUtils.getConnection();
        Channel channel = connection.createChannel();
        String QUEUE_NAME = UUID.randomUUID().toString();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //开启发布确认
        channel.confirmSelect();
        //批量确认消息大小
        int BATCHSIZE = 100;
        // 已发送消息的个数
        int confirmedCount = 0;
        long start = System.currentTimeMillis();
        for (int i = 1; i <= TIMES; i++) {
            String msg = i + "";
            // 发布消息
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));
            confirmedCount++;
            // 服务端返回false或超时事件内未返回，生产者可以消息重发
            if (confirmedCount == BATCHSIZE) {
                boolean flag = channel.waitForConfirms();
                if (flag) System.out.println("消息发送成功！");
                confirmedCount = 0;
            }
        }
        //为了确保还有剩余没有确认消息 再次确认
        if (confirmedCount > 0) {
            channel.waitForConfirms();
        }
        long end = System.currentTimeMillis();
        System.out.println("发布" + TIMES + "个单独确认消息,耗时" + (end - start) + "ms");
        channel.close();
        connection.close();
    }

    // TODO 异步发送只管发送，几下所有发送成功的消息，监听这些消息的发送，如果成功则删除这些成功消息的记录剩下的就是未成功的（RabbitMq自动再次进行发送）
    public static void publishMessageAsync() throws Exception {
        Connection connection = RabbitMqUtils.getConnection();
        Channel channel = connection.createChannel();
        String QUEUE_NAME = UUID.randomUUID().toString();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //TODO 开启发布确认
        channel.confirmSelect();
        /*TODO
         * 线程安全有序的一个哈希表，适用于高并发的情况
         * 1.轻松的将序号与消息进行关联
         * 2.轻松批量删除条目 只要给到序列号
         * 3.支持并发访问
         */
        ConcurrentSkipListMap<Long, String> outStandingConfirms = new ConcurrentSkipListMap<Long, String>(); // 记录发布的消息
        /*TODO
         * 确认收到消息的一个回调
         * 1.消息序列号
         * 2.true 可以确认小于等于当前序列号的消息
         * false 确认当前序列号消息
         */
        ConfirmCallback ackCallBack = (sequenceNumber, multiple) -> {
            if (multiple) {
                //返回的是小于等于当前序列号的未确认消息 是一个 map
                ConcurrentNavigableMap<Long, String> confirmed =
                        outStandingConfirms.headMap(sequenceNumber, true);
                //清除该部分未确认消息
                confirmed.clear();// 批量删除发送成功的消息
            } else {
                //只清除当前序列号的消息
                outStandingConfirms.remove(sequenceNumber); // 将返回确认的消息删除，这样记录的全是发送不成功的消息了
            }
            System.out.println(sequenceNumber + "成功发送！");
        };
        ConfirmCallback nackCallback = (sequenceNumber, multiple) -> {
            String message = outStandingConfirms.get(sequenceNumber); // 将已经发送的消息保存到这个map里
            System.out.println("发布的消息" + message + "未被确认，序列号" + sequenceNumber);
        };
        /*TODO
         * 添加一个异步确认的监听器
         * 1.确认收到消息的回调
         * 2.未收到消息的回调
         */
        channel.addConfirmListener(ackCallBack,nackCallback);
        long start = System.currentTimeMillis();
        for (int i = 1; i <= TIMES; i++) {
            String msg = i + "";
            /*TODO
             * channel.getNextPublishSeqNo()获取下一个消息的序列号
             * 通过序列号与消息体进行一个关联
             * 全部都是未确认的消息体
             */
            outStandingConfirms.put(channel.getNextPublishSeqNo(), msg);
            // 发布消息
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes(StandardCharsets.UTF_8));
//            Thread.sleep(1);
        }
        long end = System.currentTimeMillis();
        System.out.println("发布" + TIMES + "个单独确认消息,耗时" + (end - start) + "ms");
        channel.close();
        connection.close();
    }
}
