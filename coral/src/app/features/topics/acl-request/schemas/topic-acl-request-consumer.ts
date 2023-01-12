import { isIpRegex } from "src/app/features/topics/acl-request/schemas/validation";
import { z } from "zod";

const topicConsumerFormSchema = z
  .object({
    remarks: z.string().optional(),
    consumergroup: z
      .string()
      .min(1, { message: "Consumer group cannot be empty." }),
    aclIpPrincipleType: z.union([
      z.literal("IP_ADDRESS"),
      z.literal("PRINCIPAL"),
      z.literal("USERNAME"),
    ]),
    // acl_ip and acl_ssl are required, but they are mutually exclusive fields.
    // acl_ip is rendered when aclIpPrincipleType === "IP_ADDRESS"
    // acl_ssl is rendered when aclIpPrincipleType === "PRINCIPAL"
    // This means that without the .optional(), the form will always be invalid,
    // because one of these fields will always miss a value.
    // However, we do not want the form to be valid when a user does not enter valid values
    // So we use refine() on the schema
    acl_ip: z
      .array(z.string().regex(isIpRegex, { message: "Invalid IP address." }))
      .min(1, { message: "Enter at least one element." })
      .optional(),
    acl_ssl: z
      .array(z.string())
      .min(1, { message: "Enter at least one element." })
      .optional(),
    aclPatternType: z.literal("LITERAL"),
    transactionalId: z.string().optional(),
    topicname: z.string(),
    environment: z
      .string()
      .refine(
        (value) => value !== "placeholder",
        "Please select an environment."
      ),
    topictype: z.literal("Consumer"),
  })
  // We check if the user has entered valid values for acl_ssl or acl_ip
  .refine(({ aclIpPrincipleType, acl_ssl, acl_ip }) => {
    const hasValidIpPrincipleValues =
      aclIpPrincipleType === "IP_ADDRESS" &&
      acl_ip !== undefined &&
      acl_ip.length >= 1;
    const hasValidSSLPrincipleValues =
      aclIpPrincipleType === "PRINCIPAL" &&
      acl_ssl !== undefined &&
      acl_ssl.length >= 1;

    if (hasValidIpPrincipleValues || hasValidSSLPrincipleValues) {
      return true;
    }
    return false;
  });

export type TopicConsumerFormSchema = z.infer<typeof topicConsumerFormSchema>;
export default topicConsumerFormSchema;
