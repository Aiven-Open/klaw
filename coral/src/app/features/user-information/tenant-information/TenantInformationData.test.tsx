import { cleanup, render } from "@testing-library/react";
import { TenantInformationData } from "src/app/features/user-information/tenant-information/TenantInformationData";

const props = {
  tenantName: "default",
  contactPerson: "Klaw Administrator",
  orgName: "Default Organization",
  description: "This is the default description",
};

describe("TenantInformationData.tsx", () => {
  afterAll(cleanup);

  describe("renders correct DOM with provided props", () => {
    it("renders correct DOM with required props", () => {
      render(<TenantInformationData {...props} />);

      expect(screen).toMatchSnapshot();
    });
  });
});
