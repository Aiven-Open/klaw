import { z } from "zod";

const changePasswordFormSchema = z
  .object({
    password: z
      .string()
      .min(8, { message: "Must be 8 or more characters long" })
      .regex(/[A-Z]/, { message: "Must include at least one uppercase letter" })
      .regex(/[a-z]/, { message: "Must include at least one lowercase letter" })
      .regex(/\d/, { message: "Must include at least one number" })
      .regex(/[\W_]/, {
        message: "Must include at least one special character",
      }),
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
