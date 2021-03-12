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
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author kameshs
 */
@RestController
public class HelloBootController {

    @CrossOrigin(origins = "*", allowedHeaders = "*")
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
    
    @GetMapping("/testInternetConnection")
    public String executeSuccessfulCall() throws Exception {
        String result = "";
        HttpClientBuilder hcBuilder = HttpClients.custom();
        
        HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")), "http");
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        hcBuilder.setRoutePlanner(routePlanner);

        CloseableHttpClient httpClient = hcBuilder.build();

        try {

            HttpGet request = new HttpGet("http://www.recipepuppy.com/api/?i=onions,garlic&q=omelet&p=3);
            CloseableHttpResponse response = httpClient.execute(request);

            try {

                // Get HttpResponse Status
                System.out.println(response.getStatusLine().getStatusCode());   // 200

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // return it as a String
                    result = EntityUtils.toString(entity);
                    System.out.println(result);
                }

            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
        
        return result;
	}
    
    
    @GetMapping("/testactivemqsend")
    public String testactivemqsend() throws Exception {
        Connection connection = null;
      InitialContext initialContext = null;
      try {
          Properties jndiProps = new Properties();
          jndiProps.put("java.naming.factory.initial","org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
          jndiProps.put("connectionFactory.ConnectionFactory","tcp://172.28.134.60:30616?sslEnabled=false");
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
         TextMessage message = session.createTextMessage("Message created at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));

         System.out.println("Sent message: " + message.getText());

         // Step 8. Send the Message
         producer.send(message);

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
    
    
    @GetMapping("/testactivemqconsume")
    public String testactivemqconsume() throws Exception {
        String responseFromActiveMQ = "Default message";
        Connection connection = null;
      InitialContext initialContext = null;
      try {
          Properties jndiProps = new Properties();
          jndiProps.put("java.naming.factory.initial","org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
          jndiProps.put("connectionFactory.ConnectionFactory","tcp://172.28.134.60:30616?sslEnabled=false");
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

         // Step 9. Create a JMS Message Consumer
         MessageConsumer messageConsumer = session.createConsumer(queue);

         // Step 10. Start the Connection
         connection.start();

         // Step 11. Receive the message
         TextMessage messageReceived = (TextMessage) messageConsumer.receive(5000);

         System.out.println("Received message: " + messageReceived.getText());
         responseFromActiveMQ = messageReceived.getText();
      } finally {
         // Step 12. Be sure to close our JMS resources!
         if (initialContext != null) {
            initialContext.close();
         }
         if (connection != null) {
            connection.close();
         }
      }
		
		return responseFromActiveMQ;        
    }

}
