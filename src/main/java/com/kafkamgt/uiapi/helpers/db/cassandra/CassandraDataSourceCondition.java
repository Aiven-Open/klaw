package com.kafkamgt.uiapi.helpers.db.cassandra;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
public class CassandraDataSourceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        Environment defaultEnv = conditionContext.getEnvironment();
        if(defaultEnv.getProperty("db.storetype").equals("cassandra"))
            return true;
        else
            return false;
    }
}
