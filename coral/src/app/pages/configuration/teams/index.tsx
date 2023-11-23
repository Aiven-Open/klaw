import { PageHeader } from "@aivenio/aquarium";
import { Teams } from "src/app/features/configuration/teams/Teams";
import PreviewBanner from "src/app/components/PreviewBanner";

const TeamsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/teams"} />
      <PageHeader title={"Teams"} />
      <Teams />
    </>
  );
};

export { TeamsPage };
