package io.aiven.klaw.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationModel implements Serializable {

  private String contactFormSubject;

  private String contactFormMessage;
}
