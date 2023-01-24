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

const consumergroup = z
  .string()
  .min(1, { message: "Consumer group cannot be empty." });
const aclPatternType = z.literal("LITERAL");
const topictype = z.literal("Consumer");

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
    topictype,
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

export type TopicConsumerFormSchema = z.infer<typeof topicConsumerFormSchema>;
export default topicConsumerFormSchema;
