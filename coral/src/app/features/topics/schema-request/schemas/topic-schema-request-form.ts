import z from "zod";

const topicRequestFormSchema = z.object({
  environment: z.string().min(1, { message: "The environment is required." }),
  topicname: z.string(),
  schemafull: z.string(),
  remarks: z.string().optional(),
});

type TopicRequestFormSchema = z.infer<typeof topicRequestFormSchema>;

export { topicRequestFormSchema };
export type { TopicRequestFormSchema };
