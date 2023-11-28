import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import { ChangePasswordForm } from "src/app/features/user-information/change-password/ChangePasswordForm";

function ChangePassword() {
  return (
    <>
      <PreviewBanner linkTarget={"/changePwd"} />
      <PageHeader title={"Change password"} />
      <ChangePasswordForm />
    </>
  );
}

export { ChangePassword };
