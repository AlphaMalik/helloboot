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
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.net.ServerSocket;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * @author kameshs
 */
@RestController
public class HelloBootController {
    private ServerSocket liveNessProbeSocket;
    
    class SocketConnectionAccepter implements Runnable {
        private ServerSocket probeSocket;
        
        SocketConnectionAccepter(ServerSocket serverSocket){
            this.probeSocket = serverSocket;
        }
        
        @Override
        public void run(){
            try {
                while (true) {
                    System.out.println("Accepting request");
                    this.probeSocket.accept();
                }
            } catch (Exception e) {
            }
        }
    }
        
      @GetMapping("/connecttoftpserver")
      public String setupJsch() throws JSchException {
            System.out.println("Entered setupJsch");
            JSch jsch = new JSch();
            jsch.addIdentity("/config/secret/ssh-privatekey");
            com.jcraft.jsch.Session jschSession = jsch.getSession("writadmin_test_sftp", "38.111.98.35", 2024);
            

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);
            jsch.setKnownHosts("/config/known.hosts");
            jschSession.connect();
            ChannelSftp ret = (ChannelSftp) jschSession.openChannel("sftp");
            System.out.println("Exited setupJsch");
            return "Successfully connected";
     }
    
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/whereami")
    public String whereami(@Value("${message.prefix}") String prefix) {
        String resp = String.format("%s from %s", prefix, System.getenv().getOrDefault("HOSTNAME", "localhost"));
        return resp;
    }
    
    @GetMapping("/opensocketanis")
    public String openSocketAnis() throws Exception {
        try {
			liveNessProbeSocket = new ServerSocket(6088);
            Thread newThread = new Thread(new SocketConnectionAccepter(liveNessProbeSocket));
            newThread.start();  
		} catch (IOException e) {
			throw e;
		}
        
        return "Created socket Anis style";
    }
    
    @GetMapping("/opensockettv")
    public String openSocketTV() throws Exception {
        try {
			liveNessProbeSocket = new ServerSocket(6066);
		} catch (IOException e) {
			throw e;
		}
        
        return "Created socket TV Style";
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
        
        HttpHost proxy = new HttpHost(System.getenv("http.proxyHost"), Integer.parseInt(System.getenv("http.proxyPort")), "http");
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        hcBuilder.setRoutePlanner(routePlanner);

        CloseableHttpClient httpClient = hcBuilder.build();

        try {

            HttpGet request = new HttpGet("https://dev.teranet-onland.ets.net/wp-json/olforms/v1/forms?form_post_id=9498&start=2020-01-01&end=2020-01-31");
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
