import z from "zod";

const connectorRequestFormSchema = z.object({
  environment: z
    .string()
    .min(1, { message: "Selection Error: Please select an environment." }),
  connectorName: z.string().min(5),
  connectorConfig: z
    .string()
    // Three refines for each of the three required config properties
    // Only checking the presence of the properties, not their values
    .refine(
      (value) => {
        return value.includes("tasks.max");
      },
      { message: `Missing "tasks.max" configuration property.` }
    )
    .refine(
      (value) => {
        return value.includes("connector.class");
      },
      { message: `Missing "connector.class" configuration property.` }
    )
    .refine(
      (value) => {
        return value.includes("topics");
      },
      { message: `Missing "topics" or "topics.regex" configuration property.` }
    ),
  description: z.string().min(5),
  remarks: z.string().optional(),
});

type ConnectorRequestFormSchema = z.infer<typeof connectorRequestFormSchema>;

export { connectorRequestFormSchema };
export type { ConnectorRequestFormSchema };
