import { z } from "zod";

const fullNameRegex = /^[A-Za-zÀ-ÖØ-öø-ÿ' ]*$/;

const profileFormSchema = z.object({
  userName: z.string(),
  fullName: z
    .string()
    .min(4, { message: "Full name must contain at least 4 characters." })
    .regex(fullNameRegex, {
      message:
        "Invalid input. Full name can only include uppercase and lowercase letters, accented characters (including" +
        " umlauts), apostrophes, and spaces.",
    }),
  email: z.coerce
    .string()
    .min(1, { message: "Email address can not be empty." })
    .email({ message: "Email address is invalid." }),
  team: z.string(),
  role: z.string(),
});

type ProfileFormSchema = z.infer<typeof profileFormSchema>;

export { profileFormSchema };
export type { ProfileFormSchema };
