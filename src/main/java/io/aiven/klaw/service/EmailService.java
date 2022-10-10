package io.aiven.klaw.service;

import static io.aiven.klaw.service.KwConstants.DEFAULT_TENANT_ID;
import static io.aiven.klaw.service.KwConstants.EMAIL_NOTIFICATIONS_ENABLED_KEY;

import io.aiven.klaw.config.ManageDatabase;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

  @Autowired private JavaMailSender emailSender;

  @Autowired ManageDatabase manageDatabase;

  @Autowired public SimpleMailMessage template;

  @Value("${spring.mail.frommailid:info@klaw-project.io}")
  private String fromMailId;

  @Value("${spring.mail.noreplymailid:info@klaw-project.io}")
  private String noReplyMailId;

  private String headerString =
      "<html><table style=\"height:40px;width:60%;background-color:#016BA7;color:white;text-align:center;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "\t<tr>\n"
          + "\t\t\n"
          + "\t\t<td>\n"
          + "\t\t\t<img src=\"https://klaw-project.io/wp-content/uploads/2021/01/KW-logo-gold-sm.png\"></img>\n"
          + "\t\t</td>\n"
          + "\t</tr>\n"
          + "</table><br>\n"
          + "\n"
          + "<table style=\"width:60%;color:#016BA7;text-align:left;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "\t<tr>\n"
          + "\t\t<td>";

  private String footerString =
      "<br>\n"
          + "<table style=\"height:40px; width:60%;background-color:#016BA7;color:white;text-align:center;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "\t<tr>\n"
          + "\t\t<td>\n"
          + "\t\t\t© 2021 <a href=\"https://klaw-project.io\" style=\"color:white;\">www.klaw-project.io</a>\n"
          + "\t\t</td>\n"
          + "\t</tr>\n"
          + "</table>\n"
          + "<table style=\"height:40px; width:60%;background-color:#016BA7;color:white;text-align:center;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "\t<tr>\n"
          + "\t\t<td align=\"center\" valign=\"top\">\n"
          + "\t\t\t\n"
          + "\t\t\t<a href=\"https://www.linkedin.com/company/kafkawize/\"><img width=\"4%\" src=\"https://kafkawize.io/wp-content/uploads/2021/11/linkedin.png\"></a>  &nbsp;&nbsp;&nbsp;\n"
          + "\t\t\n"
          + "\t\t\t<a href=\"https://www.twitter.com/kafkawize/\"><img width=\"4%\" src=\"https://kafkawize.io/wp-content/uploads/2021/11/twitterf.png\"></a>\n"
          + "\t\t<td>\n"
          + "\t</tr>\n"
          + "</table></html>";

  @Async("notificationsThreadPool")
  public void sendSimpleMessage(
      String to, String cc, String subject, String text, int tenantId, String loginUrl) {

    String emailNotificationsEnabled =
        manageDatabase.getKwPropertyValue(EMAIL_NOTIFICATIONS_ENABLED_KEY, DEFAULT_TENANT_ID);
    try {
      MimeMessage message = emailSender.createMimeMessage();
      message.setRecipients(Message.RecipientType.TO, to);
      if (cc != null) {
        message.setRecipients(Message.RecipientType.CC, cc);
      }

      message.setSubject(subject);
      Address address = new InternetAddress(noReplyMailId);
      message.setReplyTo(new Address[] {address});
      message.setFrom(new InternetAddress(fromMailId, "Klaw NoReply"));

      text = text.replaceAll("\\\\n", "<br>");

      text =
          text
              + "<br><br>\n"
              + "<a href="
              + loginUrl
              + "><b>Login Now</b></a>\n"
              + "<br><br>\n"
              + "Thanks,<br>Klaw\n"
              + "\t\t</td>\n"
              + "\t</tr>\n"
              + "</table>";

      text = headerString + text + footerString;
      message.setContent(text, "text/html");

      if ("true".equals(emailNotificationsEnabled)) {
        try {
          CompletableFuture.runAsync(
                  () -> {
                    emailSender.send(message);
                  })
              .get();
        } catch (InterruptedException | ExecutionException e) {
          log.error("Exception:", e);
        }
      }

    } catch (MailException | MessagingException | UnsupportedEncodingException e) {
      log.error("Exception:", e);
    }
  }
}
