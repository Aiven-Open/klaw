import { convertQueryValuesToString } from "src/services/api-helper";
import { KlawApiRequestQueryParameters } from "types/utils";

describe("convertQueryValuesToString", () => {
  it("converts a query param to an object with string properties simple object", () => {
    const simpleObject = {
      name: "me",
      age: 99,
    };

    expect(convertQueryValuesToString(simpleObject)).toMatchObject({
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

    expect(convertQueryValuesToString(queryFake)).toMatchObject({
      pageNo: "1",
      env: "DEV",
      teamId: "1",
    });
  });
});
