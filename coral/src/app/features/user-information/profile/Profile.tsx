import { useMutation, useQuery } from "@tanstack/react-query";
import { getUser, updateProfile } from "src/domain/user";
import { Alert, Box, Grid, useToast } from "@aivenio/aquarium";
import {
  Form,
  SubmitButton,
  TextInput,
  useForm,
} from "src/app/components/Form";
import {
  profileFormSchema,
  ProfileFormSchema,
} from "src/app/features/user-information/profile/form-schema/profile-form";
import { SkeletonProfile } from "src/app/features/user-information/profile/components/SkeletonProfile";
import { parseErrorMsg } from "src/services/mutation-utils";
import isEqual from "lodash/isEqual";
import { FieldErrors } from "react-hook-form";
import { TeamsOverview } from "src/app/features/user-information/profile/components/TeamsOverview";

function Profile() {
  const toast = useToast();

  const {
    data: user,
    isLoading: isLoadingUser,
    isError: isErrorUser,
    error: errorUser,
    refetch: refetchUser,
  } = useQuery({
    queryKey: ["getUser"],
    queryFn: getUser,
  });

  const {
    mutate: updateUser,
    isLoading: isLoadingUpdateUser,
    isError: isErrorUpdateUser,
    error: errorUpdateUser,
  } = useMutation(updateProfile, {
    onSuccess: async () => {
      toast({
        message: "Profile successfully updated",
        position: "bottom-left",
        variant: "default",
      });
      await refetchUser();
    },
  });

  const form = useForm<ProfileFormSchema>({
    values: {
      userName: user?.username || "",
      fullName: user?.fullname || "",
      email: user?.mailid || "",
      team: user?.team || "",
      role: user?.role || "",
    },
    schema: profileFormSchema,
  });

  function onSubmitForm(userInput: ProfileFormSchema) {
    if (
      isEqual(
        [userInput.fullName, userInput.email],
        [user?.fullname, user?.mailid]
      )
    ) {
      toast({
        message: "No changes were made to the profile.",
        position: "bottom-left",
        variant: "default",
      });
      return;
    }
    updateUser({ fullname: userInput.fullName, mailid: userInput.email });
  }

  function onFormError(error: FieldErrors<ProfileFormSchema>) {
    console.error(error);
  }

  if (isLoadingUser) {
    return <SkeletonProfile />;
  }

  if (isErrorUser) {
    return <Alert type={"error"}>{parseErrorMsg(errorUser)}</Alert>;
  }

  return (
    <>
      {isErrorUpdateUser && (
        <Box marginBottom={"l2"}>
          <Alert type={"error"}>{parseErrorMsg(errorUpdateUser)}</Alert>
        </Box>
      )}
      <Form
        {...form}
        ariaLabel={"Update profile"}
        onSubmit={onSubmitForm}
        onError={onFormError}
      >
        <Grid>
          <Grid.Item md={6} xs={12}>
            <TextInput<ProfileFormSchema>
              labelText={"User name (read-only)"}
              name={"userName"}
              readOnly={true}
            />
            <TextInput<ProfileFormSchema>
              labelText={"Full name"}
              name={"fullName"}
              description={
                "Can include uppercase and lowercase letters, accented characters (including" +
                " umlauts), apostrophes, and spaces. It has to be at least 4 characters."
              }
            />
            <TextInput<ProfileFormSchema>
              labelText={"Email address"}
              name={"email"}
            />
            <TextInput<ProfileFormSchema>
              labelText={"Team (read-only)"}
              name={"team"}
              readOnly={true}
            />
            <TextInput<ProfileFormSchema>
              labelText={"Role (read-only)"}
              name={"role"}
              readOnly={true}
            />
          </Grid.Item>
        </Grid>
        <SubmitButton loading={isLoadingUpdateUser}>
          Update profile
        </SubmitButton>
      </Form>

      {user.switchTeams &&
        user.switchAllowedTeamNames &&
        user.switchAllowedTeamNames?.length >= 1 && (
          <TeamsOverview teams={user.switchAllowedTeamNames} />
        )}
    </>
  );
}

export { Profile };
