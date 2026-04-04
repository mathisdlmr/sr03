package fr.utc.sr03.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;


import java.util.Properties;

public class JakartaEmail {

    public JakartaEmail() {
    }

    public void sendMail(){
        String to = "cedric.martinet@utc.fr";
        String from = "nepasrepondre@utc.fr";
        String host = "smtp1.utc.fr";

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "25");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, null);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Sujet");
            message.setContent("Contenu du mail", "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println(e.toString());
        }
    }



}
