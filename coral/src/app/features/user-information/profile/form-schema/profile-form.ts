import { z } from "zod";

const profileFormSchema = z.object({
  userName: z.string(),
  fullName: z.string().min(3),
  email: z.coerce.string().email().min(5),
  team: z.string(),
  role: z.string(),
  switchTeams: z.boolean(),
});

type ProfileFormSchema = z.infer<typeof profileFormSchema>;

export { profileFormSchema };
export type { ProfileFormSchema };
