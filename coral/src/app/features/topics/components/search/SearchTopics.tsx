import {
  Flexbox,
  Icon,
  InputBase,
  PrimaryButton,
} from "@aivenio/design-system";
import { FormEvent, useState } from "react";
import searchItem from "@aivenio/design-system/dist/module/icons/search";

type SearchTopicsProps = {
  searchTerm: string;
  search: (searchTerm: string) => void;
};

function SearchTopics(props: SearchTopicsProps) {
  const { search, searchTerm } = props;
  const [currentSearchTerm, setCurrentSearchTerm] =
    useState<string>(searchTerm);

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    search(currentSearchTerm);
  }

  return (
    <form role={"search"} onSubmit={onSubmit} aria-label={"Topics"}>
      <label className={"visually-hidden"} htmlFor={"topics-search"}>
        Search topics
      </label>
      <Flexbox>
        <InputBase
          placeholder="Topic name"
          value={currentSearchTerm}
          onChange={(event) => setCurrentSearchTerm(event.target.value)}
          id={"topics-search"}
        />
        <PrimaryButton type={"submit"} dense>
          <span className={"visually-hidden"}>Submit search</span>
          <Icon
            aria-hidden={true}
            icon={searchItem}
            data-testid={"visually-hidden-search-icon"}
          />
        </PrimaryButton>
      </Flexbox>
    </form>
  );
}

export { SearchTopics };
