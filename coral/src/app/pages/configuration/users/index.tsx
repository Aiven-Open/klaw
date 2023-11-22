import { PageHeader } from "@aivenio/aquarium";
import { Users } from "src/app/features/configuration/users/Users";
import PreviewBanner from "src/app/components/PreviewBanner";

const UsersPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/users"} />
      <PageHeader title={"Users"} />
      <Users />
    </>
  );
};

export { UsersPage };
