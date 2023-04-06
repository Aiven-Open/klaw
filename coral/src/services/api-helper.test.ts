import { convertQuery } from "src/services/api-helper";
import { KlawApiRequestQueryParameters } from "types/utils";

describe("convertQuery", () => {
  it("converts a query param to an object with string properties simple object", () => {
    const simpleObject = {
      name: "me",
      age: 99,
    };

    expect(convertQuery(simpleObject)).toMatchObject({
      name: "me",
      age: "99",
    });
  });

  it("converts a query param to an object with string properties nested object", () => {
    const queryFake: KlawApiRequestQueryParameters<"getTopics"> = {
      pageNo: "1",
      env: "DEV",
      teamId: 1,
    };

    expect(convertQuery(queryFake)).toMatchObject({
      pageNo: "1",
      env: "DEV",
      teamId: "1",
    });
  });
});
