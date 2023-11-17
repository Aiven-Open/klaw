import { cleanup, render, screen, within } from "@testing-library/react";
import { TopicMessageFilters } from "src/app/features/topics/details/messages/components/TopicMessageFilters";
import { userEvent } from "@testing-library/user-event";

describe("TopicMessageFilters", () => {
  afterEach(() => {
    cleanup();
  });
  it("displays 5, 25 and 50 as possible offset values as options", () => {
    render(
      <TopicMessageFilters
        values={{ defaultOffset: "25", customOffset: null, partitionId: null }}
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
    const onChange = jest.fn();
    render(
      <TopicMessageFilters
        values={{ defaultOffset: "25", customOffset: null, partitionId: null }}
        onDefaultOffsetChange={onChange}
        onPartitionIdChange={onChange}
        onCustomOffsetChange={onChange}
        disabled={false}
        mode={"Default"}
        filterErrors={{ partitionIdFilters: null, customOffsetFilters: null }}
      />
    );
    await userEvent.click(screen.getByRole("radio", { name: "50" }));
    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith("50");
  });

  it("is not possible to select an option if the input is disabled", () => {
    render(
      <TopicMessageFilters
        values={{ defaultOffset: "25", customOffset: null, partitionId: null }}
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
        name: "Number of messages Choose how many recent messages you want to view from this topic.",
      })
    )
      .getAllByRole("radio")
      .forEach((option) => expect(option).toBeDisabled());
  });
});
