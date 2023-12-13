import { Alert, Box, Icon, Tabs } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import {
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  TopicOverviewTabEnum,
  isTopicsOverviewTabEnum,
} from "src/app/router_utils";
import { TopicOverview } from "src/domain/topic";
import { TopicSchemaOverview } from "src/domain/topic/topic-types";
import { parseErrorMsg } from "src/services/mutation-utils";

type Props = {
  currentTab: TopicOverviewTabEnum;
  setSchemaVersion: (id: number) => void;
  environmentId?: string;
  error?: unknown;
  isError: boolean;
  isLoading: boolean;
  topicOverview?: TopicOverview;
  topicOverviewIsRefetching: boolean;
  topicSchemas?: TopicSchemaOverview;
  topicSchemasIsRefetching?: boolean;
  topicName: string;
};

function TopicOverviewResourcesTabs({
  currentTab,
  environmentId,
  setSchemaVersion,
  error,
  isError,
  isLoading,
  topicOverview,
  topicOverviewIsRefetching,
  topicSchemas,
  topicSchemasIsRefetching,
  topicName,
}: Props) {
  const navigate = useNavigate();
  function navigateToTab(
    navigate: NavigateFunction,
    resourceTypeId: unknown
  ): void {
    if (isTopicsOverviewTabEnum(resourceTypeId)) {
      navigate(
        `/topic/${topicName}/${TOPIC_OVERVIEW_TAB_ID_INTO_PATH[resourceTypeId]}`,
        {
          replace: true,
        }
      );
    }
  }

  const tabContent = (
    <div>
      {isError && (
        <Box marginBottom={"l1"} marginTop={"l2"}>
          <Alert type="error">
            There was an error trying to load the topic details:{" "}
            {parseErrorMsg(error)}.
            <br />
            Please try again later.
          </Alert>
        </Box>
      )}
      {isLoading && (
        <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading topic details</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box>
      )}
      {!isLoading && !isError && !topicOverview?.topicExists && (
        <Box marginBottom={"l1"} marginTop={"l2"}>
          <Alert type="warning">Topic {topicName} does not exist.</Alert>
        </Box>
      )}
      {!isError && !isLoading && topicOverview?.topicExists && (
        <div data-testid={"tabpanel-content"}>
          <Outlet
            context={{
              environmentId:
                environmentId || topicOverview.availableEnvironments[0].id,
              setSchemaVersion,
              topicOverview,
              topicOverviewIsRefetching,
              topicName,
              topicSchemas,
              topicSchemasIsRefetching,
              userCanDeleteTopic: topicOverview.topicInfo.topicDeletable,
              topicHasOpenDeleteRequest:
                !topicOverview.topicInfo.showDeleteTopic,
            }}
          />
        </div>
      )}
    </div>
  );

  return (
    <Tabs
      value={currentTab}
      onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
    >
      <Tabs.Tab
        title={"Overview"}
        value={TopicOverviewTabEnum.OVERVIEW}
        aria-label={"Overview"}
        key={"Overview"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"Subscriptions"}
        value={TopicOverviewTabEnum.ACLS}
        aria-label={"Subscriptions"}
        key={"Subscriptions"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"Messages"}
        value={TopicOverviewTabEnum.MESSAGES}
        aria-label={"Messages"}
        key={"Messages"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"Schema"}
        value={TopicOverviewTabEnum.SCHEMA}
        aria-label={"Schema"}
        key={"Schema"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"Readme"}
        value={TopicOverviewTabEnum.DOCUMENTATION}
        aria-label={"Readme"}
        key={"Readme"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"History"}
        value={TopicOverviewTabEnum.HISTORY}
        aria-label={"History"}
        key={"History"}
      >
        {tabContent}
      </Tabs.Tab>
      <Tabs.Tab
        title={"Settings"}
        value={TopicOverviewTabEnum.SETTINGS}
        aria-label={"Settings"}
        key={"Settings"}
      >
        {tabContent}
      </Tabs.Tab>
    </Tabs>
  );
}

export { TopicOverviewResourcesTabs };
