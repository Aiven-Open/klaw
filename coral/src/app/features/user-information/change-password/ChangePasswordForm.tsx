import { Alert, Box, Typography, useToast } from "@aivenio/aquarium";
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
import { PasswordStrengthMeter } from "src/app/features/user-information/change-password/PasswordStrengthMeter";

function ChangePasswordForm() {
  const toast = useToast();

  const [passwordFocused, setPasswordFocused] = useState(false);
  const [showConfirmationModal, setShowConfirmationModal] = useState(false);

  const form = useForm<ChangePasswordFormSchema>({
    schema: changePasswordFormSchema,
  });

  const currentPassword = form.watch("password");

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
    setShowConfirmationModal(true);
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
      <Box maxWidth={"lg"}>
        {isError && (
          <Box marginBottom={"l1"}>
            <Alert type="error">{parseErrorMsg(error)}</Alert>
          </Box>
        )}

        <Box paddingBottom={"l2"}>
          <Typography>
            Entered password should be at least 8 characters long, and contain
            at least one: uppercase letter, lowercase letter, number and special
            character.
          </Typography>
        </Box>
        <Form
          {...form}
          ariaLabel={"Change your password by entering a new password"}
          onSubmit={handleSubmit}
        >
          <Box.Flex marginBottom={"l1"} gap={"2"} flexDirection={"column"}>
            <PasswordInput<ChangePasswordFormSchema>
              labelText="New password "
              name="password"
              required={true}
              reserveSpaceForError={false}
              onFocus={() => setPasswordFocused(true)}
              onBlur={() => setPasswordFocused(false)}
            />
            <PasswordStrengthMeter
              password={currentPassword}
              active={passwordFocused}
            />
          </Box.Flex>
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
