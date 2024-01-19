import { NativeSelect, Option } from "@aivenio/aquarium";
import { useFiltersContext } from "src/app/features/components/filters/useFiltersContext";
import { TopicType } from "src/domain/topic";
import upperFirst from "lodash/upperFirst";

type TopicTypeForFilter = TopicType | "ALL";
const topicTypesForFilter: TopicTypeForFilter[] = [
  "ALL",
  "Consumer",
  "Producer",
];
function TopicTypeFilter() {
  const { topicType, setFilterValue } = useFiltersContext();

  return (
    <NativeSelect
      labelText={"Filter by Topic type"}
      defaultValue={topicType}
      onChange={(e) => {
        const selectedType = e.target.value as TopicTypeForFilter;
        return setFilterValue({ name: "topicType", value: selectedType });
      }}
    >
      {topicTypesForFilter.map((type) => {
        if (type === "ALL") {
          return (
            <Option key={type} value="ALL">
              All Topics
            </Option>
          );
        }
        return (
          <Option key={type} value={type}>
            {upperFirst(type.toLowerCase())}
          </Option>
        );
      })}
    </NativeSelect>
  );
}

export { TopicTypeFilter };
