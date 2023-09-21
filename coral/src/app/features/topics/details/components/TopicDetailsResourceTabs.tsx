import { Alert, Box, Icon, Tabs } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import PreviewBanner from "src/app/components/PreviewBanner";
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
}: Props) {
  const navigate = useNavigate();
  const topicName = topicOverview?.topicInfo.topicName;

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

  const renderTabContent = () => {
    if (isError) {
      return (
        <Box.Flex marginBottom={"l1"} marginTop={"l2"} role="alert">
          <Alert type="error">
            There was an error trying to load the topic details:{" "}
            {parseErrorMsg(error)}.
            <br />
            Please try again later.
          </Alert>
        </Box.Flex>
      );
    }

    if (isLoading) {
      return (
        <Box.Flex paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading topic details</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box.Flex>
      );
    }

    if (!topicOverview?.topicExists) {
      return (
        <Box.Flex marginBottom={"l1"} marginTop={"l2"} role="alert">
          <Alert type="warning">Topic {topicName} does not exist.</Alert>
        </Box.Flex>
      );
    }
    return (
      <div data-testid={"tabpanel-content"}>
        <Outlet
          context={{
            environmentId:
              environmentId || topicOverview.availableEnvironments[0].id,
            setSchemaVersion,
            topicOverview,
            topicOverviewIsRefetching,
            topicName: topicOverview.topicInfo.topicName,
            topicSchemas,
            topicSchemasIsRefetching,
            userCanDeleteTopic: topicOverview.topicInfo.topicDeletable,
            topicHasOpenDeleteRequest: !topicOverview.topicInfo.showDeleteTopic,
          }}
        />
      </div>
    );
  };

  const TopicTabs = () => (
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
  );

  return <TopicTabs />;
}

export { TopicOverviewResourcesTabs };
