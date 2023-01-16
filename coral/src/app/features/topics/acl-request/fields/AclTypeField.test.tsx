import { cleanup, render } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AclTypeField from "src/app/features/topics/acl-request/fields/AclTypeField";

describe("AclTypeField", () => {
  const onSubmit = jest.fn();
  const onError = jest.fn();
  const handleChange = jest.fn();

  afterEach(() => {
    cleanup();
    onSubmit.mockClear();
    onError.mockClear();
  });

  it("renders AclTypeField with two options", () => {
    const result = render(
      <AclTypeField handleChange={handleChange} topicType={"Producer"} />
    );
    const radioButtons = result.getAllByRole("radio");
    expect(radioButtons).toHaveLength(2);
  });

  it("should render AclTypeField with Consumer option checked by default", () => {
    const result = render(
      <AclTypeField handleChange={handleChange} topicType={"Consumer"} />
    );
    const consumerRadioButton = result.getByLabelText("Consumer");
    const producerRadioButton = result.getByLabelText("Producer");
    expect(consumerRadioButton).toBeChecked();
    expect(producerRadioButton).not.toBeChecked();
  });

  it("should render AclTypeField with Producer option checked by default", () => {
    const result = render(
      <AclTypeField handleChange={handleChange} topicType={"Producer"} />
    );
    const consumerRadioButton = result.getByLabelText("Consumer");
    const producerRadioButton = result.getByLabelText("Producer");
    expect(producerRadioButton).toBeChecked();
    expect(consumerRadioButton).not.toBeChecked();
  });

  it("should call handleChange when options are clicked", async () => {
    const result = render(
      <AclTypeField handleChange={handleChange} topicType={"Producer"} />
    );
    const consumerRadioButton = result.getByLabelText("Consumer");

    await userEvent.click(consumerRadioButton);
    expect(handleChange).toBeCalled();
  });
});
