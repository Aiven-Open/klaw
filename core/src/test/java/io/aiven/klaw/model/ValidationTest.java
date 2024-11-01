package io.aiven.klaw.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.requests.KafkaConnectorRequestModel;
import io.aiven.klaw.model.requests.TopicRequestModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ValidationTest {

  private static Validator validator;

  @BeforeAll
  public static void setUp() {
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @Test
  public void testNewTopicRequest() {
    TopicRequest topicRequest = new TopicRequest();
    //        topicRequest.setAcl_ip("10.1.1.1");
    topicRequest.setAppname("newapp");
    topicRequest.setEnvironment("dev");
    topicRequest.setReplicationfactor("1");
    topicRequest.setPossibleTeams(new ArrayList<String>());
    topicRequest.setTotalNoPages("1");
    ArrayList<String> pageList = new ArrayList();
    pageList.add("1");
    topicRequest.setAllPageNos(pageList);

    topicRequest.setTopicname("newtopic");
    topicRequest.setEnvironment("dev");

    assertThat(topicRequest).isNotNull();
  }

  @Test
  public void testNewAclRequest() {
    AclRequests aclRequests = new AclRequests();
    aclRequests.setAcl_ip("10.1.1.1");
    aclRequests.setAppname("newapp");
    aclRequests.setEnvironment("dev");
    aclRequests.setReq_no(1001);
    aclRequests.setRequestingteam(1);
    aclRequests.setApprover("user1");
    aclRequests.setTopicname("newtopic");
    aclRequests.setAclType(AclType.PRODUCER.value);

    assertThat(aclRequests).isNotNull();
  }

  @Test
  public void testNewSchemaRequest() {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setAppname("newapp");
    schemaRequest.setEnvironment("dev");
    schemaRequest.setApprover("user1");
    schemaRequest.setTopicname("newtopic");
    schemaRequest.setTeamId(3);
    schemaRequest.setSchemafull("{type:string}");
    schemaRequest.setSchemaversion("1.0");
    schemaRequest.setEnvironment("dev");

    assertThat(schemaRequest).isNotNull();
  }

  @ParameterizedTest
  @CsvSource({
    "null topic name,,1,1,mydesc,must not be null",
    "Invalid topic name,!!!!,1,1,mydesc,Invalid topic name",
    "partitions must be greater then 0,topic-1,0,1,mydesc,must be greater than zero",
    "replication must not be null,topic-1,1,,mydesc,must not be null"
  })
  public void validateTopicRequestModel(
      String testName,
      String topicName,
      String partitions,
      String replication,
      String description,
      String errMsgContains) {
    TopicRequestModel model = new TopicRequestModel();
    model.setTopicname(topicName);
    model.setTopicpartitions(Integer.parseInt(partitions));
    model.setReplicationfactor(replication);
    model.setDescription(description);
    model.setEnvironment("Dev");
    model.setRequestOperationType(RequestOperationType.CREATE);

    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(model);
    assertThat(violations).hasSize(1);
    ConstraintViolation<TopicRequestModel> violation = violations.iterator().next();
    assertThat(violation.getMessage()).contains(errMsgContains);
  }

  @ParameterizedTest
  @MethodSource("kafkaConnectorGenerateTestData")
  public void validateConnectorRequestModel(
      String testName,
      String connectorName,
      String connectorConfig,
      String env,
      String description,
      String requestOperationType,
      List<String> errorMessages) {
    KafkaConnectorRequestModel model = new KafkaConnectorRequestModel();
    model.setConnectorName(connectorName);
    model.setConnectorConfig(connectorConfig);
    model.setDescription(description);
    model.setEnvironment(env);
    model.setRequestOperationType(RequestOperationType.of(requestOperationType));

    Set<ConstraintViolation<KafkaConnectorRequestModel>> violations = validator.validate(model);
    assertThat(violations).hasSize(errorMessages.size());
    for (ConstraintViolation<KafkaConnectorRequestModel> violation : violations) {

      assertThat(errorMessages).contains(violation.getMessage());
    }
  }

  private static Stream<Arguments> kafkaConnectorGenerateTestData() {
    return Stream.of(
        Arguments.of(
            "null Connector name",
            null,
            "{}",
            "DEV",
            "mydesc",
            "Create",
            List.of("Connector name must not be null")),
        Arguments.of(
            "Invalid Connector name",
            "!!!!",
            "{}",
            "DEV",
            "mydesc",
            "Create",
            List.of("Invalid connector name")),
        Arguments.of(
            "connector config must not be null",
            "connector-1",
            null,
            "DEV",
            "mydesc",
            "Create",
            List.of("Connector configuration must not be null")),
        Arguments.of(
            "environment must not be null",
            "connector-1",
            "{}",
            null,
            "mydesc",
            "Create",
            List.of("The environment must not be null")),
        Arguments.of(
            "Description must be less then 100",
            "connector-1",
            "{}",
            "DEV",
            "ddesddedededededededededededededededededededededdededdesddededededededededededededededededededededes1",
            "Create",
            List.of(
                "Description must be a minimum of 1 character and a maximum of 100, this can be less if multibyte encoding is being used.")),
        Arguments.of(
            "Description must be at least 1 char",
            "connector-1",
            "{}",
            "DEV",
            "",
            "Create",
            List.of(
                "Invalid description",
                "Description must be a minimum of 1 character and a maximum of 100, this can be less if multibyte encoding is being used.")),
        Arguments.of(
            "Request Operation must not be null",
            "connector-1",
            "{}",
            "DEV",
            "a simple description",
            "",
            List.of("Request operation type must not be null")));
  }
}
