import { PageHeader } from "@aivenio/aquarium";
import PreviewBanner from "src/app/components/PreviewBanner";
import KafkaEnvironments from "src/app/features/configuration/environments/Kafka/KafkaEnvironments";

const KafkaEnvironmentsPage = () => {
  return (
    <>
      <PreviewBanner linkTarget={"/envs"} />
      <PageHeader title={"Kafka Environments"} />
      <KafkaEnvironments />
    </>
  );
};

export default KafkaEnvironmentsPage;
