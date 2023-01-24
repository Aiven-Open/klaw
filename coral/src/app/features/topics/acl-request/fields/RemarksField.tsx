import { Textarea } from "src/app/components/Form";

const RemarksField = () => (
  <Textarea
    name="remarks"
    labelText="Remarks"
    placeholder="Comments about this request."
  />
);

export default RemarksField;
