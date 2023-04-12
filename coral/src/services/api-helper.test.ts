import { convertQueryValuesToString } from "src/services/api-helper";
import { KlawApiRequestQueryParameters } from "types/utils";

describe("convertQueryValuesToString", () => {
  it("converts a simple query param to an object with string properties", () => {
    const queryFake: KlawApiRequestQueryParameters<"getTopics"> = {
      pageNo: "1",
      env: "DEV",
      teamId: 1,
    };

    expect(convertQueryValuesToString(queryFake)).toStrictEqual({
      pageNo: "1",
      env: "DEV",
      teamId: "1",
    });
  });

  it("converts a nested query param to an object with string properties nested object", () => {
    const queryFake = {
      pageNo: "1",
      env: "DEV",
      teamId: 1,
      environments: {
        1: {
          name: "Dev",
          editable: false,
        },
        2: {
          name: "Test",
          editable: true,
        },
      },
    };

    expect(convertQueryValuesToString(queryFake)).toStrictEqual({
      pageNo: "1",
      env: "DEV",
      teamId: "1",
      environments: {
        "1": {
          name: "Dev",
          editable: "false",
        },
        "2": {
          name: "Test",
          editable: "true",
        },
      },
    });
  });
});
