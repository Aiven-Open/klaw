import { Switch } from "@aivenio/aquarium";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";

function MyRequestsFilter() {
  const { showOnlyMyRequests, setFilterValue } = useFiltersValues();

  const handleChangeIsMyRequest = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    if (event.target.checked) {
      setFilterValue({ name: "showOnlyMyRequests", value: "true" });
    } else {
      setFilterValue({ name: "showOnlyMyRequests", value: "false" });
    }
  };

  return (
    <Switch checked={showOnlyMyRequests} onChange={handleChangeIsMyRequest}>
      Show only my requests
    </Switch>
  );
}

export { MyRequestsFilter };
