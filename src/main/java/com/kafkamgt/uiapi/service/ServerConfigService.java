package com.kafkamgt.uiapi.service;


import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.ServerConfigProperties;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServerConfigService {

    private static Logger LOG = LoggerFactory.getLogger(ServerConfigService.class);

    @Autowired
    private Environment env;

    private static List<ServerConfigProperties> listProps;

    public ServerConfigService(Environment env){
        this.env = env;
    }

    @PostConstruct
    public void getAllProperties() {
        LOG.info("All server properties loaded");

        List<ServerConfigProperties> listProps = new ArrayList<>();
        WordUtils wr;

        if (env instanceof ConfigurableEnvironment) {
            for (PropertySource propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
                if (propertySource instanceof EnumerablePropertySource) {
                    for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {

                        ServerConfigProperties props = new ServerConfigProperties();
                        props.setKey(key);
                        if(key.contains("password") || key.contains("license"))
                            props.setValue("*******");
                        else
                            props.setValue(WordUtils.wrap(propertySource.getProperty(key)+"",125,"\n",true));

                        if(!checkPropertyExists(listProps,key))
                            listProps.add(props);
                    }
                }
            }
        }
        this.listProps = listProps;

    }

    public List<ServerConfigProperties> getAllProps(){
        return this.listProps;
    }

    private boolean checkPropertyExists(List<ServerConfigProperties> props, String key){
        for(ServerConfigProperties serverProps: props){
            if(serverProps.getKey().equals(key))
                return true;
        }
        return false;
    }


}