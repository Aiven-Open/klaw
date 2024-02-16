import { z } from "zod";

const clusterType = z.enum(["KAFKA", "SCHEMA_REGISTRY", "KAFKA_CONNECT"]);

const protocol = z.enum([
  "PLAINTEXT",
  "SSL",
  "SASL_PLAIN",
  "SASL_SSL_PLAIN_MECHANISM",
  "SASL_SSL_GSSAPI_MECHANISM",
  "SASL_SSL_SCRAM_MECHANISM_256",
  "SASL_SSL_SCRAM_MECHANISM_512",
]);

const kafkaFlavor = z.enum([
  "APACHE_KAFKA",
  "AIVEN_FOR_APACHE_KAFKA",
  "CONFLUENT",
  "CONFLUENT_CLOUD",
  "OTHERS",
]);

const bootstrapServers = z
  .array(z.string(), {
    errorMap: () => ({ message: "Enter at least one server" }),
  })
  .refine(
    (values) => {
      return (
        values.find((value) => {
          const [name, port] = value.split(":");
          return name.includes(" ") || !Number.isInteger(Number(port));
        }) === undefined
      );
    },
    {
      message:
        "Must be in the format name:port, where name is any string without space and port is an integer",
    }
  );

const clusterName = z
  .string()
  .min(1, "Required")
  .refine((value) => !value.includes(" "), {
    message: "Must not contain any space",
  });

const associatedServers = z.string().url();

const addNewClusterFormSchema = z
  .object({
    clusterName,
    bootstrapServers,
    // REST API URL is optional
    associatedServers: z.array(associatedServers).optional(),
    clusterType,
    protocol,
    kafkaFlavor,
    // projectName and serviceName are only required in the case handled in superRefine
    projectName: z.string().optional(),
    serviceName: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    if (
      data.clusterType === "KAFKA" &&
      data.kafkaFlavor === "AIVEN_FOR_APACHE_KAFKA"
    ) {
      if (data.projectName === undefined || data.projectName === "") {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["projectName"],
          message: "Required",
        });
      }
      if (data.serviceName === undefined || data.serviceName === "") {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["serviceName"],
          message: "Required",
        });
      }
      return data.projectName && data.serviceName;
    }
    return true;
  });

type AddNewClusterFormSchema = z.infer<typeof addNewClusterFormSchema>;

export { addNewClusterFormSchema };
export type { AddNewClusterFormSchema };
