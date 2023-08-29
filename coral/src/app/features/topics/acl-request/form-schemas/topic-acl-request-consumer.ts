import {
  aclIpPrincipleType,
  acl_ip,
  acl_ssl,
  environment,
  remarks,
  topicname,
  teamId,
} from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-shared-fields";
import {
  hasOnlyValidCharacters,
  validateAclPrincipleValue,
} from "src/app/features/topics/acl-request/form-schemas/validation";
import { z } from "zod";

const consumergroup = z
  .string()
  .min(1, { message: "Consumer group cannot be empty." })
  .max(150, { message: "Consumer group cannot be more than 150 characters." })
  .regex(hasOnlyValidCharacters, {
    message: "Only characters allowed: a-z, A-Z, 0-9, ., _,-.",
  });
const aclPatternType = z.literal("LITERAL");
const aclType = z.literal("CONSUMER");

const topicConsumerFormSchema = z
  .object({
    remarks,
    consumergroup,
    aclIpPrincipleType,
    acl_ip,
    acl_ssl,
    aclPatternType,
    topicname,
    environment,
    aclType,
    teamId,
  })
  // We check if the user has entered valid values for acl_ssl or acl_ip
  // We need two different refine fns because we need to specify the path for the message to be displayed correctly
  // And we can't know which path is relevant if we do both checks in the same refine
  .refine(
    ({ aclIpPrincipleType, acl_ip }) => {
      if (aclIpPrincipleType === "IP_ADDRESS") {
        return validateAclPrincipleValue(acl_ip);
      }
      return true;
    },
    { message: "Enter at least one element.", path: ["acl_ip"] }
  )
  .refine(
    ({ aclIpPrincipleType, acl_ssl }) => {
      if (aclIpPrincipleType === "PRINCIPAL") {
        return validateAclPrincipleValue(acl_ssl);
      }
      return true;
    },
    { message: "Enter at least one element.", path: ["acl_ssl"] }
  );

export type TopicConsumerFormSchema = z.infer<typeof topicConsumerFormSchema>;
export default topicConsumerFormSchema;
