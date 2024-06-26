package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

public boolean sendEmail(String subject,String message,String to) {
        
        //variable for gmail
        boolean f=false;
        String from="varshil.patel@drcsystems.com";
        String host="smtp.gmail.com";
        
        //get the system properties
        Properties properties=System.getProperties();
        System.out.println("PROPERTIES "+properties);
        
        //setting important information to properties object
        //host set
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable","true");
        properties.put("mail.smtp.auth","true");
        
        //step 1: to get the session object
        Session session = Session.getInstance(properties, new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
            	return new PasswordAuthentication("varshil.patel@drcsystems.com", "gdqn qwwl gots qpmc");
            }
        });
        session.setDebug(true);
        
        //step 2: compose the message
        MimeMessage m=new MimeMessage(session);
        
        try {
            m.setFrom(from);
            m.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            m.setSubject(subject);
        //    m.setText(message);
            m.setContent(message,"text/html");
            Transport.send(m);
            System.out.println("sent success...........");
            f=true;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return f;    
    }
}
