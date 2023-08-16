import {
  Box,
  Icon,
  NativeSelectBase,
  Option,
  Typography,
} from "@aivenio/aquarium";
import database from "@aivenio/aquarium/dist/src/icons/database";
import { Dispatch, SetStateAction } from "react";
import { EnvironmentInfo } from "src/domain/environment";
import { InternalLinkButton } from "src/app/components/InternalLinkButton";
import { DisabledButtonTooltip } from "src/app/components/DisabledButtonTooltip";

type TopicOverviewHeaderProps = {
  entity: { name: string; type: "connector" | "topic" };
  entityEditLink: string;
  showEditButton: boolean;
  hasPendingRequest: boolean;
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
    hasPendingRequest,
    entityEditLink,
    environments,
    environmentId,
    setEnvironmentId,
    entityExists,
    entityUpdating,
  } = props;

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
        <Typography.Heading>{entity.name}</Typography.Heading>

        <Box width={"l6"}>
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
        </Box>

        {entityExists && environments && environments.length > 0 && (
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
      {showEditButton && !hasPendingRequest && (
        <InternalLinkButton
          to={entityEditLink}
          disabled={!entityExists || entityUpdating}
        >
          {`Edit ${entity.type}`}
        </InternalLinkButton>
      )}

      {showEditButton && hasPendingRequest && (
        <DisabledButtonTooltip
          tooltip={`The ${entity.type} has a pending request.`}
          role={"link"}
        >
          {`Edit ${entity.type}`}
        </DisabledButtonTooltip>
      )}
    </Box>
  );
}

export { EntityDetailsHeader };
