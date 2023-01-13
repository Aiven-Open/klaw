import {
  remarks,
  aclIpPrincipleType,
  acl_ip,
  acl_ssl,
  topicname,
  environment,
} from "src/app/features/topics/acl-request/schemas/topic-acl-request-shared-fields";
import { validateAclPrincipleValue } from "src/app/features/topics/acl-request/schemas/validation";
import { z } from "zod";

const aclPatternType = z.union([z.literal("LITERAL"), z.literal("PREFIXED")]);
const topictype = z.literal("Producer");
const transactionalId = z.string().optional();

const topicProducerFormSchema = z
  .object({
    remarks,
    aclIpPrincipleType,
    acl_ip,
    acl_ssl,
    aclPatternType,
    topicname,
    environment,
    topictype,
    transactionalId,
  })
  // We check if the user has entered valid values for acl_ssl or acl_ip
  .refine(({ aclIpPrincipleType, acl_ssl, acl_ip }) => {
    if (aclIpPrincipleType === "IP_ADDRESS") {
      return validateAclPrincipleValue(acl_ip);
    }
    if (aclIpPrincipleType === "PRINCIPAL") {
      return validateAclPrincipleValue(acl_ssl);
    }

    return false;
  });

export type TopicProducerFormSchema = z.infer<typeof topicProducerFormSchema>;
export default topicProducerFormSchema;
