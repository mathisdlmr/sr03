package fr.utc.sr03.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class JakartaEmail {

    public JakartaEmail() {}

    public void sendMail(String mail, String subject, String content) {
        String to = mail;
        String from = "nepasrepondre@utc.fr";
        String host = "smtp1.utc.fr";

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "25");
        props.put("mail.debug", "false");

        Session session = Session.getInstance(props, null);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println(e.toString());
        }
    }
}
