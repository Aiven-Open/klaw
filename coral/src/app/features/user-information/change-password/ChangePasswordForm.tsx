import { Alert, Box, useToast } from "@aivenio/aquarium";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Dialog } from "src/app/components/Dialog";
import {
  Form,
  PasswordInput,
  SubmitButton,
  useForm,
} from "src/app/components/Form";
import {
  ChangePasswordFormSchema,
  changePasswordFormSchema,
} from "src/app/features/user-information/change-password/changePasswordFormSchema";
import { changePassword } from "src/domain/user/user-api";
import { parseErrorMsg } from "src/services/mutation-utils";

function ChangePasswordForm() {
  const toast = useToast();

  const [showConfirmationModal, setShowConfirmationModal] = useState(false);

  const form = useForm<ChangePasswordFormSchema>({
    schema: changePasswordFormSchema,
  });
  // This destructuring is necessary for the test to correctly register isDirty
  // If we access it inline like form.formState.isDirty, it works when using it
  // But in the test isDirty will always be false at the moment of being checked
  const { isDirty } = form.formState;

  const { mutate, isLoading, isError, error, isSuccess } =
    useMutation(changePassword);

  useEffect(() => {
    if (isSuccess) {
      toast({
        message: "Password successfully changed",
        position: "bottom-left",
        variant: "default",
      });
      setShowConfirmationModal(false);
      form.reset();
      return;
    }
    if (isError) {
      setShowConfirmationModal(false);
      return;
    }
  }, [isSuccess, isError]);

  const handleSubmit = () => {
    // onSubmit will not trigger if form has errors, but will if the form is pristine
    // We only want to show the confirm modal if form is filled and with no field error
    isDirty && setShowConfirmationModal(true);
  };

  return (
    <>
      {showConfirmationModal && (
        <Dialog
          title={"Confirm password change?"}
          primaryAction={{
            text: "Change password",
            onClick: () =>
              mutate({
                pwd: form.getValues().password,
                repeatPwd: form.getValues().confirmPassword,
              }),
            loading: isLoading,
          }}
          secondaryAction={{
            text: "Cancel password change",
            onClick: () => setShowConfirmationModal(false),
          }}
          type={"warning"}
        >
          Are you sure you want to change your password? The change will take
          effect immediately.
        </Dialog>
      )}
      <Box maxWidth={"md"}>
        {isError && (
          <Box marginBottom={"l1"}>
            <Alert type="error">{parseErrorMsg(error)}</Alert>
          </Box>
        )}
        <Form
          {...form}
          ariaLabel={"Change your password by entering a new password"}
          onSubmit={handleSubmit}
        >
          <PasswordInput<ChangePasswordFormSchema>
            labelText="New password "
            name="password"
            description="Entered password should be at least 8 characters long"
          />
          <PasswordInput<ChangePasswordFormSchema>
            labelText="Confirm new password"
            name="confirmPassword"
          />
          <SubmitButton loading={isLoading}>Update password</SubmitButton>
        </Form>
      </Box>
    </>
  );
}

export { ChangePasswordForm };
