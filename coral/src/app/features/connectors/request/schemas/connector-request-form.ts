import attempt from "lodash/attempt";
import isError from "lodash/isError";
import z from "zod";

const connectorRequestFormSchema = z.object({
  environment: z.string().min(1, { message: "Please select an environment" }),
  connectorName: z
    .string()
    .min(5, { message: "Connector name must be at least 5 characters" }),
  connectorConfig: z
    .string()
    // Refines for JSON object and for each of the three required config properties
    // Only checking the presence of the properties, not their values
    .refine((value) => !isError(attempt(() => JSON.parse(value))), {
      message: "Must be valid JSON",
    })
    .refine((value) => value.startsWith("{") && value.endsWith("}"), {
      message: "Must be a JSON Object",
    })
    .refine((value) => "tasks.max" in attempt(() => JSON.parse(value)), {
      message: 'Missing "tasks.max" configuration property.',
    })
    .refine((value) => "connector.class" in attempt(() => JSON.parse(value)), {
      message: 'Missing "connector.class" configuration property.',
    })
    .refine(
      (value) => {
        const json = attempt(() => JSON.parse(value));
        return "topics" in json || "topics.regex" in json;
      },
      { message: 'Missing "topics" or "topics.regex" configuration property.' }
    ),
  description: z
    .string()
    .min(5, { message: "Connector description must be at least 5 characters" }),
  remarks: z.string().optional(),
});

type ConnectorRequestFormSchema = z.infer<typeof connectorRequestFormSchema>;

export { connectorRequestFormSchema };
export type { ConnectorRequestFormSchema };
