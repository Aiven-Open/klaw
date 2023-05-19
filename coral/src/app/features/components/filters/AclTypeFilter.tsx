import { NativeSelect } from "@aivenio/aquarium";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";
import { AclType } from "src/domain/acl";

type AclTypeForFilter = AclType | "ALL";
const aclTypesForFilter: AclTypeForFilter[] = ["ALL", "CONSUMER", "PRODUCER"];

interface AclTypeFilterProps {
  paginated?: boolean;
}

function AclTypeFilter({ paginated }: AclTypeFilterProps) {
  const { aclType, setFilterValue } = useFiltersValues();

  return (
    <NativeSelect
      labelText={"Filter by ACL type"}
      key={"filter-acl-type"}
      defaultValue={aclType}
      onChange={(e) => {
        const selectedType = e.target.value as AclTypeForFilter;
        return setFilterValue({
          name: "aclType",
          value: selectedType,
          paginated,
        });
      }}
    >
      {aclTypesForFilter.map((type) => {
        if (type === "ALL") {
          return (
            <option key={type} value="ALL">
              All ACL types
            </option>
          );
        }
        return <option key={type}>{type}</option>;
      })}
    </NativeSelect>
  );
}

export default AclTypeFilter;
