import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import { Profile } from "src/app/features/user-information/profile/Profile";

function UserProfile() {
  return (
    <>
      <PreviewBanner linkTarget={"/myProfile"} />
      <PageHeader title={"User profile"} />
      <Profile />
    </>
  );
}

export { UserProfile };
