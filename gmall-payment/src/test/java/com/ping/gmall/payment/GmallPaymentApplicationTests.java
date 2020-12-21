package com.ping.gmall.payment;


import com.ping.gmall.config.ActiveMQUtil;
import com.ping.gmall.payment.mq.ProducerTest;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

    @Autowired
    private ActiveMQUtil activeMQUtil;
    @Test
    public void contextLoads() {
    }

    @Test
    public void testMQ() throws JMSException {
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

        Connection connection = activeMQUtil.getConnection();

        connection.start();
        // 第一个参数：是否开启事务
        // 第二个参数：表示开启/关闭事务的相应配置参数，
        Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue ping = session.createQueue("ping-tools");
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
