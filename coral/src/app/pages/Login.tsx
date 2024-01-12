import { Button } from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { login } from "src/domain/user/user-api";

const Login = () => {
  const navigate = useNavigate();
  const { mutate, isLoading } = useMutation(login, {
    onSuccess: () => navigate("/"),
  });
  return (
    <Button.Primary
      loading={isLoading}
      onClick={() =>
        mutate({
          username: "josepprat",
          password: "chase123",
        })
      }
    >
      Log in
    </Button.Primary>
  );
};

export default Login;
