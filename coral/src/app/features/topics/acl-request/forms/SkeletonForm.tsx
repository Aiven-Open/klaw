import {
  Box,
  Divider,
  Grid,
  RadioButton
} from "@aivenio/aquarium";
import {
  MultiInput,
  NativeSelect,
  Textarea,
  TextInput,
} from "src/app/components/Form";

const SkeletonForm = () => {
  return (
    <Box data-testid={"skeleton"}>
      <Grid cols="2" minWidth={"fit"} colGap={"9"}>
        <Grid.Item>
          <Box.Flex gap={"4"}>
            <RadioButton.Skeleton data-testid="skeleton" />
            <RadioButton.Skeleton />
          </Box.Flex>
        </Grid.Item>
        <Grid.Item>
          <NativeSelect.Skeleton />
        </Grid.Item>

        <Grid.Item colSpan={"span-2"} paddingBottom={"l2"}>
          <Divider />
        </Grid.Item>

        <Grid.Item>
          <Box.Flex gap={"4"}>
            <RadioButton.Skeleton />
            <RadioButton.Skeleton />
          </Box.Flex>{" "}
        </Grid.Item>
        <Grid.Item>
          <TextInput.Skeleton />
        </Grid.Item>

        <Grid.Item colSpan={"span-2"}>
          <TextInput.Skeleton />
        </Grid.Item>

        <Grid.Item>
          <Box.Flex gap={"4"}>
            <RadioButton.Skeleton />
            <RadioButton.Skeleton />
          </Box.Flex>
        </Grid.Item>
        <Grid.Item>
          <MultiInput.Skeleton />
        </Grid.Item>
        <Grid.Item colSpan={"span-2"} minWidth={"full"}>
          <Textarea.Skeleton />
        </Grid.Item>
      </Grid>
    </Box>
  );
};

export default SkeletonForm;
