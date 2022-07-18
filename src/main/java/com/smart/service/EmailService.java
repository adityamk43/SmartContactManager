package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	
	 public boolean sendEmail(String subject, String message, String to) {

	        // rest of the code..

	        boolean f=false;


	        String from = "adityamk43@gmail.com";

	        // variable for gmail
	        String host = "smtp.gmail.com";
	        // String host = "smtp-relay.gmail.com";

	        // get the system properties
	        Properties properties = System.getProperties();

	        // setting important information to properties object

	        // host set
	        properties.put("mail.smtp.auth", "true");
//	        properties.put("mail.smtp.starttls.enable", "true");
	        properties.put("mail.smtp.host", host);
	        properties.put("mail.smtp.port", "465");
	        properties.put("mail.smtp.ssl.enable", "true");

	        // Step 1:to get the session object..
	        Session session = Session.getInstance(properties, new Authenticator() {

	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication("adityamk43@gmail.com", "xatoacxbfiszvoqw");
	            }
	        });

	        session.setDebug(true);
	        // Step 2: compose the message [text, multi media]
	        MimeMessage m = new MimeMessage(session);

	        try {

	            // from email
	            m.setFrom(from);

	            // adding recipient to message
	            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	            // adding subject to message
	            m.setSubject(subject);

	            // adding text to message
//	            m.setText(message);
	            m.setContent(message, "text/html");

	            // send

	            // Step 3: send the message using Transport class
	            Transport.send(m);
	            

	            System.out.println("Sent Success................");

	            f=true;

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return f;
	    }
	
	
	
	
	
	
	
	
	
	

//	@Autowired
//	private JavaMailSender mailSender;
//	
//	public boolean sendEmail(String subject, String message, String to)
//	{
//		
//		boolean f=false;
//		
//		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//		
//		simpleMailMessage.setFrom("adityamk43@gmail.com");
//		simpleMailMessage.setSubject(subject);
//		simpleMailMessage.setTo(to);
//		simpleMailMessage.setText(message);
//		
//		mailSender.send(simpleMailMessage);
//		f=true;
//		
//		return f;
//	}

}
