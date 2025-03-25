import { z } from "zod";
import { buildPasswordSchema } from "src/app/features/user-information/change-password/password-requirement";

const changePasswordFormSchema = z
  .object({
    password: buildPasswordSchema(),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

type ChangePasswordFormSchema = z.infer<typeof changePasswordFormSchema>;

export { changePasswordFormSchema };
export type { ChangePasswordFormSchema };
