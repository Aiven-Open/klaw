import { isIpRegex } from "src/app/features/topics/acl-request/form-schemas/validation";
import { z } from "zod";

// The errorMap usage in this schema is to allow react-hook-form to return the correct error
// When a user clicks Submit without filling all fields properly
// If error map is not provided, and the empty fields have not been touched,
// the zod resolver will return the default error messages for z.literal and z.union fields
// Which are not very helpful for a user
const remarks = z.string().optional();
const aclIpPrincipleType = z.union([
  z.literal("IP_ADDRESS", {
    errorMap: () => ({ message: "Please choose an option." }),
  }),
  z.literal("PRINCIPAL", {
    errorMap: () => ({ message: "Please choose an option." }),
  }),
  z.literal("USERNAME", {
    errorMap: () => ({ message: "Please choose an option." }),
  }),
]);

// acl_ip and acl_ssl are required, but they are mutually exclusive fields.
// acl_ip is rendered when aclIpPrincipleType === "IP_ADDRESS"
// acl_ssl is rendered when aclIpPrincipleType === "PRINCIPAL"
// This means that without the .optional(), the form will always be invalid,
// because one of these fields will always miss a value.
// However, we do not want the form to be valid when a user does not enter valid values
// So we use refine() on the schema
const acl_ip = z
  .array(z.string().regex(isIpRegex, { message: "Invalid IP address." }), {
    errorMap: () => ({ message: "Enter at least one element." }),
  })
  .min(1, { message: "Enter at least one element." })
  .max(15, { message: "Maximum 15 elements allowed." })
  .optional();
const acl_ssl = z
  .array(z.string(), {
    errorMap: () => ({ message: "Enter at least one element." }),
  })
  .min(1, { message: "Enter at least one element." })
  .max(5, { message: "Maximum 5 elements allowed." })
  .refine(
    (values) => {
      return values.find((value) => value.length < 3) === undefined;
    },
    {
      message: "Every element must have at least 3 characters.",
    }
  )
  .optional();
const aclPatternType = z.union([
  z.literal("LITERAL", {
    errorMap: () => ({ message: "Please choose an option." }),
  }),
  z.literal("PREFIXED", {
    errorMap: () => ({ message: "Please choose an option." }),
  }),
]);
const topicname = z.string().min(1, { message: "Please enter a prefix." });
const environment = z.string({
  errorMap: () => ({ message: "Please select an environment." }),
});

const teamId = z.number();

export {
  remarks,
  aclIpPrincipleType,
  acl_ip,
  acl_ssl,
  aclPatternType,
  topicname,
  environment,
  teamId,
};
