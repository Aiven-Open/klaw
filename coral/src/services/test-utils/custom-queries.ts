import { RenderResult } from "@testing-library/react";
import { within } from "@testing-library/react/pure";

function getDefinitionList(component: RenderResult) {
  const list = component.container.querySelectorAll("dl");
  if (list.length === 0) {
    throw Error("No definition list found");
  }

  if (list.length > 1) {
    throw Error("Multiple definition lists found");
  }

  return list[0];
}

function getByTermInList(list: HTMLDListElement, term: string) {
  const allTerms = within(list).getAllByRole("term");
  const termComponent = allTerms.filter((termElement) => {
    return termElement.textContent === term;
  });

  if (termComponent === undefined) {
    throw Error(`Could not find a term (<dt>) "${term}"`);
  }

  if (termComponent.length > 1) {
    throw Error(
      `There are multiple term (<dt>) elements with name "${term}". A term should be unique in a definition list!`
    );
  }

  return termComponent[0];
}

function getAllNextSiblings(element: HTMLElement) {
  const siblings = [];
  let nextElementSibling = element.nextElementSibling;

  while (nextElementSibling) {
    if (nextElementSibling.tagName === "DD") {
      siblings.push(nextElementSibling);
    }
    nextElementSibling = nextElementSibling.nextElementSibling;
  }
  return siblings;
}
function getAllDefinitions(list: HTMLDListElement, term: string) {
  const termComponent = getByTermInList(list, term);

  const allDDSiblings = getAllNextSiblings(termComponent);

  if (allDDSiblings.length === 0) {
    throw Error(`No definitions (<dd>) found related to {term}.`);
  }

  return allDDSiblings;
}

export { getDefinitionList, getByTermInList, getAllDefinitions };
