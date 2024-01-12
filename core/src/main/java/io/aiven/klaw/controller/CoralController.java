package io.aiven.klaw.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@ConditionalOnProperty(name = "klaw.coral.enabled", havingValue = "true")
@Controller
@RequestMapping("/coral")
public class CoralController {

  public static final String CORAL_INDEX = "coral/index";
  public static final String CORAL_LOGIN = "coral/coral-login";

  @RequestMapping(value = "/index", method = RequestMethod.GET)
  public String index() {
    return CORAL_INDEX;
  }

  @RequestMapping(value = "/coral-login", method = RequestMethod.GET)
  public String login() {
    return CORAL_LOGIN;
  }
}
