import { Flexbox, TagLabel, Typography } from "@aivenio/design-system";

function App() {
  return (
    <div>
      <Typography htmlTag={"h1"} variant={"heading-2xl"}>
        Hello Klaw 👋
      </Typography>
      <Flexbox alignItems={"center"}>
        <p>This uses the aiven design system! &nbsp; </p>
        <TagLabel title="yey" />
      </Flexbox>
    </div>
  );
}

export default App;
