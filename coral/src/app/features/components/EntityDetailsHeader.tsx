import {
  Box,
  Button,
  Icon,
  NativeSelectBase,
  Option,
  Typography,
} from "@aivenio/aquarium";
import database from "@aivenio/aquarium/dist/src/icons/database";
import { Dispatch, SetStateAction } from "react";
import { useNavigate } from "react-router-dom";
import { EnvironmentInfo } from "src/domain/environment";

type TopicOverviewHeaderProps = {
  entity: { name: string; type: "connector" | "topic" };
  entityEditLink: string;
  showEditButton: boolean;
  entityExists: boolean;
  entityUpdating: boolean;
  environments?: EnvironmentInfo[];
  environmentId?: string;
  setEnvironmentId: Dispatch<SetStateAction<string | undefined>>;
};

function EntityDetailsHeader(props: TopicOverviewHeaderProps) {
  const {
    entity,
    showEditButton,
    entityEditLink,
    environments,
    environmentId,
    setEnvironmentId,
    entityExists,
    entityUpdating,
  } = props;

  const navigate = useNavigate();

  return (
    <Box.Flex
      display={"flex"}
      flexDirection={"row"}
      alignItems={"start"}
      justifyContent={"space-between"}
      marginBottom={"l2"}
    >
      <Box.Flex
        display={"flex"}
        flexDirection={"row"}
        alignItems={"center"}
        colGap={"l2"}
      >
        <Typography.Heading>{entity.name}</Typography.Heading>

        <Box.Flex width={"l6"}>
          {!environments && (
            <NativeSelectBase
              placeholder={"Loading"}
              disabled={true}
            ></NativeSelectBase>
          )}
          {environments && entityExists && (
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
        </Box.Flex>

        {entityExists && environments && environments.length > 0 && (
          <Box.Flex display={"flex"} alignItems={"center"} colGap={"2"}>
            <Typography.SmallStrong color={"grey-40"}>
              <Icon icon={database} />
            </Typography.SmallStrong>
            <Typography.SmallStrong color={"grey-40"}>
              {environments.length}{" "}
              {environments.length === 1 ? "Environment" : "Environments"}
            </Typography.SmallStrong>
          </Box.Flex>
        )}
      </Box.Flex>
      {showEditButton && (
        <Button.Primary
          disabled={!entityExists || entityUpdating}
          onClick={() => navigate(entityEditLink)}
        >
          Edit {entity.type}
        </Button.Primary>
      )}
    </Box.Flex>
  );
}

export { EntityDetailsHeader };
