import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";

function TenantInfo() {
  return (
    <>
      <PreviewBanner linkTarget={"/tenantInfo"} />
      <PageHeader title={"Tenant info"} />
      <div>David Tennant</div>
    </>
  );
}

export { TenantInfo };
