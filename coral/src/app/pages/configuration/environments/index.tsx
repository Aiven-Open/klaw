import { PageHeader } from "@aivenio/aquarium";
import { useMatches, Navigate } from "react-router-dom";
import EnvironmentsTabs from "src/app/features/configuration/environments/components/EnvironmentsTabs";

import PreviewBanner from "src/app/components/PreviewBanner";
import {
  ENVIRONMENT_TAB_ID_INTO_PATH,
  EnvironmentsTabEnum,
} from "src/services/router-utils/types";
import { isEnvironmentsTabEnum } from "src/services/router-utils/route-utils";

function findMatchingTab(
  matches: ReturnType<typeof useMatches>
): EnvironmentsTabEnum | undefined {
  const match = matches
    .map((match) => match.id)
    .find((id) =>
      Object.prototype.hasOwnProperty.call(ENVIRONMENT_TAB_ID_INTO_PATH, id)
    );
  if (isEnvironmentsTabEnum(match)) {
    return match;
  }
  return undefined;
}

const EnvironmentsPage = () => {
  const matches = useMatches();
  const currentTab = findMatchingTab(matches);

  if (currentTab === undefined) {
    return <Navigate to={`/configuration/environments/kafka`} replace={true} />;
  }
  return (
    <>
      <PreviewBanner linkTarget={"/envs"} />
      <PageHeader title={"Environments"} />
      <EnvironmentsTabs currentTab={currentTab} />
    </>
  );
};

export default EnvironmentsPage;
