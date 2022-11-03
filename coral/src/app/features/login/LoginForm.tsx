import z from "zod";
import {
  Form,
  PasswordInput,
  SubmitButton,
  TextInput,
  useForm,
} from "src/app/components/Form";
import { FieldErrors } from "react-hook-form";
import { Flexbox, FlexboxItem } from "@aivenio/design-system";
import useLoginUser from "src/app/features/login/useLoginUser";

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

  function onSubmitForm(userInput: Schema) {
    loginUser.mutate({
      username: userInput.username,
      password: userInput.password,
    });

    if (loginUser.isSuccess) {
      form.reset();
    }
  }

  function onErrorForm(arg: FieldErrors) {
    return arg;
  }

  return (
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
        {loginUser.isSuccess && <div>Login successful 🎉</div>}
        {loginUser.isError && <div>Username or password are wrong 😞</div>}
      </FlexboxItem>
    </Flexbox>
  );
};

export { LoginForm };
