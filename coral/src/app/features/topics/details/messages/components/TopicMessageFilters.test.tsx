import { cleanup, render, screen, within } from "@testing-library/react";
import { userEvent } from "@testing-library/user-event";
import { TopicMessageFilters } from "src/app/features/topics/details/messages/components/TopicMessageFilters";

describe("TopicMessageFilters", () => {
  describe("mode: default", () => {
    afterEach(() => {
      cleanup();
    });
    it("displays 5, 25 and 50 as possible offset values as options", () => {
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "25",
            customOffset: null,
            partitionId: null,
            rangeOffsetStart: null,
            rangeOffsetEnd: null,
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          onRangeOffsetStartChange={jest.fn()}
          onRangeOffsetEndChange={jest.fn()}
          disabled={false}
          mode={"default"}
          filterErrors={{
            partitionIdFilters: null,
            customOffsetFilters: null,
            rangeOffsetStartFilters: null,
            rangeOffsetEndFilters: null,
          }}
        />
      );
      expect(screen.getByRole("radio", { name: "5" })).toBeVisible();
      expect(screen.getByRole("radio", { name: "25" })).toBeVisible();
      expect(screen.getByRole("radio", { name: "50" })).toBeVisible();
    });

    it("is possible to select an option", async () => {
      const onDefaultOffsetChange = jest.fn();
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "25",
            customOffset: null,
            partitionId: null,
            rangeOffsetStart: null,
            rangeOffsetEnd: null,
          }}
          onDefaultOffsetChange={onDefaultOffsetChange}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          onRangeOffsetStartChange={jest.fn()}
          onRangeOffsetEndChange={jest.fn()}
          disabled={false}
          mode={"default"}
          filterErrors={{
            partitionIdFilters: null,
            customOffsetFilters: null,
            rangeOffsetStartFilters: null,
            rangeOffsetEndFilters: null,
          }}
        />
      );
      await userEvent.click(screen.getByRole("radio", { name: "50" }));
      expect(onDefaultOffsetChange).toHaveBeenCalledTimes(1);
      expect(onDefaultOffsetChange).toHaveBeenCalledWith("50");
    });

    it("is not possible to select an option if the input is disabled", () => {
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "25",
            customOffset: null,
            partitionId: null,
            rangeOffsetStart: null,
            rangeOffsetEnd: null,
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          onRangeOffsetStartChange={jest.fn()}
          onRangeOffsetEndChange={jest.fn()}
          disabled={true}
          mode={"default"}
          filterErrors={{
            partitionIdFilters: null,
            customOffsetFilters: null,
            rangeOffsetStartFilters: null,
            rangeOffsetEndFilters: null,
          }}
        />
      );
      within(
        screen.getByRole("group", {
          name: "Number of messages Select number of messages to display from this topic",
        })
      )
        .getAllByRole("radio")
        .forEach((option) => expect(option).toBeDisabled());
    });
  });
  describe("mode: custom", () => {
    afterEach(() => {
      cleanup();
    });
    it("displays required Partion ID and Cusom offset fields", () => {
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "custom",
            customOffset: null,
            partitionId: null,
            rangeOffsetStart: null,
            rangeOffsetEnd: null,
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          onRangeOffsetStartChange={jest.fn()}
          onRangeOffsetEndChange={jest.fn()}
          disabled={false}
          mode={"custom"}
          filterErrors={{
            partitionIdFilters: null,
            customOffsetFilters: null,
            rangeOffsetStartFilters: null,
            rangeOffsetEndFilters: null,
          }}
        />
      );
      expect(
        screen.getByRole("spinbutton", {
          name: "Partition ID * Enter partition ID to retrieve last messages",
        })
      ).toBeVisible();
      expect(
        screen.getByRole("spinbutton", {
          name: "Number of messages * Set the number of recent messages to display from this partition",
        })
      ).toBeVisible();
    });

    it("is possible to enter values in fields for custom select mode", async () => {
      const onPartitionIdChange = jest.fn();
      const onCustomOffsetChange = jest.fn();
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "custom",
            customOffset: null,
            partitionId: null,
            rangeOffsetStart: null,
            rangeOffsetEnd: null,
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={onPartitionIdChange}
          onCustomOffsetChange={onCustomOffsetChange}
          onRangeOffsetStartChange={jest.fn()}
          onRangeOffsetEndChange={jest.fn()}
          disabled={false}
          mode={"custom"}
          filterErrors={{
            partitionIdFilters: null,
            customOffsetFilters: null,
            rangeOffsetStartFilters: null,
            rangeOffsetEndFilters: null,
          }}
        />
      );

      const partitionIdInput = screen.getByRole("spinbutton", {
        name: "Partition ID * Enter partition ID to retrieve last messages",
      });
      const customOffsetInput = screen.getByRole("spinbutton", {
        name: "Number of messages * Set the number of recent messages to display from this partition",
      });

      await userEvent.type(partitionIdInput, "1");
      await userEvent.type(customOffsetInput, "10");
      expect(onPartitionIdChange).toHaveBeenCalledWith("1");
      expect(onCustomOffsetChange).toHaveBeenCalledWith("10");
    });
  });

  describe("mode: range", () => {
    afterEach(() => {
      cleanup();
    });
    it("displays required Partion ID and Range offset start and end fields", () => {
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "range",
            customOffset: null,
            partitionId: null,
            rangeOffsetStart: null,
            rangeOffsetEnd: null,
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          onRangeOffsetStartChange={jest.fn()}
          onRangeOffsetEndChange={jest.fn()}
          disabled={false}
          mode={"range"}
          filterErrors={{
            partitionIdFilters: null,
            customOffsetFilters: null,
            rangeOffsetStartFilters: null,
            rangeOffsetEndFilters: null,
          }}
        />
      );
      expect(
        screen.getByRole("spinbutton", {
          name: "Partition ID * Enter partition ID to retrieve last messages",
        })
      ).toBeVisible();
      expect(
        screen.getByRole("spinbutton", {
          name: "Start Offset * Set the start offset",
        })
      ).toBeVisible();
      expect(
        screen.getByRole("spinbutton", {
          name: "End Offset * Set the end offset",
        })
      ).toBeVisible();
    });

    it("is possible to enter values in fields for range select mode", async () => {
      const onPartitionIdChange = jest.fn();
      const onRangeOffsetStartChange = jest.fn();
      const onRangeOffsetEndChange = jest.fn();
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "range",
            customOffset: null,
            partitionId: null,
            rangeOffsetStart: null,
            rangeOffsetEnd: null,
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={onPartitionIdChange}
          onCustomOffsetChange={jest.fn()}
          onRangeOffsetStartChange={onRangeOffsetStartChange}
          onRangeOffsetEndChange={onRangeOffsetEndChange}
          disabled={false}
          mode={"range"}
          filterErrors={{
            partitionIdFilters: null,
            customOffsetFilters: null,
            rangeOffsetStartFilters: null,
            rangeOffsetEndFilters: null,
          }}
        />
      );

      const partitionIdInput = screen.getByRole("spinbutton", {
        name: "Partition ID * Enter partition ID to retrieve last messages",
      });
      const startOffsetInput = screen.getByRole("spinbutton", {
        name: "Start Offset * Set the start offset",
      });
      const endOffsetInput = screen.getByRole("spinbutton", {
        name: "End Offset * Set the end offset",
      });

      await userEvent.type(partitionIdInput, "1");
      await userEvent.type(startOffsetInput, "5");
      await userEvent.type(endOffsetInput, "10");
      expect(onPartitionIdChange).toHaveBeenCalledWith("1");
      expect(onRangeOffsetStartChange).toHaveBeenCalledWith("5");
      expect(onRangeOffsetEndChange).toHaveBeenCalledWith("10");
    });
  });
});
