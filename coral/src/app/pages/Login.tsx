import z from "zod";
import {
  Form,
  SubmitButton,
  TextInput,
  useForm,
} from "src/app/components/Form";
import { FieldErrors } from "react-hook-form";

const formSchema = z.object({
  username: z.string(),
  password: z.string(),
});

type Schema = z.infer<typeof formSchema>;

const Login = () => {
  const form = useForm<Schema>({
    schema: formSchema,
  });

  function onSubmitForm() {
    console.log("submit");
  }

  function onErrorForm(arg: FieldErrors) {
    console.error(arg);
  }

  return (
    <>
      <h1>Login page</h1>

      <Form {...form} onSubmit={onSubmitForm} onError={onErrorForm}>
        <TextInput<Schema> name={"username"} />
        <TextInput<Schema> name={"password"} />
        <SubmitButton>Submit</SubmitButton>
      </Form>
    </>
  );
};

export default Login;
