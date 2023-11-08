import { PageHeader } from "@aivenio/aquarium";
import { Users } from "src/app/features/configuration/users/Users";

const UsersPage = () => {
  return (
    <>
      <PageHeader title={"Users"} />
      <Users />
    </>
  );
};

export { UsersPage };
