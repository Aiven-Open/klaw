import PreviewBanner from "src/app/components/PreviewBanner";
import KafkaEnvironments from "src/app/features/configuration/environments/Kafka/KafkaEnvironments";

const KafkaEnvironmentsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/envs"} />
      <KafkaEnvironments />
    </>
  );
};

export default KafkaEnvironmentsPage;
