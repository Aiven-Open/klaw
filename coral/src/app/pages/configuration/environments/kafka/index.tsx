import PreviewBanner from "src/app/components/PreviewBanner";
import KafkaEnvironments from "src/app/features/environments/KafkaEnvironments";

const KafkaEnvironmentsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/envs"} />
      <KafkaEnvironments />
    </>
  );
};

export default KafkaEnvironmentsPage;
