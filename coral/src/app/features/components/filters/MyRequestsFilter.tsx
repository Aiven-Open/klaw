import { Switch } from "@aivenio/aquarium";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";

function MyRequestsFilter() {
  const { showOnlyMyRequests, setFilterValue } = useFiltersValues();

  const handleChangeIsMyRequest = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFilterValue({ name: "showOnlyMyRequests", value: event.target.checked });
  };

  return (
    <Switch checked={showOnlyMyRequests} onChange={handleChangeIsMyRequest}>
      Show only my requests
    </Switch>
  );
}

export { MyRequestsFilter };
