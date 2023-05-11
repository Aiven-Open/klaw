import z from "zod";

const topicRequestFormSchema = z.object({
  environment: z.string({
    errorMap: () => ({
      message: "Selection Error: Please select an environment",
    }),
  }),
  topicname: z.string(),
  schemafull: z.string(),
  remarks: z.string().optional(),
});

type TopicRequestFormSchema = z.infer<typeof topicRequestFormSchema>;

export { topicRequestFormSchema };
export type { TopicRequestFormSchema };
