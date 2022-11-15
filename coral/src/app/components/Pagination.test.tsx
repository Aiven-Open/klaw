// @TODO remove import from `pure` when custom cleanup is merged
import { cleanup, render } from "@testing-library/react/pure";
import { Pagination } from "src/app/components/Pagination";

describe("Pagination.tsx", () => {
  beforeAll(() => {
    render(<Pagination activePage={1} totalPages={5} />);
  });

  afterAll(cleanup);

  it.todo("shows the pagination");
});
