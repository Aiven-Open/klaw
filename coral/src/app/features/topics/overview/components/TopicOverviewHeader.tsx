import { useNavigate } from "react-router-dom";
import { Box, Button } from "@aivenio/aquarium";

type TopicOverviewHeaderProps = {
  topicName: string;
};

function TopicOverviewHeader(props: TopicOverviewHeaderProps) {
  const { topicName } = props;
  // @TODO get data via api
  const topicOverview = {
    topicExists: true,
    schemaExists: false,
    prefixAclsExists: false,
    txnAclsExists: false,
    topicInfoList: [
      {
        noOfPartitions: 1,
        noOfReplicas: "1",
        teamname: "Ospo",
        teamId: 0,
        envId: "1",
        showEditTopic: true,
        showDeleteTopic: false,
        topicDeletable: false,
        envName: "DEV",
      },
    ],
    aclInfoList: [
      {
        req_no: "1006",
        acl_ssl: "aivtopic3user",
        topicname: "aivtopic3",
        topictype: "Producer",
        consumergroup: "-na-",
        environment: "1",
        environmentName: "DEV",
        teamname: "Ospo",
        teamid: 0,
        aclPatternType: "LITERAL",
        showDeleteAcl: true,
        kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
      },
      {
        req_no: "1011",
        acl_ssl: "declineme",
        topicname: "aivtopic3",
        topictype: "Producer",
        consumergroup: "-na-",
        environment: "1",
        environmentName: "DEV",
        teamname: "Ospo",
        teamid: 0,
        aclPatternType: "LITERAL",
        showDeleteAcl: true,
        kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
      },
      {
        req_no: "1060",
        acl_ssl: "amathieu",
        topicname: "aivtopic3",
        topictype: "Producer",
        consumergroup: "-na-",
        environment: "1",
        environmentName: "DEV",
        teamname: "Ospo",
        teamid: 0,
        aclPatternType: "LITERAL",
        showDeleteAcl: true,
        kafkaFlavorType: "AIVEN_FOR_APACHE_KAFKA",
      },
    ],
    topicHistoryList: [
      {
        environmentName: "DEV",
        teamName: "Ospo",
        requestedBy: "muralibasani",
        requestedTime: "2022-Nov-04 14:41:18",
        approvedBy: "josepprat",
        approvedTime: "2022-Nov-04 14:48:38",
        remarks: "Create",
      },
    ],
    topicPromotionDetails: {
      topicName: "aivtopic3",
      status: "NO_PROMOTION",
    },
    availableEnvironments: [
      {
        id: "1",
        name: "DEV",
      },
      {
        id: "2",
        name: "TST",
      },
    ],
    topicIdForDocumentation: 1015,
  };

  const navigate = useNavigate();

  return (
    <Box
      display={"flex"}
      flexDirection={"row"}
      alignItems={"center"}
      justifyContent={"space-between"}
    >
      <Box
        display={"flex"}
        flexDirection={"row"}
        alignItems={"center"}
        colGap={"l2"}
      >
        <h1>{topicName}</h1>

        <select>
          <option>{topicOverview.availableEnvironments[0].name}</option>
        </select>
      </Box>
      <Button.Primary
        onClick={() =>
          navigate(
            "/topicOverview/topicname=SchemaTest&env=${topicOverview.availableEnvironments[0].id}&requestType=edit"
          )
        }
      >
        Edit topic
      </Button.Primary>
    </Box>
  );
}

export { TopicOverviewHeader };
