import z from "zod";
import {
  Form,
  PasswordInput,
  SubmitButton,
  TextInput,
  useForm,
} from "src/app/components/Form";
import { FieldErrors } from "react-hook-form";
import { Box } from "@aivenio/aquarium";
import { useLoginUser } from "src/app/features/login/hooks/useLoginUser";
import { useEffect } from "react";

const formSchema = z.object({
  username: z.string().min(1, { message: "Username is required" }),
  password: z.string().min(1, { message: "Password is required" }),
});

type Schema = z.infer<typeof formSchema>;

const LoginForm = () => {
  const loginUser = useLoginUser();
  const form = useForm<Schema>({
    schema: formSchema,
  });

  useEffect(() => {
    if (loginUser.isSuccess) {
      form.reset();
    }
  }, [loginUser.isSuccess]);

  function onSubmitForm(userInput: Schema) {
    loginUser.mutate({
      username: userInput.username,
      password: userInput.password,
    });
  }

  function onErrorForm(arg: FieldErrors) {
    return arg;
  }

  return (
    <Box
      display={"flex"}
      flexDirection={"column"}
      alignItems={"center"}
      width={"3/5"}
    >
      <Box width={"3/5"}>
        <Form {...form} onSubmit={onSubmitForm} onError={onErrorForm}>
          <TextInput<Schema>
            name={"username"}
            labelText="Username"
            placeholder="Your username"
            required
          />
          <PasswordInput<Schema>
            name={"password"}
            labelText="Password"
            placeholder="Your password"
            required
          />
          <SubmitButton>Submit</SubmitButton>
        </Form>
        {loginUser.isSuccess && <div>Login successful 🎉 </div>}
        {loginUser.isError && <div>Something went wrong 😞</div>}
      </Box>
    </Box>
  );
};

export { LoginForm };
