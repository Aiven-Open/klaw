package com.kafkamgt.uiapi.entities;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PCStream {
    String topicName;
    String env;
    List<String> producerTeams;
    List<String> consumerTeams;
}
