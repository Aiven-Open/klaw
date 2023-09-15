import { doesEntityRelatedToRequestExists } from "src/services/entity-exists/entity-related-to-request-exists";

describe("doesEntityRelatedToRequestExists", () => {
  describe.each`
    type         | status        | exists
    ${"CLAIM"}   | ${"APPROVED"} | ${true}
    ${"CREATE"}  | ${"APPROVED"} | ${true}
    ${"DELETE"}  | ${"APPROVED"} | ${false}
    ${"PROMOTE"} | ${"APPROVED"} | ${true}
    ${"UPDATE"}  | ${"APPROVED"} | ${true}
    ${"CLAIM"}   | ${"CREATED"}  | ${true}
    ${"CREATE"}  | ${"CREATED"}  | ${false}
    ${"DELETE"}  | ${"CREATED"}  | ${true}
    ${"PROMOTE"} | ${"CREATED"}  | ${true}
    ${"UPDATE"}  | ${"CREATED"}  | ${true}
    ${"CLAIM"}   | ${"DECLINED"} | ${true}
    ${"CREATE"}  | ${"DECLINED"} | ${false}
    ${"DELETE"}  | ${"DECLINED"} | ${true}
    ${"PROMOTE"} | ${"DECLINED"} | ${true}
    ${"UPDATE"}  | ${"DECLINED"} | ${true}
    ${"CLAIM"}   | ${"DELETED"}  | ${true}
    ${"CREATE"}  | ${"DELETED"}  | ${false}
    ${"DELETE"}  | ${"DELETED"}  | ${true}
    ${"PROMOTE"} | ${"DELETED"}  | ${true}
    ${"UPDATE"}  | ${"DELETED"}  | ${true}
  `(
    "type: $type, status: $status, exists: $exists",
    ({ type, status, exists }) => {
      it(`returns ${exists} when type is ${type}, status is ${status}`, () => {
        const result = doesEntityRelatedToRequestExists({
          requestStatus: status,
          requestOperationType: type,
        });

        expect(result).toBe(exists);
      });
    }
  );
});
