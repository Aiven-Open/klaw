package io.aiven.klaw.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan(basePackages = {"io.aiven.klaw"})
public class EmailConfig {

  @Value("${spring.mail.host:null}")
  private String smtpHost;

  @Value("${spring.mail.port:587}")
  private int smtpPort;

  @Value("${spring.mail.username:null}")
  private String smtpFromUsername;

  @Value("${spring.mail.password:null}")
  private String smtpFromPassword;

  @Value("${spring.mail.properties.mail.smtp.auth:null}")
  private String smtpAuthentication;

  @Value("${spring.mail.properties.mail.smtp.starttls.enable:null}")
  private String smtpTlsEnable;

  @Value("${spring.mail.properties.mail.transport.protocol:null}")
  private String mailProtocol;

  @Value("${spring.mail.properties.mail.debug:null}")
  private String smtpDebug;

  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    mailSender.setHost(smtpHost);
    mailSender.setPort(smtpPort);

    mailSender.setUsername(smtpFromUsername);
    mailSender.setPassword(smtpFromPassword);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", mailProtocol);
    props.put("mail.smtp.auth", smtpAuthentication);
    props.put("mail.smtp.starttls.enable", smtpTlsEnable);
    props.put("mail.debug", smtpDebug);

    return mailSender;
  }

  @Bean
  public SimpleMailMessage templateSimpleMessage() {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setText("This is the test email template for your email:\n%s\n");
    return message;
  }

  @Bean(name = "notificationsThreadPool")
  public ThreadPoolTaskExecutor getGetThreadPoolExecutor() {
    return new ThreadPoolTaskExecutor();
  }
}
