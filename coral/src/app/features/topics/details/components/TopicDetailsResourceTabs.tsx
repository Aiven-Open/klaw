import { Tabs } from "@aivenio/aquarium";
import { NavigateFunction, Outlet, useNavigate } from "react-router-dom";
import {
  TopicOverviewTabEnum,
  TOPIC_OVERVIEW_TAB_ID_INTO_PATH,
  isTopicsOverviewTabEnum,
} from "src/app/router_utils";
import PreviewBanner from "src/app/components/PreviewBanner";

type Props = {
  currentTab: TopicOverviewTabEnum;
  topicName: string;
};

function TopicOverviewResourcesTabs({ currentTab, topicName }: Props) {
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
                  <Outlet />
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
