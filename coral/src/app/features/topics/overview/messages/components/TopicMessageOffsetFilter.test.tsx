import { cleanup, render, screen, within } from "@testing-library/react";
import { TopicMessageOffsetFilter } from "src/app/features/topics/overview/messages/components/TopicMessageOffsetFilter";
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
    screen.getByRole("radio", { name: "5" });
    screen.getByRole("radio", { name: "25" });
    screen.getByRole("radio", { name: "50" });
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
    within(screen.getByRole("group", { name: "Offset" }))
      .getAllByRole("radio")
      .forEach((option) => expect(option).toBeDisabled());
  });
});
