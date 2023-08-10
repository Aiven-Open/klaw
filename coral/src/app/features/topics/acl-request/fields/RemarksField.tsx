import { Textarea } from "src/app/components/Form";

const RemarksField = () => (
  <Textarea
    name="remarks"
    labelText="Message for approval"
    placeholder="Comments about this request for the approver."
  />
);

export default RemarksField;
