import { z } from "zod";

const changePasswordFormSchema = z
  .object({
    password: z
      .string()
      .min(8, { message: "Must be 8 or more characters long" }),
    confirmPassword: z
      .string()
      .min(8, { message: "Must be 8 or more characters long" }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

type ChangePasswordFormSchema = z.infer<typeof changePasswordFormSchema>;

export { changePasswordFormSchema, type ChangePasswordFormSchema };
