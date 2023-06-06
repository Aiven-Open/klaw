import { Alert, Box, Icon, Tabs } from "@aivenio/aquarium";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import {
  TopicOverviewTabEnum,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  isTopicsOverviewTabEnum,
} from "src/app/router_utils";
import PreviewBanner from "src/app/components/PreviewBanner";
import { TopicOverview } from "src/domain/topic";
import loading from "@aivenio/aquarium/icons/loading";
import { parseErrorMsg } from "src/services/mutation-utils";
import { TopicSchemaOverview } from "src/domain/topic/topic-types";

type Props = {
  currentTab: TopicOverviewTabEnum;
  environmentId?: string;
  error?: unknown;
  isError: boolean;
  isLoading: boolean;
  topicOverview?: TopicOverview;
  topicSchemas?: TopicSchemaOverview;
};

function TopicOverviewResourcesTabs({
  currentTab,
  environmentId,
  error,
  isError,
  isLoading,
  topicOverview,
  topicSchemas,
}: Props) {
  const navigate = useNavigate();
  const topicName = topicOverview?.topicInfoList[0].topicName;

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

  const tabsMap: Array<{
    topicOverviewTabEnum: TopicOverviewTabEnum;
    title: string;
  }> = [
    {
      topicOverviewTabEnum: TopicOverviewTabEnum.OVERVIEW,
      title: "Overview",
    },
    {
      topicOverviewTabEnum: TopicOverviewTabEnum.ACLS,
      title: "Subscriptions",
    },
    {
      topicOverviewTabEnum: TopicOverviewTabEnum.MESSAGES,
      title: "Messages",
    },
    {
      topicOverviewTabEnum: TopicOverviewTabEnum.SCHEMA,
      title: "Schema",
    },
    {
      topicOverviewTabEnum: TopicOverviewTabEnum.DOCUMENTATION,
      title: "Documentation",
    },
    {
      topicOverviewTabEnum: TopicOverviewTabEnum.HISTORY,
      title: "History",
    },
    {
      topicOverviewTabEnum: TopicOverviewTabEnum.SETTINGS,
      title: "Settings",
    },
  ];

  function renderTabContent() {
    if (isError) {
      return (
        <Box marginBottom={"l1"} marginTop={"l2"} role="alert">
          <Alert type="error">
            There was an error trying to load the topic details:{" "}
            {parseErrorMsg(error)}.
            <br />
            Please try again later.
          </Alert>
        </Box>
      );
    }

    if (isLoading) {
      return (
        <Box paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading topic details</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box>
      );
    }

    if (!topicOverview?.topicExists) {
      return (
        <Box marginBottom={"l1"} marginTop={"l2"} role="alert">
          <Alert type="warning">Topic {topicName} does not exist.</Alert>
        </Box>
      );
    }
    return (
      <div data-testid={"tabpanel-content"}>
        <Outlet
          context={{
            environmentId:
              environmentId || topicOverview.availableEnvironments[0].id,
            topicOverview,
            topicName: topicOverview.topicInfoList[0].topicName,
            topicSchemas,
          }}
        />
      </div>
    );
  }

  return (
    <div>
      <Tabs
        value={currentTab}
        onChange={(resourceTypeId) => navigateToTab(navigate, resourceTypeId)}
      >
        {tabsMap.map((tab) => {
          return (
            <Tabs.Tab
              title={tab.title}
              value={tab.topicOverviewTabEnum}
              aria-label={tab.title}
              key={tab.title}
            >
              {currentTab === tab.topicOverviewTabEnum && (
                <div>
                  <PreviewBanner
                    linkTarget={`/topicOverview?topicname=${topicName}`}
                  />
                  {renderTabContent()}
                </div>
              )}
            </Tabs.Tab>
          );
        })}
      </Tabs>
    </div>
  );
}

export { TopicOverviewResourcesTabs };
