import { z } from "zod";

const topicProducerFormSchema = z.object({
  remarks: z.string().optional(),
  consumergroup: z.string().optional(),
  acl_ip: z.array(z.string()).optional(),
  acl_ssl: z.array(z.string()).optional(),
  aclPatternType: z.union([z.literal("LITERAL"), z.literal("PREFIXED")]),
  topicname: z.string(),
  environment: z.string(),
  teamname: z.string(),
  topictype: z.literal("Producer"),
  aclIpPrincipleType: z.union([
    z.literal("IP_ADDRESS"),
    z.literal("PRINCIPAL"),
    z.literal("USERNAME"),
  ]),
});

export type TopicProducerFormSchema = z.infer<typeof topicProducerFormSchema>;
export default topicProducerFormSchema;
