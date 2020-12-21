package com.ping.gmall.payment.mq;

import com.ping.gmall.config.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public class ProducerTest {
    @Autowired
    private ActiveMQUtil activeMQUtil;

    public static void main(String[] args) throws JMSException {
           /*
        1.  创建连接工厂
        2.  创建连接
        3.  打开连接
        4.  创建session
        5.  创建队列
        6.  创建消息提供者
        7.  创建消息对象
        8.  发送消息
        9.  关闭
         */
//        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.234.131:61616");
//        Connection connection = activeMQConnectionFactory.createConnection();
        Connection connection = new ProducerTest().activeMQUtil.getConnection();

        connection.start();
        // 第一个参数：是否开启事务
        // 第二个参数：表示开启/关闭事务的相应配置参数，
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue ping = session.createQueue("ping-true");
        MessageProducer producer = session.createProducer(ping);

        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("电脑电脑电脑电脑电脑电脑");
        producer.send(activeMQTextMessage);

        session.commit();
        producer.close();
        session.close();
        connection.close();


    }

}
