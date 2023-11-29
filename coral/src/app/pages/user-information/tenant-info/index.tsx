import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import { TenantInformation } from "src/app/features/user-information/tenant-information/TenantInformation";

function TenantInfo() {
  return (
    <>
      <PreviewBanner linkTarget={"/tenantInfo"} />
      <PageHeader title={"Tenant information"} />
      <TenantInformation />
    </>
  );
}

export { TenantInfo };
