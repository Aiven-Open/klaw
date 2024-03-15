import { cleanup, render, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import ClusterDeleteModal from "src/app/features/configuration/clusters/components/ClusterDeleteModal";
import { ClusterDetails } from "src/domain/cluster";

const handleClose = jest.fn();
const handleDelete = jest.fn();
const modalDataWithAllowedDeletion: {
  canDeleteCluster: ClusterDetails["showDeleteCluster"];
  clusterName: ClusterDetails["clusterName"];
  clusterId: ClusterDetails["clusterId"];
} = {
  canDeleteCluster: true,
  clusterName: "TestCluster",
  clusterId: 123,
};
const modalDataWithForbiddenDeletion: {
  canDeleteCluster: ClusterDetails["showDeleteCluster"];
  clusterName: ClusterDetails["clusterName"];
  clusterId: ClusterDetails["clusterId"];
} = {
  canDeleteCluster: false,
  clusterName: "TestCluster",
  clusterId: 123,
};

describe("ClusterDeleteModal", () => {
  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  it("renders with disabled Delete button", async () => {
    render(
      <ClusterDeleteModal
        handleClose={handleClose}
        handleDelete={handleDelete}
        isLoading={false}
        error={null}
        {...modalDataWithForbiddenDeletion}
      />
    );
    const deleteButton = screen.getByRole("button", { name: "Delete cluster" });
    expect(screen.getByText(/Cannot delete cluster/)).toBeVisible();
    expect(deleteButton).toBeDisabled();

    await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it("renders with enabled Delete button", async () => {
    render(
      <ClusterDeleteModal
        handleClose={handleClose}
        handleDelete={handleDelete}
        isLoading={false}
        error={null}
        {...modalDataWithAllowedDeletion}
      />
    );
    const deleteButton = screen.getByRole("button", { name: "Delete cluster" });
    expect(screen.getByText(/Deleting cluster/)).toBeVisible();
    expect(deleteButton).toBeEnabled();

    await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

    expect(handleClose).toHaveBeenCalledTimes(1);
  });

  it("deletes Cluster on clicking the Delete button", async () => {
    render(
      <ClusterDeleteModal
        handleClose={handleClose}
        handleDelete={handleDelete}
        isLoading={false}
        error={null}
        {...modalDataWithAllowedDeletion}
      />
    );
    const deleteButton = screen.getByRole("button", { name: "Delete cluster" });
    expect(screen.getByText(/Deleting cluster/)).toBeVisible();
    expect(deleteButton).toBeEnabled();

    await userEvent.click(deleteButton);

    expect(handleDelete).toHaveBeenCalledWith("123");
  });

  it("renders loading state correctly", async () => {
    render(
      <ClusterDeleteModal
        handleClose={handleClose}
        handleDelete={handleDelete}
        isLoading={true}
        error={null}
        {...modalDataWithAllowedDeletion}
      />
    );
    const deleteButton = screen.getByRole("button", { name: "Delete cluster" });

    expect(deleteButton).toBeDisabled();
  });

  it("renders error state correctly", async () => {
    render(
      <ClusterDeleteModal
        handleClose={handleClose}
        handleDelete={handleDelete}
        isLoading={false}
        error={new Error("Test error")}
        {...modalDataWithAllowedDeletion}
      />
    );
    const errorText = screen.getByText("Test error");

    expect(errorText).toBeVisible();
  });
});
