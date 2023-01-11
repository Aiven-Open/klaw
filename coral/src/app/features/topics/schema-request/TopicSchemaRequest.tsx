type TopicSchemaRequestProps = {
  topicName: string;
};

function TopicSchemaRequest(props: TopicSchemaRequestProps) {
  const { topicName } = props;
  return (
    <>
      <h2>Hello this is topic schema request for {topicName}</h2>
    </>
  );
}

export { TopicSchemaRequest };
