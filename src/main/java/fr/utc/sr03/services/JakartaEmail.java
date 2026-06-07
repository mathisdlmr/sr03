package fr.utc.sr03.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class JakartaEmail {

    @Value("${mail.mode:dev}")
    private String mailMode; // "dev" = pour print en console, "prod" = envoie de mail classique

    @Value("${mail.smtp.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${mail.smtp.port:587}")
    private String smtpPort;

    @Value("${mail.smtp.user:}")
    private String smtpUser;

    @Value("${mail.smtp.password:}")
    private String smtpPassword;

    @Value("${mail.from:nepasrepondre@utc.fr}")
    private String fromAddress;

    public void sendMail(String mail, String subject, String content) {
        if ("dev".equalsIgnoreCase(mailMode)) {
            System.out.println(" --------------- EMAIL (mode dev) ---------------");
            System.out.println("To : " + mail);
            System.out.println("Subject : " + subject);
            System.out.println("Content : " + content);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=UTF-8");
            Transport.send(message);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email à " + mail + " : " + e.getMessage());
        }
    }
}
