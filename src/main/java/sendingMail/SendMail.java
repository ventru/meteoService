package sendingMail;

import config.Config;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendMail {

    public void sendReportToOperator(String[] statusForReport) {

        final Config config = new Config();
        config.readConfig();

        String buildMessage = "Warning message! - \n";
        for (int i = 0; i < statusForReport.length; i++) {
            buildMessage += statusForReport[i] + "\n";
        }
        buildMessage += "Details: \n";



        String mailbox = config.getMailForSend();

        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.mail.ru");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getMailLogin(), config.getMailPassword());
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("smithalex80@mail.ru"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(mailbox));
            message.setSubject("[Метео станция] Отчет о несоответствии норм!");
            message.setText((buildMessage).toString());

            Transport.send(message);
            System.out.println("Отправка отчета администратору.");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
