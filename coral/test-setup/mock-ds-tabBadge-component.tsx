// This is necessary because otherwise Jest cannot resolve the TabBadge import
// SyntaxError: Cannot use import statement outside a module

//       1 | import { Box, Icon, Link } from "@aivenio/aquarium";
//       2 | import data from "@aivenio/aquarium/dist/src/icons/console";
//     > 3 | import { TabBadge } from "@aivenio/aquarium/dist/src/molecules/Badge/Badge";
//         |T^
jest.mock("@aivenio/aquarium/dist/src/molecules/Badge/Badge", () => {
  return {
    __esModule: true,
    TabBadge: jest.fn(({ value }: { value: number }) => {
      return <div data-testid={"ds-tabBadge"}>{value}</div>;
    }),
  };
});
