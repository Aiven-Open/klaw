import { cleanup, render, screen, within } from "@testing-library/react";
import { TopicMessageFilters } from "src/app/features/topics/details/messages/components/TopicMessageFilters";
import { userEvent } from "@testing-library/user-event";

describe("TopicMessageFilters", () => {
  describe("mode: Default", () => {
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
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          disabled={false}
          mode={"Default"}
          filterErrors={{ partitionIdFilters: null, customOffsetFilters: null }}
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
          }}
          onDefaultOffsetChange={onDefaultOffsetChange}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          disabled={false}
          mode={"Default"}
          filterErrors={{ partitionIdFilters: null, customOffsetFilters: null }}
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
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          disabled={true}
          mode={"Default"}
          filterErrors={{ partitionIdFilters: null, customOffsetFilters: null }}
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
  describe("mode: Custom", () => {
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
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={jest.fn()}
          onCustomOffsetChange={jest.fn()}
          disabled={false}
          mode={"Custom"}
          filterErrors={{ partitionIdFilters: null, customOffsetFilters: null }}
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

    it("is possible to enter values in fields", async () => {
      const onPartitionIdChange = jest.fn();
      const onCustomOffsetChange = jest.fn();
      render(
        <TopicMessageFilters
          values={{
            defaultOffset: "custom",
            customOffset: null,
            partitionId: null,
          }}
          onDefaultOffsetChange={jest.fn()}
          onPartitionIdChange={onPartitionIdChange}
          onCustomOffsetChange={onCustomOffsetChange}
          disabled={false}
          mode={"Custom"}
          filterErrors={{ partitionIdFilters: null, customOffsetFilters: null }}
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
});
