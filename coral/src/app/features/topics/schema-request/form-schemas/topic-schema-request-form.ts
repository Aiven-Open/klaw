import { z } from "zod";

const topicRequestFormSchema = z.object({
  environment: z
    .string({
      errorMap: () => ({
        message: "Selection Error: Please select an environment",
      }),
    })
    // min(1) so that an empty selection ("") is rejected with the same
    // message, not a string-length message
    .min(1, { message: "Selection Error: Please select an environment" }),
  topicname: z.string(),
  schemafull: z.string(),
  remarks: z.string().optional(),
  forceRegister: z.boolean().optional(),
  schemaType: z.enum(["JSON", "AVRO"]),
});

type TopicRequestFormSchema = z.infer<typeof topicRequestFormSchema>;

export { topicRequestFormSchema };
export type { TopicRequestFormSchema };
