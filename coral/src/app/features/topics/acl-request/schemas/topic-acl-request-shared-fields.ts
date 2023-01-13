import { isIpRegex } from "src/app/features/topics/acl-request/schemas/validation";
import { z } from "zod";

const remarks = z.string().optional();
const aclIpPrincipleType = z.union([
  z.literal("IP_ADDRESS"),
  z.literal("PRINCIPAL"),
  z.literal("USERNAME"),
]);
// acl_ip and acl_ssl are required, but they are mutually exclusive fields.
// acl_ip is rendered when aclIpPrincipleType === "IP_ADDRESS"
// acl_ssl is rendered when aclIpPrincipleType === "PRINCIPAL"
// This means that without the .optional(), the form will always be invalid,
// because one of these fields will always miss a value.
// However, we do not want the form to be valid when a user does not enter valid values
// So we use refine() on the schema
const acl_ip = z
  .array(z.string().regex(isIpRegex, { message: "Invalid IP address." }))
  .min(1, { message: "Enter at least one element." })
  .optional();
const acl_ssl = z
  .array(z.string())
  .min(1, { message: "Enter at least one element." })
  .optional();
const aclPatternType = z.union([z.literal("LITERAL"), z.literal("PREFIXED")]);
const topicname = z.string().min(1, { message: "Please enter a prefix." });
const environment = z
  .string()
  .refine((value) => value !== "placeholder", "Please select an environment");

export {
  remarks,
  aclIpPrincipleType,
  acl_ip,
  acl_ssl,
  aclPatternType,
  topicname,
  environment,
};
