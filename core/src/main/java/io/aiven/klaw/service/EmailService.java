package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.DEFAULT_TENANT_ID;
import static io.aiven.klaw.helpers.KwConstants.EMAIL_NOTIFICATIONS_ENABLED_KEY;

import io.aiven.klaw.config.ManageDatabase;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

  private static final String KW_LOGO_PNG =
      "https://klaw-project.io/wp-content/uploads/2021/01/KW-logo-gold-sm.png";

  private static String headerString =
      "<html><table style=\"height:40px;width:60%;background-color:#016BA7;color:white;text-align:center;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "\t<tr>\n"
          + "\t\t\n"
          + "\t\t<td>\n"
          + "\t\t\t<img src=\""
          + "LOGO"
          + "\"></img>\n"
          + "\t\t</td>\n"
          + "\t</tr>\n"
          + "</table><br>\n"
          + "\n"
          + "<table style=\"width:60%;color:#016BA7;text-align:left;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "\t<tr>\n"
          + "\t\t<td>";

  private static final String footerString =
      "<br>\n"
          + "<table style=\"height:40px; width:60%;background-color:#016BA7;color:white;text-align:center;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "\t<tr>\n"
          + "\t\t<td>\n"
          + "\t\t\t© 2023 <a href=\"https://klaw-project.io\" style=\"color:white;\">www.klaw-project.io</a>\n"
          + "\t\t</td>\n"
          + "\t</tr>\n"
          + "</table>\n"
          + "<table style=\"height:40px; width:60%;background-color:#016BA7;color:white;text-align:center;font-family: Arial, Helvetica, sans-serif; margin-left: auto;margin-right: auto;\">\n"
          + "</table></html>";
  @Autowired private JavaMailSender emailSender;

  @Autowired ManageDatabase manageDatabase;

  @Autowired public SimpleMailMessage template;

  @Value("${spring.mail.frommailid:info@klaw-project.io}")
  private String fromMailId;

  @Value("${spring.mail.noreplymailid:info@klaw-project.io}")
  private String noReplyMailId;

  @Value("${spring.mail.noreplymailid.display:'Klaw No Reply'}")
  private String noReplyMailIdDisplay;

  @Value("${klaw.notification.header.logo:http://yourcompany/logo.png}")
  private String notificationHeaderLogo;

  public void sendSimpleMessage(
      String to, String cc, String subject, String text, int tenantId, String loginUrl) {
    List<String> toList = new ArrayList<>();
    List<String> ccList = new ArrayList<>();
    CollectionUtils.addIgnoreNull(toList, to);
    CollectionUtils.addIgnoreNull(ccList, cc);
    sendSimpleMessage(toList, ccList, null, subject, text, tenantId, loginUrl);
  }

  @Async("notificationsThreadPool")
  public void sendSimpleMessage(
      List<String> to,
      List<String> cc,
      List<String> bcc,
      String subject,
      String text,
      int tenantId,
      String loginUrl) {
    String emailNotificationsEnabled =
        manageDatabase.getKwPropertyValue(EMAIL_NOTIFICATIONS_ENABLED_KEY, DEFAULT_TENANT_ID);
    try {
      MimeMessage message = emailSender.createMimeMessage();
      addEmailRecipientsToEmail(to, message, Message.RecipientType.TO);
      addEmailRecipientsToEmail(cc, message, Message.RecipientType.CC);
      addEmailRecipientsToEmail(bcc, message, Message.RecipientType.BCC);

      message.setSubject(subject);
      Address address = new InternetAddress(noReplyMailId);
      message.setReplyTo(new Address[] {address});
      message.setFrom(new InternetAddress(fromMailId, noReplyMailIdDisplay));

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

  private static void addEmailRecipientsToEmail(
      List<String> addresses, MimeMessage message, Message.RecipientType recipientType)
      throws MessagingException {
    if (addresses != null && !addresses.isEmpty()) {
      for (String address : addresses) {
        if (log.isDebugEnabled()) {
          log.debug("Add {} to recipientType {}", address, recipientType);
        }
        message.addRecipients(recipientType, address);
      }
    }
  }

  public void updateHeaderText() {
    if (notificationHeaderLogo != null) {
      if (notificationHeaderLogo.equals("http://yourcompany/logo.png")) {
        notificationHeaderLogo = KW_LOGO_PNG;
      }
    } else {
      notificationHeaderLogo = KW_LOGO_PNG;
    }

    headerString = headerString.replace("LOGO", notificationHeaderLogo);
  }
}
