package io.aiven.klaw.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@ConditionalOnProperty(name = "klaw.coral.enabled", havingValue = "true")
@Controller
@Slf4j
public class CoralController {

  @RequestMapping(value = "/coral/{path:[^\\.]*}", method = RequestMethod.GET)
  public String root(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
    if (request.getRequestURI().endsWith(".js") || request.getRequestURI().endsWith(".css"))
      return request.getRequestURI();
    else return "coral/index";
  }

  @RequestMapping(value = "/coral/index.html", method = RequestMethod.GET)
  public String rootIndex(
      ModelMap model, HttpServletRequest request, HttpServletResponse response) {
    if (request.getRequestURI().endsWith(".js") || request.getRequestURI().endsWith(".css"))
      return request.getRequestURI();
    else return "coral/index";
  }

  @RequestMapping(value = "/coral/", method = RequestMethod.GET)
  public String rootPath(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
    if (request.getRequestURI().endsWith(".js") || request.getRequestURI().endsWith(".css"))
      return request.getRequestURI();
    else return "coral/index";
  }
}
