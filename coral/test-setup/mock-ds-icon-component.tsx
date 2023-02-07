// The Icon component is creating a lot of clutter in test output if not mocked out.
// If you want to check for an Icon to be rendered, use: `screen.[x]byTestId("ds-icon").`
// There should not be a need to check that the right icon element is passed,
// but in case you want to do that:
// the element queried with by test id can be checked for the icon with:
// expect(element).toHaveAttribute("data-icon", [ICON-IMPORTED-FROM-@aivenio/aquarium/dist/src/icons].body);

jest.mock("@aivenio/aquarium", () => {
  return {
    __esModule: true,
    ...jest.requireActual("@aivenio/aquarium"),
    Icon: jest.fn((props) => {
      return <div data-testid={"ds-icon"} data-icon={props.icon.body} />;
    }),
  };
});
