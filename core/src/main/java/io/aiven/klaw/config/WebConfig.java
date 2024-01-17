package io.aiven.klaw.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/notFound").setViewName("forward:/index.html");
    registry.addViewController("/login").setViewName("forward:/coral/index.html");
    registry.addViewController("/coral/coral-login").setViewName("forward:/coral/index.html");
    registry.addViewController("/coral-login").setViewName("forward:/coral/index.html");
  }

  @Bean
  public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> containerCustomizer() {
    return container -> container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/notFound"));
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/static/assets/images/**")
        .addResourceLocations("classpath:/static/assets/images/")
        .setCachePeriod(604800); // 1 week in seconds;

    registry
        .addResourceHandler("/static/coral/**")
        .addResourceLocations("classpath:/static/coral/", "classpath:/static/coral/assets/")
        .setCachePeriod(604800); // 1 week in seconds

    registry
        .addResourceHandler("/static/coral/index.html")
        .addResourceLocations("classpath:/static/coral/index.html")
        .setCachePeriod(604800); // 1 week in seconds

    registry
        .addResourceHandler("/static/coral/assets")
        .addResourceLocations("classpath:/static/coral/assets/")
        .setCachePeriod(604800); // 1 week in seconds
  }
}
