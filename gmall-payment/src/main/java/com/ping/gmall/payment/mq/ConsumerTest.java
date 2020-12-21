package com.ping.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ConsumerTest {

    public static void main(String[] args) throws JMSException {
                 /*
        1.  创建连接工厂
        2.  创建连接
        3.  打开连接
        4.  创建session
        5.  创建队列
        6.  创建消息消费者
        7.  消费消息
         */
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://192.168.234.131:61616");
        Connection connection = activeMQConnectionFactory.createConnection();
        // 第一个参数：是否开启事务
        // 第二个参数：表示开启/关闭事务的相应配置参数，
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue ping = session.createQueue("ping-true");
        MessageConsumer consumer = session.createConsumer(ping);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                if (message instanceof TextMessage){
                    try {
                        String text = ((TextMessage) message).getText();
                        System.out.println(text);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }


                }
            }
        });




    }
}
