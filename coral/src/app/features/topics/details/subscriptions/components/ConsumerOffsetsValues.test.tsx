import { cleanup, screen } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { ConsumerOffsetsValues } from "src/app/features/topics/details/subscriptions/components/ConsumerOffsetsValues";
import { getConsumerOffsets } from "src/domain/acl/acl-api";
import { ConsumerOffsets } from "src/domain/acl/acl-types";
import { customRender } from "src/services/test-utils/render-with-wrappers";

jest.mock("src/domain/acl/acl-api");

const mockGetConsumerOffsets = getConsumerOffsets as jest.MockedFunction<
  typeof getConsumerOffsets
>;

const testOffsetsDataOnePartition: ConsumerOffsets[] = [
  {
    topicPartitionId: "0",
    currentOffset: "0",
    endOffset: "0",
    lag: "0",
  },
];

const testOffsetsDataTwoPartitions: ConsumerOffsets[] = [
  {
    topicPartitionId: "0",
    currentOffset: "0",
    endOffset: "0",
    lag: "0",
  },
  {
    topicPartitionId: "1",
    currentOffset: "0",
    endOffset: "0",
    lag: "0",
  },
];

const testOffsetsNoData: ConsumerOffsets[] = [];

const props = {
  topicName: "aivtopic3",
  consumerGroup: "-na-",
  environment: "1",
  setError: jest.fn(),
};

describe("ConsumerOffsetValues.tsx", () => {
  beforeEach(() => {
    customRender(<ConsumerOffsetsValues {...props} />, {
      queryClient: true,
    });
  });
  afterEach(() => {
    jest.resetAllMocks();
    cleanup();
  });

  it("does not call getConsumerOffsets on load", () => {
    expect(mockGetConsumerOffsets).not.toHaveBeenCalled();
  });

  it("renders correct initial state", () => {
    const fetchButton = screen.getByRole("button", {
      name: "Fetch the consumer offset lag of the current subscription",
    });
    const offsetLagText = screen.getByText("Fetch offset lag to display data.");

    expect(fetchButton).toBeEnabled();
    expect(offsetLagText).toBeVisible();
  });

  it("renders Consumer offset lag when clicking Fetch offset lag button (one partition)", async () => {
    mockGetConsumerOffsets.mockResolvedValue(testOffsetsDataOnePartition);

    const fetchButton = screen.getByRole("button", {
      name: "Fetch the consumer offset lag of the current subscription",
    });

    await userEvent.click(fetchButton);

    const offsets = screen.getByText(
      "Partition 0: Current offset 0 | End offset 0 | Lag 0"
    );
    expect(offsets).toBeVisible();

    const refetchButton = screen.getByRole("button", {
      name: "Refetch the consumer offset lag of the current subscription",
    });

    expect(refetchButton).toBeEnabled();
  });

  it("renders Consumer offset lag when clicking Fetch offset lag button (two partitions)", async () => {
    mockGetConsumerOffsets.mockResolvedValue(testOffsetsDataTwoPartitions);

    const fetchButton = screen.getByRole("button", {
      name: "Fetch the consumer offset lag of the current subscription",
    });

    await userEvent.click(fetchButton);

    const offsetsPartitionOne = screen.getByText(
      "Partition 0: Current offset 0 | End offset 0 | Lag 0"
    );
    const offsetsPartitionTwo = screen.getByText(
      "Partition 1: Current offset 0 | End offset 0 | Lag 0"
    );
    expect(offsetsPartitionOne).toBeVisible();
    expect(offsetsPartitionTwo).toBeVisible();

    const refetchButton = screen.getByRole("button", {
      name: "Refetch the consumer offset lag of the current subscription",
    });

    expect(refetchButton).toBeEnabled();
  });

  it("renders no data message when clicking Fetch offset lag button (no data)", async () => {
    mockGetConsumerOffsets.mockResolvedValue(testOffsetsNoData);

    const fetchButton = screen.getByRole("button", {
      name: "Fetch the consumer offset lag of the current subscription",
    });

    await userEvent.click(fetchButton);

    const noOffsets = screen.getByText("No offset lag is currently retained.");

    expect(noOffsets).toBeVisible();

    const refetchButton = screen.getByRole("button", {
      name: "Refetch the consumer offset lag of the current subscription",
    });

    expect(refetchButton).toBeEnabled();
  });
});
