import { PageHeader } from "@aivenio/aquarium";
import KafkaEnvironments from "src/app/features/configuration/environments/Kafka/KafkaEnvironments";

const KafkaEnvironmentsPage = () => {
  return (
    <>
      <PageHeader title={"Kafka Environments"} />
      <KafkaEnvironments />
    </>
  );
};

export default KafkaEnvironmentsPage;
