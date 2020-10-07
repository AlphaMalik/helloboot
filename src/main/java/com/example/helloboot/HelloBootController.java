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

}
