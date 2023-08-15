import { cleanup, render, screen, within } from "@testing-library/react";
import { TopicMessageOffsetFilter } from "src/app/features/topics/details/messages/components/TopicMessageOffsetFilter";
import userEvent from "@testing-library/user-event";

describe("TopicMessageOffsetFilter", () => {
  afterEach(() => {
    cleanup();
  });
  it("displays 5, 25 and 50 as possible offset values as options", () => {
    render(
      <TopicMessageOffsetFilter
        value="25"
        onChange={jest.fn()}
        disabled={false}
      />
    );
    expect(screen.getByRole("radio", { name: "5" })).toBeVisible();
    expect(screen.getByRole("radio", { name: "25" })).toBeVisible();
    expect(screen.getByRole("radio", { name: "50" })).toBeVisible();
  });

  it("is possible to select an option", async () => {
    const onChange = jest.fn();
    render(
      <TopicMessageOffsetFilter
        value="25"
        onChange={onChange}
        disabled={false}
      />
    );
    await userEvent.click(screen.getByRole("radio", { name: "50" }));
    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith("50");
  });

  it("is not possible to select an option if the input is disabled", () => {
    render(
      <TopicMessageOffsetFilter
        value="25"
        onChange={jest.fn()}
        disabled={true}
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
