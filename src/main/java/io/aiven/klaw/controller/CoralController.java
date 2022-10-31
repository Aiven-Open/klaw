package io.aiven.klaw.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@ConditionalOnProperty(name = "klaw.coral.enabled", havingValue = "true")
@Controller
@RequestMapping("/coral")
@Slf4j
public class CoralController {

  public static final String CORAL_INDEX = "coral/index";

  @RequestMapping(value = "/{path:[^\\.]*}", method = RequestMethod.GET)
  public String rootPattern(@PathVariable String path) {
    log.info("Requested path {}", path);
    return CORAL_INDEX;
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String rootPath() {
    return CORAL_INDEX;
  }
}
