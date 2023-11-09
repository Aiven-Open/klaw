import { PageHeader } from "@aivenio/aquarium";
import { Teams } from "src/app/features/configuration/teams/Teams";

const TeamsPage = () => {
  return (
    <>
      <PageHeader title={"Teams"} />
      <Teams />
    </>
  );
};

export { TeamsPage };
