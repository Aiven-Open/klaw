import {
  Box,
  Divider,
  Flexbox,
  Grid,
  GridItem,
  RadioButton,
} from "@aivenio/aquarium";
import {
  MultiInput,
  NativeSelect,
  Textarea,
  TextInput,
} from "src/app/components/Form";

const SkeletonForm = () => {
  return (
    <Box data-testid={"skeleton"} maxWidth={"4xl"}>
      <Grid cols="2" minWidth={"fit"} colGap={"9"}>
        <GridItem>
          <Flexbox gap={"4"}>
            <RadioButton.Skeleton data-testid="skeleton" />
            <RadioButton.Skeleton />
          </Flexbox>
        </GridItem>
        <GridItem>
          <NativeSelect.Skeleton />
        </GridItem>

        <GridItem colSpan={"span-2"} paddingBottom={"l2"}>
          <Divider />
        </GridItem>

        <GridItem>
          <Flexbox gap={"4"}>
            <RadioButton.Skeleton />
            <RadioButton.Skeleton />
          </Flexbox>{" "}
        </GridItem>
        <GridItem>
          <TextInput.Skeleton />
        </GridItem>

        <GridItem colSpan={"span-2"}>
          <TextInput.Skeleton />
        </GridItem>

        <GridItem>
          <Flexbox gap={"4"}>
            <RadioButton.Skeleton />
            <RadioButton.Skeleton />
          </Flexbox>
        </GridItem>
        <GridItem>
          <MultiInput.Skeleton />
        </GridItem>
        <GridItem colSpan={"span-2"} minWidth={"full"}>
          <Textarea.Skeleton />
        </GridItem>
      </Grid>
    </Box>
  );
};

export default SkeletonForm;
