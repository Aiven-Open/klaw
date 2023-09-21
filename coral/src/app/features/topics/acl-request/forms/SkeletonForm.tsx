import { Box, Divider, Grid, GridItem, RadioButton } from "@aivenio/aquarium";
import {
  MultiInput,
  NativeSelect,
  Textarea,
  TextInput,
} from "src/app/components/Form";

const SkeletonForm = () => {
  return (
    <Box.Flex data-testid={"skeleton"}>
      <Grid cols="2" minWidth={"fit"} colGap={"9"}>
        <GridItem>
          <Box.Flex gap={"4"}>
            <RadioButton.Skeleton data-testid="skeleton" />
            <RadioButton.Skeleton />
          </Box.Flex>
        </GridItem>
        <GridItem>
          <NativeSelect.Skeleton />
        </GridItem>

        <GridItem colSpan={"span-2"} paddingBottom={"l2"}>
          <Divider />
        </GridItem>

        <GridItem>
          <Box.Flex gap={"4"}>
            <RadioButton.Skeleton />
            <RadioButton.Skeleton />
          </Box.Flex>{" "}
        </GridItem>
        <GridItem>
          <TextInput.Skeleton />
        </GridItem>

        <GridItem colSpan={"span-2"}>
          <TextInput.Skeleton />
        </GridItem>

        <GridItem>
          <Box.Flex gap={"4"}>
            <RadioButton.Skeleton />
            <RadioButton.Skeleton />
          </Box.Flex>
        </GridItem>
        <GridItem>
          <MultiInput.Skeleton />
        </GridItem>
        <GridItem colSpan={"span-2"} minWidth={"full"}>
          <Textarea.Skeleton />
        </GridItem>
      </Grid>
    </Box.Flex>
  );
};

export default SkeletonForm;
