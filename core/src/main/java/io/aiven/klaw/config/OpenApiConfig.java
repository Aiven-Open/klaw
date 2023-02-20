package io.aiven.klaw.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${klaw.version}")
  private String kwVersion;

  @Bean
  public OpenAPI springShopOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Klaw - OpenAPI")
                .contact(new Contact().email("info@klaw-project.io"))
                .description(
                    "This specification is still a work in progress and is not yet implemented in any API."
                        + " The purpose of this specification is to facilitate developers discussions.")
                .version(kwVersion)
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
        .externalDocs(
            new ExternalDocumentation()
                .description("Klaw documentation")
                .url("https://www.klaw-project.io/docs"));
  }
}
