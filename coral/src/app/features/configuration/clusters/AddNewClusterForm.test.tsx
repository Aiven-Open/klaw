import { cleanup, screen, within } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import AddNewClusterForm from "src/app/features/configuration/clusters/AddNewClusterForm";
import { customRender } from "src/services/test-utils/render-with-wrappers";
import { addNewCluster } from "src/domain/cluster";

jest.mock("src/domain/cluster/cluster-api.ts");

const mockAddNewClusterRequest = addNewCluster as jest.MockedFunction<
  typeof addNewCluster
>;

const mockedUsedNavigate = jest.fn();
jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedUsedNavigate,
}));

const mockedUseToast = jest.fn();
jest.mock("@aivenio/aquarium", () => ({
  ...jest.requireActual("@aivenio/aquarium"),
  useToast: () => mockedUseToast,
}));

describe("<AddNewClusterForm />", () => {
  const originalConsoleError = console.error;

  beforeEach(() => {
    customRender(<AddNewClusterForm />, {
      queryClient: true,
      aquariumContext: true,
    });
  });

  afterEach(() => {
    cleanup();
    jest.clearAllMocks();
  });

  describe("renders all necessary elements by default", () => {
    it("renders Cluster type field", () => {
      const clusterTypeSelect = screen.getByRole("group", {
        name: "Cluster type *",
      });
      const clusterTypeOptions =
        within(clusterTypeSelect).getAllByRole("radio");

      expect(clusterTypeSelect).toBeEnabled();
      expect(clusterTypeOptions).toHaveLength(3);
    });

    it("renders Cluster name field", () => {
      const clusterNameInput = screen.getByRole("textbox", {
        name: "Cluster name *",
      });

      expect(clusterNameInput).toBeEnabled();
    });

    it("renders Protocol field", () => {
      const protocolSelect = screen.getByRole("combobox", {
        name: "Protocol *",
      });
      const protocolSelectOptions =
        within(protocolSelect).getAllByRole("option");

      expect(protocolSelect).toBeEnabled();
      expect(protocolSelectOptions).toHaveLength(7);
    });

    it("renders cluster type field", () => {
      const clusterTypeSelect = screen.getByRole("group", {
        name: "Cluster type *",
      });
      const clusterTypeOptions =
        within(clusterTypeSelect).getAllByRole("radio");

      expect(clusterTypeSelect).toBeEnabled();
      expect(clusterTypeOptions).toHaveLength(3);
    });

    it("renders Kafka flavor field", () => {
      const kafkaFlavorSelect = screen.getByRole("combobox", {
        name: "Kafka flavor *",
      });
      const kafkaFlavorSelectOptions =
        within(kafkaFlavorSelect).getAllByRole("option");

      expect(kafkaFlavorSelect).toBeEnabled();
      expect(kafkaFlavorSelectOptions).toHaveLength(6);
    });

    it("renders Bootstrap servers field", () => {
      const bootstrapServersInput = screen.getByRole("textbox", {
        name: "Bootstrap servers *",
      });

      expect(bootstrapServersInput).toBeEnabled();
    });

    it("renders REST API servers field", () => {
      const restServersInput = screen.getByRole("textbox", {
        name: "REST API servers (optional)",
      });

      expect(restServersInput).toBeEnabled();
    });

    it("renders Add new server button", () => {
      const submitButton = screen.getByRole("button", {
        name: "Add new cluster",
      });

      expect(submitButton).toBeEnabled();
    });
  });

  it("shows an error message when the cluster name is empty", async () => {
    const submitButton = await screen.findByRole("button", {
      name: "Add new cluster",
    });
    const kafkaFlavorSelect = await screen.findByRole("combobox", {
      name: "Kafka flavor *",
    });
    const protocolSelect = screen.getByRole("combobox", {
      name: "Protocol *",
    });

    await userEvent.selectOptions(kafkaFlavorSelect, "APACHE_KAFKA");
    await userEvent.selectOptions(protocolSelect, "PLAINTEXT");

    await userEvent.click(submitButton);

    const errorMessage = await screen.findByText("Required");
    expect(errorMessage).toBeVisible();
  });

  it("shows project name and service name fields when Kafka flavor 'Aiven for Apache Kafka' and cluster type 'kafka' are selected", async () => {
    const kafkaFlavorSelect = await screen.findByRole("combobox", {
      name: "Kafka flavor *",
    });
    const protocolSelect = screen.getByRole("combobox", {
      name: "Protocol *",
    });

    await userEvent.selectOptions(kafkaFlavorSelect, "AIVEN_FOR_APACHE_KAFKA");
    await userEvent.selectOptions(protocolSelect, "PLAINTEXT");

    const projectNameInput = screen.getByRole("textbox", {
      name: "Project name *",
    });
    const serviceNameInput = screen.getByRole("textbox", {
      name: "Service name *",
    });

    expect(projectNameInput).toBeVisible();
    expect(serviceNameInput).toBeVisible();
  });

  it("shows errors when project name or service name is missing when adding an Aiven for Apache Kafka cluster", async () => {
    const submitButton = await screen.findByRole("button", {
      name: "Add new cluster",
    });
    const kafkaFlavorSelect = await screen.findByRole("combobox", {
      name: "Kafka flavor *",
    });
    const clusterNameInput = screen.getByRole("textbox", {
      name: "Cluster name *",
    });
    const bootstrapServersInput = screen.getByRole("textbox", {
      name: "Bootstrap servers *",
    });

    await userEvent.type(clusterNameInput, "MyCluster");
    await userEvent.type(bootstrapServersInput, "sever:9090");
    await userEvent.selectOptions(kafkaFlavorSelect, "AIVEN_FOR_APACHE_KAFKA");

    const projectNameInput = screen.getByRole("textbox", {
      name: "Project name *",
    });
    const serviceNameInput = screen.getByRole("textbox", {
      name: "Service name *",
    });

    await userEvent.type(projectNameInput, "MyProject");
    await userEvent.click(submitButton);
    const serviceNameErrorMessage = screen.getByText("Required");

    expect(serviceNameErrorMessage).toBeVisible();

    await userEvent.clear(projectNameInput);
    await userEvent.type(serviceNameInput, "MyService");
    const projectNameErrorMessage = screen.getByText("Required");

    expect(projectNameErrorMessage).toBeVisible();
  });

  it("updates Protocol options when Kafka connect or Schema registry cluster type is selected", async () => {
    const kafkaConnectOption = screen.getByRole("radio", {
      name: "Kafka Connect",
    });
    const schemaRegistryOption = screen.getByRole("radio", {
      name: "Schema Registry",
    });
    const protocolSelect = screen.getByRole("combobox", {
      name: "Protocol *",
    });

    await userEvent.click(kafkaConnectOption);

    const kafkaConnectProtocolSelectOptions =
      within(protocolSelect).getAllByRole("option");
    expect(kafkaConnectProtocolSelectOptions).toHaveLength(2);

    await userEvent.click(schemaRegistryOption);

    const schemaRegistryProtocolSelectOptions =
      within(protocolSelect).getAllByRole("option");
    expect(schemaRegistryProtocolSelectOptions).toHaveLength(2);
  });

  it("shows an error when boostrap server is incorrect", async () => {
    const bootstrapServersInput = screen.getByRole("textbox", {
      name: "Bootstrap servers *",
    });

    await userEvent.type(bootstrapServersInput, "server19092");
    await userEvent.tab();

    const errorMessage = await screen.findByText(
      "Must be in the format name:port, where name is any string without space and port is an integer"
    );
    expect(errorMessage).toBeVisible();
  });

  it("shows an error when REST API server is incorrect", async () => {
    const restApiServersInput = screen.getByRole("textbox", {
      name: "REST API servers (optional)",
    });

    await userEvent.type(restApiServersInput, "hello");
    await userEvent.tab();

    const errorMessage = await screen.findByText("Invalid url");
    expect(errorMessage).toBeVisible();
  });

  it("submits the form with the correct data", async () => {
    const clusterNameInput = screen.getByRole("textbox", {
      name: "Cluster name *",
    });
    const protocolSelect = screen.getByRole("combobox", {
      name: "Protocol *",
    });
    const kafkaFlavorSelect = screen.getByRole("combobox", {
      name: "Kafka flavor *",
    });
    const bootstrapServersInput = screen.getByRole("textbox", {
      name: "Bootstrap servers *",
    });
    const submitButton = screen.getByRole("button", {
      name: "Add new cluster",
    });
    await userEvent.type(clusterNameInput, "MyCluster");
    await userEvent.selectOptions(kafkaFlavorSelect, "APACHE_KAFKA");
    await userEvent.selectOptions(protocolSelect, "PLAINTEXT");
    await userEvent.type(bootstrapServersInput, "server:9092");
    await userEvent.click(submitButton);

    expect(mockAddNewClusterRequest).toHaveBeenCalledWith({
      clusterType: "KAFKA",
      clusterName: "MyCluster",
      protocol: "PLAINTEXT",
      kafkaFlavor: "APACHE_KAFKA",
      bootstrapServers: "server:9092",
    });
    expect(mockedUseToast).toHaveBeenCalledWith({
      message: "Cluster successfully added",
      variant: "default",
      position: "bottom-left",
    });
    expect(mockedUsedNavigate).toHaveBeenCalledWith(
      "/configuration/clusters?search=MyCluster&showConnectHelp=true"
    );
  });

  it("renders error message when submitting fails", async () => {
    console.error = jest.fn();

    mockAddNewClusterRequest.mockRejectedValue(new Error("Request failed"));

    const clusterNameInput = screen.getByRole("textbox", {
      name: "Cluster name *",
    });
    const protocolSelect = screen.getByRole("combobox", {
      name: "Protocol *",
    });
    const kafkaFlavorSelect = screen.getByRole("combobox", {
      name: "Kafka flavor *",
    });
    const bootstrapServersInput = screen.getByRole("textbox", {
      name: "Bootstrap servers *",
    });
    const submitButton = screen.getByRole("button", {
      name: "Add new cluster",
    });
    await userEvent.selectOptions(kafkaFlavorSelect, "APACHE_KAFKA");
    await userEvent.selectOptions(protocolSelect, "PLAINTEXT");
    await userEvent.type(clusterNameInput, "MyCluster");

    await userEvent.type(bootstrapServersInput, "server:9092");
    await userEvent.click(submitButton);

    expect(mockAddNewClusterRequest).toHaveBeenCalledWith({
      clusterType: "KAFKA",
      clusterName: "MyCluster",
      protocol: "PLAINTEXT",
      kafkaFlavor: "APACHE_KAFKA",
      bootstrapServers: "server:9092",
    });
    expect(mockedUseToast).toHaveBeenCalledWith({
      message: "Could not add cluster: Request failed",
      variant: "danger",
      position: "bottom-left",
    });
    expect(mockedUsedNavigate).not.toHaveBeenCalled();

    console.error = originalConsoleError;
  });
});
