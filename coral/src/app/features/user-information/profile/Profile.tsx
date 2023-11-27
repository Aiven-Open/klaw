import { useQuery } from "@tanstack/react-query";
import { getUser } from "src/domain/user/user-api";
import { Box, Grid, Typography } from "@aivenio/aquarium";
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

function Profile() {
  const { data: user } = useQuery({
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
    console.log(userInput);
  }

  //updateProfile
  return (
    <Form {...form} ariaLabel={"Request a new schema"} onSubmit={onSubmitForm}>
      <Grid>
        <Grid.Item md={6} xs={12}>
          <TextInput<ProfileFormSchema>
            labelText={"User name"}
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
            labelText={"Team (currently logged in)"}
            name={"team"}
            readOnly={true}
          />
          <TextInput<ProfileFormSchema>
            labelText={"Role"}
            name={"role"}
            readOnly={true}
          />

          {user?.switchAllowedTeamNames &&
            user?.switchAllowedTeamNames?.length >= 1 && (
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
                  User can switch teams
                </Checkbox>

                <div>
                  <Typography.SmallStrong>
                    Member of team
                  </Typography.SmallStrong>
                  <ul>
                    {user?.switchAllowedTeamNames.map((team) => {
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
