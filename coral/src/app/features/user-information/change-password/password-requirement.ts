import { z } from "zod";

const passwordRequirements = [
  {
    regex: /.{8,}/,
    errorMessage: "Must be 8 or more characters long",
    label: "8+ characters",
  },
  {
    regex: /[A-Z]/,
    errorMessage: "Must include at least one uppercase letter",
    label: "Uppercase letter",
  },
  {
    regex: /[a-z]/,
    errorMessage: "Must include at least one lowercase letter",
    label: "Lowercase letter",
  },
  {
    regex: /\d/,
    errorMessage: "Must include at least one number",
    label: "Number",
  },
  {
    regex: /[\W_]/,
    errorMessage: "Must include at least one special character",
    label: "Special character",
  },
];

const buildPasswordSchema = () => {
  return passwordRequirements.reduce(
    (schema, req) => schema.regex(req.regex, { message: req.errorMessage }),
    z.string()
  );
};

const getFulfilledPasswordRules = (password: string) => {
  return passwordRequirements.map((req) => ({
    label: req.label,
    fulfilled: req.regex.test(password),
  }));
};

export { passwordRequirements, buildPasswordSchema, getFulfilledPasswordRules };
