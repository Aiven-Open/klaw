import { z } from "zod";

const topicConsumerFormSchema = z.object({
  remarks: z.string().optional(),
  consumergroup: z.string(),
  acl_ip: z.array(z.string()).optional(),
  acl_ssl: z.array(z.string()).optional(),
  aclPatternType: z.literal("LITERAL"),
  transactionalId: z.string().optional(),
  topicname: z.string(),
  environment: z.string(),
  teamname: z.string(),
  topictype: z.literal("Consumer"),
  aclIpPrincipleType: z.union([
    z.literal("IP_ADDRESS"),
    z.literal("PRINCIPAL"),
    z.literal("USERNAME"),
  ]),
});

export type TopicConsumerFormSchema = z.infer<typeof topicConsumerFormSchema>;
export default topicConsumerFormSchema;
