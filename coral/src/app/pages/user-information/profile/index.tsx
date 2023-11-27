import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";

function UserProfile() {
  return (
    <>
      <PreviewBanner linkTarget={"/myProfile"} />
      <PageHeader title={"User profile"} />
      <div>here is a user profile!</div>
    </>
  );
}

export { UserProfile };
