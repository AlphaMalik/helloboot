package com.example.helloboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

/**
 * @author kameshs
 */
@RestController
public class HelloBootController {

    @GetMapping("/whereami")
    public String whereami(@Value("${message.prefix}") String prefix) {
        String resp = String.format("%s from %s", prefix, System.getenv().getOrDefault("HOSTNAME", "localhost"));
        return resp;
    }
    
    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping("/loadfile")
    public String loadfile() throws Exception {
        Properties properties = new Properties();
		InputStream inputStream = new FileInputStream("/config/test.properties");
		try {
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			try {
				properties.load(reader);
			} finally {
				reader.close();
			}
		} finally {
			inputStream.close();
		} 
		
		return properties.getProperty("test");        
    }
    
    
    @GetMapping("/testactivemq")
    public String testactivemq() throws Exception {
        Connection connection = null;
      InitialContext initialContext = null;
      try {
          Properties jndiProps = new Properties();
          jndiProps.put("java.naming.factory.initial","org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
          jndiProps.put("connectionFactory.ConnectionFactory","tcp://ex-aao-hdls-svc.amq.svc.cluster.local:61616?sslEnabled=false");
          jndiProps.put("queue.queue/exampleQueue","anisnotifications");

          
         // Step 1. Create an initial context to perform the JNDI lookup.
         initialContext = new InitialContext(jndiProps);
         
         // Step 2. Perform a lookup on the queue
         Queue queue = (Queue) initialContext.lookup("queue/exampleQueue");

         
         // Step 3. Perform a lookup on the Connection Factory
         ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

         // Step 4.Create a JMS Connection
         connection = cf.createConnection();

         // Step 5. Create a JMS Session
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         // Step 6. Create a JMS Message Producer
         MessageProducer producer = session.createProducer(queue);

         // Step 7. Create a Text Message
         TextMessage message = session.createTextMessage("This is a text message");

         System.out.println("Sent message: " + message.getText());

         // Step 8. Send the Message
         producer.send(message);

         // Step 9. Create a JMS Message Consumer
         MessageConsumer messageConsumer = session.createConsumer(queue);

         // Step 10. Start the Connection
         connection.start();

         // Step 11. Receive the message
         TextMessage messageReceived = (TextMessage) messageConsumer.receive(5000);

         System.out.println("Received message: " + messageReceived.getText());
      } finally {
         // Step 12. Be sure to close our JMS resources!
         if (initialContext != null) {
            initialContext.close();
         }
         if (connection != null) {
            connection.close();
         }
      }
		
		return "successfully tested";        
    }

}
