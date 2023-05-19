import { Switch } from "@aivenio/aquarium";
import { useFiltersValues } from "src/app/features/components/filters/useFiltersValues";

interface MyRequestsFilterProps {
  paginated?: boolean;
}

function MyRequestsFilter({ paginated }: MyRequestsFilterProps) {
  const { showOnlyMyRequests, setFilterValue } = useFiltersValues();

  const handleChangeIsMyRequest = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFilterValue({
      name: "showOnlyMyRequests",
      value: event.target.checked,
      paginated,
    });
  };

  return (
    <Switch checked={showOnlyMyRequests} onChange={handleChangeIsMyRequest}>
      Show only my requests
    </Switch>
  );
}

export { MyRequestsFilter };
