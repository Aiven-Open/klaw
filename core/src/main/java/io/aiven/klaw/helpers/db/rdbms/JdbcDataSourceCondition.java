package io.aiven.klaw.helpers.db.rdbms;

import java.util.Objects;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
public class JdbcDataSourceCondition implements Condition {

  @Override
  public boolean matches(
      ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

    Environment defaultEnv = conditionContext.getEnvironment();
    return Objects.requireNonNull(defaultEnv.getProperty("klaw.db.storetype")).equals("rdbms");
  }
}
