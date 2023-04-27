import {
  remarks,
  aclIpPrincipleType,
  aclPatternType,
  acl_ip,
  acl_ssl,
  topicname,
  environment,
  teamId,
} from "src/app/features/topics/acl-request/form-schemas/topic-acl-request-shared-fields";
import {
  hasOnlyValidCharacters,
  validateAclPrincipleValue,
} from "src/app/features/topics/acl-request/form-schemas/validation";
import { z } from "zod";

const aclType = z.literal("PRODUCER");
const transactionalId = z
  .string()
  .regex(hasOnlyValidCharacters, {
    message: "Only characters allowed: a-z, A-Z, 0-9, ., _,-.",
  })
  .max(150, { message: "Transactional ID cannot be more than 150 characters." })
  .optional();

const topicProducerFormSchema = z
  .object({
    remarks,
    aclIpPrincipleType,
    acl_ip,
    acl_ssl,
    aclPatternType,
    topicname,
    environment,
    aclType,
    transactionalId,
    teamId,
  })
  // We check if the user has entered valid values for acl_ssl or acl_ip
  // We need two different refine fns because we need to specify the path for the message to be dispalyed correctly
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

export type TopicProducerFormSchema = z.infer<typeof topicProducerFormSchema>;
export default topicProducerFormSchema;
