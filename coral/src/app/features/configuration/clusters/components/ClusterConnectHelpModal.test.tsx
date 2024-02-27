import { cleanup, render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import ClusterConnectHelpModal from "src/app/features/configuration/clusters/components/ClusterConnectHelpModal";
import { ClusterDetails } from "src/domain/cluster";

const onClose = jest.fn();
const modalDataWithApplicationPropertiesToCopy: {
  kafkaFlavor: ClusterDetails["kafkaFlavor"];
  protocol: ClusterDetails["protocol"];
  clusterType: ClusterDetails["clusterType"];
  clusterName: ClusterDetails["clusterName"];
  clusterId: ClusterDetails["clusterId"];
} = {
  kafkaFlavor: "APACHE_KAFKA",
  protocol: "SSL",
  clusterType: "KAFKA",
  clusterName: "TestCluster",
  clusterId: 123,
};
const modalDataWithoutApplicationPropertiesToCopy: {
  kafkaFlavor: ClusterDetails["kafkaFlavor"];
  protocol: ClusterDetails["protocol"];
  clusterType: ClusterDetails["clusterType"];
  clusterName: ClusterDetails["clusterName"];
  clusterId: ClusterDetails["clusterId"];
} = {
  kafkaFlavor: "APACHE_KAFKA",
  protocol: "PLAINTEXT",
  clusterType: "KAFKA",
  clusterName: "TestCluster",
  clusterId: 123,
};

describe("ClusterConnectHelpModal", () => {
  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  it("renders with applicationPropertiesString", async () => {
    render(
      <ClusterConnectHelpModal
        onClose={onClose}
        modalData={modalDataWithApplicationPropertiesToCopy}
      />
    );
    const expectedApplicationPropertiesString = `${modalDataWithApplicationPropertiesToCopy.clusterName.toLowerCase() + modalDataWithApplicationPropertiesToCopy.clusterId}.kafkassl.keystore.location=path/to/client.keystore.p12\n${modalDataWithApplicationPropertiesToCopy.clusterName.toLowerCase() + modalDataWithApplicationPropertiesToCopy.clusterId}.kafkassl.keystore.pwd=keystorePw\n${modalDataWithApplicationPropertiesToCopy.clusterName.toLowerCase() + modalDataWithApplicationPropertiesToCopy.clusterId}.kafkassl.key.pwd=keyPw\n${modalDataWithApplicationPropertiesToCopy.clusterName.toLowerCase() + modalDataWithApplicationPropertiesToCopy.clusterId}.kafkassl.truststore.location=path/to/client.truststore.jks\n${modalDataWithApplicationPropertiesToCopy.clusterName.toLowerCase() + modalDataWithApplicationPropertiesToCopy.clusterId}.kafkassl.truststore.pwd=truststorePw\n${modalDataWithApplicationPropertiesToCopy.clusterName.toLowerCase() + modalDataWithApplicationPropertiesToCopy.clusterId}.kafkassl.keystore.type=pkcs12\n${modalDataWithApplicationPropertiesToCopy.clusterName.toLowerCase() + modalDataWithApplicationPropertiesToCopy.clusterId}.kafkassl.truststore.type=JKS`;

    expect(screen.getByText("Connect cluster to Klaw")).toBeVisible();
    expect(
      screen.getByText(/Copy, paste, and replace placeholders/)
    ).toBeVisible();
    expect(screen.getByRole("link", { name: /Learn more/ })).toHaveAttribute(
      "href",
      "https://www.klaw-project.io/docs/cluster-connectivity-setup/"
    );
    // Contraption needed to assert the expectedApplicationPropertiesString when it is broken on multiple lines
    expect(
      screen.getByText((_, node) => {
        const hasText = (node: Element) =>
          node.textContent === expectedApplicationPropertiesString;
        const nodeHasText = node ? hasText(node) : false;
        const childrenDontHaveText = Array.from(node?.children || []).every(
          (child) => !hasText(child)
        );

        return nodeHasText && childrenDontHaveText;
      })
    ).toBeVisible();

    await userEvent.click(screen.getByRole("button", { name: "Done" }));

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it("renders without applicationPropertiesString", async () => {
    render(
      <ClusterConnectHelpModal
        onClose={onClose}
        modalData={modalDataWithoutApplicationPropertiesToCopy}
      />
    );

    expect(screen.getByText("Connect cluster to Klaw")).toBeVisible();
    expect(
      screen.queryByText(/Copy, paste, and replace placeholders/)
    ).toBeNull();
    expect(
      screen.getByRole("link", {
        name: /Learn more about cluster connectivity setup/,
      })
    ).toHaveAttribute(
      "href",
      "https://www.klaw-project.io/docs/cluster-connectivity-setup/"
    );

    await userEvent.click(screen.getByRole("button", { name: "Done" }));

    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
