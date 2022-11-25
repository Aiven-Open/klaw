import { transformTeamNamesGetResponse } from "src/domain/team/team-transformer";

describe("team-transformer.ts", () => {
  describe("transformTopicApiResponse", () => {
    it("does not modify empty response", () => {
      expect(transformTeamNamesGetResponse([])).toEqual([]);
    });
    it("filters out 'All teams' option from response", () => {
      expect(
        transformTeamNamesGetResponse(["team-1", "All teams", "team-2"])
      ).toEqual(["team-1", "team-2"]);
    });
    it("does not modify response without 'All teams'", () => {
      const response = ["team-1", "team-2"];
      expect(transformTeamNamesGetResponse(response)).toEqual(response);
    });
  });
});
