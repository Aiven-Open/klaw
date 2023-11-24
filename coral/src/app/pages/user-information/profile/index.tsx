import PreviewBanner from "src/app/components/PreviewBanner";
import { Profile } from "src/app/features/user-information/profile/Profile";
import { PageHeader } from "@aivenio/aquarium";

function UserProfile() {
  return (
    <>
      <PreviewBanner linkTarget={"/myProfile"} />
      <PageHeader
        title={"User profile"}
        subtitle={"Manage your user profile information and settings."}
      />
      <Profile />
    </>
  );
}

export { UserProfile };
