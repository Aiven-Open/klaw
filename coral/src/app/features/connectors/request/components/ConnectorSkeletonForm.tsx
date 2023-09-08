import { Grid, GridItem } from "@aivenio/aquarium";
import { NativeSelect, Textarea, TextInput } from "src/app/components/Form";

const ConnectorSkeletonForm = () => {
  return (
    <div data-testid={"connector-form-skeleton-skeleton"}>
      <span className={"visually-hidden"}>Form is loading.</span>
      <div aria-hidden={true}>
        <Grid cols={"2"} minWidth={"fit"} colGap={"9"}>
          <GridItem colSpan={"span-2"}>
            <NativeSelect.Skeleton />
          </GridItem>
          <GridItem colSpan={"span-2"}>
            <TextInput.Skeleton />
          </GridItem>

          <GridItem colSpan={"span-2"} rowSpan={"span-2"}>
            <Textarea.Skeleton />
          </GridItem>

          <GridItem>
            <Textarea.Skeleton />
          </GridItem>
          <GridItem>
            <Textarea.Skeleton />
          </GridItem>
        </Grid>
      </div>
    </div>
  );
};

export { ConnectorSkeletonForm };
