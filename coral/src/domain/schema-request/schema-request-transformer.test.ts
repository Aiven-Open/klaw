import { transformGetSchemaRequestsForApproverResponse } from "src/domain/schema-request/schema-request-transformer";
import {
  SchemaRequest,
  SchemaRequestApiResponse,
} from "src/domain/schema-request/schema-request-types";

describe("schema-request-transformer.ts", () => {
  describe("transformGetSchemaRequestsForApproverResponse", () => {
    it("transforms empty payload into empty array", () => {
      const transformedResponse = transformGetSchemaRequestsForApproverResponse(
        []
      );

      const result: SchemaRequestApiResponse = {
        totalPages: 0,
        currentPage: 0,
        entries: [],
      };

      expect(transformedResponse).toEqual(result);
    });

    it("transforms all response items into expected type", () => {
      const mockedResponse: SchemaRequest[] = [
        {
          req_no: 1014,
          topicname: "testtopic-first",
          environment: "1",
          environmentName: "BRG",
          schemaversion: "1.0",
          teamname: "NCC1701D",
          teamId: 1701,
          appname: "App",
          schemafull: "",
          username: "jlpicard",
          requesttime: "1987-09-28T13:37:00.001+00:00",
          requesttimestring: "28-Sep-1987 13:37:00",
          requestStatus: "CREATED",
          requestOperationType: "CREATE",
          remarks: "asap",
          approvingTeamDetails:
            "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
          approvingtime: "2022-11-04T14:54:13.414+00:00",
          totalNoPages: "4",
          allPageNos: ["1"],
          currentPage: "1",
          deletable: false,
          editable: false,
          forceRegister: false,
        },
        {
          req_no: 1013,
          topicname: "testtopic-second",
          environment: "2",
          environmentName: "SEC",
          schemaversion: "1.0",
          teamname: "NCC1701D",
          teamId: 1701,
          appname: "App",
          schemafull: "",
          username: "bcrusher",
          requesttime: "1994-23-05T13:37:00.001+00:00",
          requesttimestring: "23-May-1994 13:37:00",
          requestStatus: "CREATED",
          requestOperationType: "DELETE",
          remarks: "asap",
          approvingTeamDetails:
            "Team : NCC1701D, Users : jlpicard, worf, bcrusher, geordilf",
          approvingtime: "2022-11-04T14:54:13.414+00:00",
          totalNoPages: "4",
          allPageNos: ["1"],
          currentPage: "1",
          deletable: false,
          editable: false,
          forceRegister: false,
        },
      ];
      const transformedResponse =
        transformGetSchemaRequestsForApproverResponse(mockedResponse);

      const result: SchemaRequestApiResponse = {
        totalPages: 4,
        currentPage: 1,
        entries: mockedResponse,
      };

      expect(transformedResponse.entries).toHaveLength(2);
      expect(transformedResponse).toEqual(result);
    });
  });
});
