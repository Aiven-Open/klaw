package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SyncBackTopics {

    private String[] topicIds;

    @NonNull
    private String sourceEnv;

    @NonNull
    private String targetEnv;

    @NonNull
    private String typeOfSync;
}
