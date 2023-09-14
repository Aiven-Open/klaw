import PreviewBanner from "src/app/components/PreviewBanner";
import KafkaConnectEnvironments from "src/app/features/configuration/environments/KafkaConnect/KafkaConnectEnvironments";

const KafkaConnectEnvironmentsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/envs"} />
      <KafkaConnectEnvironments />
    </>
  );
};

export default KafkaConnectEnvironmentsPage;
