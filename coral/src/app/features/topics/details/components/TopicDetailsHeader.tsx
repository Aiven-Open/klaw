import { useNavigate } from "react-router-dom";
import {
  Box,
  Button,
  Icon,
  NativeSelectBase,
  Option,
  Typography,
} from "@aivenio/aquarium";
import { EnvironmentInfo } from "src/domain/environment";
import { Dispatch, SetStateAction } from "react";
import database from "@aivenio/aquarium/dist/src/icons/database";

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
              value={environmentId || environments[0]?.id}
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
            <Typography.SmallTextBold color={"grey-40"}>
              <Icon icon={database} />
            </Typography.SmallTextBold>
            <Typography.SmallTextBold color={"grey-40"}>
              {environments.length}{" "}
              {environments.length === 1 ? "Environment" : "Environments"}
            </Typography.SmallTextBold>
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
