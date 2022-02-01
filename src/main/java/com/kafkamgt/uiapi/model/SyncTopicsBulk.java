package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SyncTopicsBulk {

    private String[] topicNames;

    @NonNull
    private String sourceEnv;

    @NonNull
    private String selectedTeam;

    @NonNull
    private String typeOfSync;

    private Object[] topicDetails;

    private String topicSearchFilter;
}
