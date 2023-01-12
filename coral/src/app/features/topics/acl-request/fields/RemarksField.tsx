import { Textarea } from "@aivenio/aquarium";

const RemarksField = () => (
  <Textarea
    name="remarks"
    labelText="Remarks"
    placeholder="Comments about this request."
  />
);

export default RemarksField;
