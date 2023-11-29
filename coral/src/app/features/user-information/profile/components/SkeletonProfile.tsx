import { Grid } from "@aivenio/aquarium";
import { TextInput } from "src/app/components/Form";

function SkeletonProfile() {
  return (
    <>
      <div className={"visually-hidden"}>User profile loading.</div>
      <div aria-hidden={true}>
        <Grid>
          <Grid.Item md={6} xs={12} aria-hidden={true}>
            <TextInput.Skeleton />

            <TextInput.Skeleton />
            <TextInput.Skeleton />
            <TextInput.Skeleton />
            <TextInput.Skeleton />
          </Grid.Item>
        </Grid>
      </div>
    </>
  );
}

export { SkeletonProfile };
