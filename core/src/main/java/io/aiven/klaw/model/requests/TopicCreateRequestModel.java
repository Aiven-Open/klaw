package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.validation.TopicRequestValidator;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@TopicRequestValidator(getPermissionType = PermissionType.REQUEST_CREATE_TOPICS)
public class TopicCreateRequestModel extends TopicRequestModel implements Serializable {}
