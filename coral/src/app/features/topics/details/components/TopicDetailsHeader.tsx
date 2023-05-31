import { useNavigate } from "react-router-dom";
import { Box, Button, NativeSelect, Option } from "@aivenio/aquarium";
import { EnvironmentInfo } from "src/domain/environment";
import { Dispatch, SetStateAction } from "react";

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
    >
      <Box
        display={"flex"}
        flexDirection={"row"}
        alignItems={"center"}
        colGap={"l2"}
      >
        {/* @ TODO NativeSelect has a <p></p> without content that takes */}
        {/* space (placeholder for error messages). To align headline */}
        {/* and select visually centered, we need to set fixed heights and */}
        {/* margins to the other elements. WAIT until design is cleared */}
        {/* to avoid unnecessary work! */}
        <h1>{topicName}</h1>

        <Box width={"l6"} height={"l5"}>
          {!environments && (
            <NativeSelect
              placeholder={"Loading"}
              disabled={true}
            ></NativeSelect>
          )}
          {environments && topicExists && (
            <NativeSelect
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
            </NativeSelect>
          )}
        </Box>
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
