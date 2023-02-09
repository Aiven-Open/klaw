import { AclRequestsForApprover } from "src/domain/acl/acl-types";
import { KlawApiResponse } from "types/utils";

const transformAclRequestApiResponse = (
  data: KlawApiResponse<"getAclRequestsForApprover">
): AclRequestsForApprover => {
  if (data.length === 0) {
    return {
      totalPages: 0,
      currentPage: 0,
      entries: [],
    };
  }
  return {
    totalPages: Number(data[0].totalNoPages),
    currentPage: Number(data[0].currentPage),
    entries: data,
  };
};

export default transformAclRequestApiResponse;
