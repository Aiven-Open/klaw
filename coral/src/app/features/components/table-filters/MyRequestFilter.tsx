import { Switch } from "@aivenio/aquarium";
import { useSearchParams } from "react-router-dom";

function MyRequestFilter() {
  const [searchParams, setSearchParams] = useSearchParams();
  const isMyRequest = searchParams.get("showOnlyMyRequests") === "true";

  const handleChangeIsMyRequest = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    if (event.target.checked) {
      searchParams.set("showOnlyMyRequests", "true");
    } else {
      searchParams.delete("showOnlyMyRequests");
    }
    searchParams.set("page", "1");
    setSearchParams(searchParams);
  };

  return (
    <Switch checked={isMyRequest} onChange={handleChangeIsMyRequest}>
      Show only my requests
    </Switch>
  );
}

export { MyRequestFilter };
