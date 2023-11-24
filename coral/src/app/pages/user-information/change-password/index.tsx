import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";

function ChangePassword() {
  return (
    <>
      <PreviewBanner linkTarget={"/changePwd"} />
      <PageHeader title={"Change password"} />
      <div>Ch-ch-ch-ch-changes</div>
    </>
  );
}

export { ChangePassword };
