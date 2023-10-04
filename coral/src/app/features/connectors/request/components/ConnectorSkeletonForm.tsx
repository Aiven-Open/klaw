import { Grid } from "@aivenio/aquarium";
import { NativeSelect, Textarea, TextInput } from "src/app/components/Form";

const ConnectorSkeletonForm = () => {
  return (
    <div data-testid={"connector-form-skeleton-skeleton"}>
      <span className={"visually-hidden"}>Form is loading.</span>
      <div aria-hidden={true}>
        <Grid cols={"2"} minWidth={"fit"} colGap={"9"}>
          <Grid.Item colSpan={"span-2"}>
            <NativeSelect.Skeleton />
          </Grid.Item>
          <Grid.Item colSpan={"span-2"}>
            <TextInput.Skeleton />
          </Grid.Item>

          <Grid.Item colSpan={"span-2"} rowSpan={"span-2"}>
            <Textarea.Skeleton />
          </Grid.Item>

          <Grid.Item>
            <Textarea.Skeleton />
          </Grid.Item>
          <Grid.Item>
            <Textarea.Skeleton />
          </Grid.Item>
        </Grid>
      </div>
    </div>
  );
};

export { ConnectorSkeletonForm };
