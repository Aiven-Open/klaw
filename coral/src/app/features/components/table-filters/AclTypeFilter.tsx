import { NativeSelect } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";
import { AclType } from "src/domain/acl";

type AclTypeForFilter = AclType | "ALL";
const aclTypesForFilter: AclTypeForFilter[] = ["ALL", "CONSUMER", "PRODUCER"];

function AclTypeFilter() {
  const [searchParams, setSearchParams] = useSearchParams();
  const aclType = searchParams.get("aclType") ?? ("ALL" as AclType);

  const handleChangeAclType = (nextAclType: AclTypeForFilter) => {
    searchParams.set("aclType", nextAclType);
    searchParams.set("page", "1");

    setSearchParams(searchParams);
  };

  return (
    <NativeSelect
      labelText={"Filter by ACL type"}
      key={"filter-acl-type"}
      defaultValue={aclType}
      onChange={(e) => {
        const selectedType = e.target.value as AclTypeForFilter;
        return handleChangeAclType(selectedType);
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
