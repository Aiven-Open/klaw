import {
  Box,
  Button,
  Icon,
  NativeSelectBase,
  Option,
  Typography,
} from "@aivenio/aquarium";
import database from "@aivenio/aquarium/dist/src/icons/database";
import { Dispatch, SetStateAction, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { EnvironmentInfo } from "src/domain/environment";

type TopicOverviewHeaderProps = {
  topicName: string;
  topicExists: boolean;
  environments?: EnvironmentInfo[];
  environmentId?: string;
  setEnvironmentId: Dispatch<SetStateAction<string | undefined>>;
};

function TopicDetailsHeader(props: TopicOverviewHeaderProps) {
  const {
    topicName,
    environments,
    environmentId,
    setEnvironmentId,
    topicExists,
  } = props;

  // To simplify data fetching in TopicDetails, we need environmentId to always be defined.
  // This should always be the case through the state prop on the Browse topics page links setting an initial env...
  // ... but if a user accesses the Topic overview directly, we do not have access to it.
  // We therefore need to set it in this way.
  // This will unfortunately trigger a superfluous refetch for getTopicOverview, but it should be infrequent
  useEffect(() => {
    if (environments !== undefined && environmentId === undefined) {
      setEnvironmentId(environments[0].id);
    }
  }, [environments, environmentId, setEnvironmentId]);

  const navigate = useNavigate();

  return (
    <Box
      display={"flex"}
      flexDirection={"row"}
      alignItems={"start"}
      justifyContent={"space-between"}
      marginBottom={"l2"}
    >
      <Box
        display={"flex"}
        flexDirection={"row"}
        alignItems={"center"}
        colGap={"l2"}
      >
        <Typography.Heading>{topicName}</Typography.Heading>

        <Box width={"l6"}>
          {!environments && (
            <NativeSelectBase
              placeholder={"Loading"}
              disabled={true}
            ></NativeSelectBase>
          )}
          {environments && topicExists && (
            <NativeSelectBase
              aria-label={"Select environment"}
              value={environmentId}
              onChange={(event) => {
                setEnvironmentId(event.target.value);
              }}
            >
              {environments?.length === 1 && (
                <Option
                  aria-readonly={true}
                  disabled={true}
                  value={environments[0].id}
                >
                  {environments[0].name}
                </Option>
              )}
              {environments.length > 1 &&
                environments.map((env) => {
                  return (
                    <Option key={env.id} value={env.id}>
                      {env.name}
                    </Option>
                  );
                })}
            </NativeSelectBase>
          )}
        </Box>

        {topicExists && environments && environments.length > 0 && (
          <Box display={"flex"} alignItems={"center"} colGap={"2"}>
            <Typography.SmallStrong color={"grey-40"}>
              <Icon icon={database} />
            </Typography.SmallStrong>
            <Typography.SmallStrong color={"grey-40"}>
              {environments.length}{" "}
              {environments.length === 1 ? "Environment" : "Environments"}
            </Typography.SmallStrong>
          </Box>
        )}
      </Box>
      <Button.Primary
        disabled={!topicExists}
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

export { TopicDetailsHeader };
