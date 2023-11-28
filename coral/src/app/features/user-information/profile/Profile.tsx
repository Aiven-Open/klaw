import { useQuery } from "@tanstack/react-query";
import { getUser } from "src/domain/user";
import { Alert, Box, Grid, Typography, useToast } from "@aivenio/aquarium";
import {
  Form,
  SubmitButton,
  TextInput,
  Checkbox,
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

function Profile() {
  const toast = useToast();

  const {
    data: user,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["getUser"],
    queryFn: getUser,
  });

  const form = useForm<ProfileFormSchema>({
    values: {
      userName: user?.username || "",
      fullName: user?.fullname || "",
      email: user?.mailid || "",
      team: user?.team || "",
      role: user?.role || "",
      switchTeams: user?.switchTeams || false,
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
        message: "No changes were made to the topic.",
        position: "bottom-left",
        variant: "default",
      });
      return;
    }
    console.log(userInput);
  }

  function onFormError(error: FieldErrors<ProfileFormSchema>) {
    console.error(error);
  }

  if (isLoading) {
    return <SkeletonProfile />;
  }

  if (!isLoading && isError) {
    return <Alert type={"error"}>{parseErrorMsg(error)}</Alert>;
  }

  return (
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

          {user.switchTeams &&
            user.switchAllowedTeamNames &&
            user.switchAllowedTeamNames?.length >= 1 && (
              <Box.Flex
                flexDirection={"column"}
                rowGap={"l2"}
                paddingBottom={"l2"}
              >
                <Checkbox<ProfileFormSchema>
                  name={"switchTeams"}
                  checked={user.switchTeams}
                  disabled={true}
                >
                  User can switch teams (read-only)
                </Checkbox>

                <div>
                  <Typography.SmallStrong>
                    <span id={"team-list-id"}>Member of team (read-only)</span>
                  </Typography.SmallStrong>
                  <ul aria-labelledby={"team-list-id"}>
                    {user.switchAllowedTeamNames.map((team) => {
                      return <li key={team}>{team}</li>;
                    })}
                  </ul>
                </div>
              </Box.Flex>
            )}
        </Grid.Item>
      </Grid>
      <SubmitButton>Update profile</SubmitButton>
    </Form>
  );
}

export { Profile };
