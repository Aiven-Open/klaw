import z from "zod";
import {
  Form,
  PasswordInput,
  SubmitButton,
  TextInput,
  useForm,
} from "src/app/components/Form";
import { FieldErrors } from "react-hook-form";
import { useState } from "react";
import { Flexbox, FlexboxItem } from "@aivenio/design-system";

const formSchema = z.object({
  username: z.string().min(1, { message: "Username is required" }),
  password: z.string().min(1, { message: "Password is required" }),
});

type Schema = z.infer<typeof formSchema>;

const LoginPage = () => {
  const [success, setSuccess] = useState<boolean>(false);
  const form = useForm<Schema>({
    schema: formSchema,
  });

  function onSubmitForm(userInput: Schema) {
    setSuccess(true);
    return userInput;
  }

  function onErrorForm(arg: FieldErrors) {
    return arg;
  }

  return (
    <>
      <h1>Login page</h1>
      <Flexbox direction={"column"} alignItems={"center"}>
        <FlexboxItem width={"3/5"}>
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
          {success && <div>Login successful!</div>}
        </FlexboxItem>
      </Flexbox>
    </>
  );
};

export default LoginPage;
