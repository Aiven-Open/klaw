import { RenderResult } from "@testing-library/react";
import { within } from "@testing-library/react/pure";

/** Custom query to be able to check for a Definition List
 * that testing-library does not provide
 *
 * `<dl>`: https://www.w3.org/MarkUp/html3/deflists.html
 *
 * @param component RenderResult from testing-library's "render"
 * => `const component = render(<YourComponent)`
 */
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

/** Custom query to be able to check for a Term in a Definition List
 *
 * `<dt>`: https://www.w3.org/MarkUp/html3/termname.html
 *
 * While testing-library can query for the role "term", this does
 * not confirm that the term is semantically correctly used in
 * a `<dl>` and therefor does not confirm accessibility.
 *
 * @param list a HTMLDListElement, ideally the result from
 * `getDefinitionList`
 */
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

function getAllNextDefinitionSiblings(element: HTMLElement) {
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

/** Custom query to be able to get all definitions in a Definition List
 *
 * `<dd>`: https://www.w3.org/MarkUp/html3/termdef.html
 *
 * A `<dd>` element is only semantically correct and to identify correctly
 * for assistive technology if it follows a preceding term (`dt`) element.
 * This is why we provide a query to query for all next `dd` siblings of a
 * specific term in a Definition List
 *
 * @param list a HTMLDListElement, ideally the result from
 * `getDefinitionList`
 * @param term the term that the definition relates to
 */
function getAllDefinitions(list: HTMLDListElement, term: string) {
  const termComponent = getByTermInList(list, term);

  const allDDSiblings = getAllNextDefinitionSiblings(termComponent);

  if (allDDSiblings.length === 0) {
    throw Error(`No definitions (<dd>) found related to {term}.`);
  }

  return allDDSiblings;
}

export { getDefinitionList, getByTermInList, getAllDefinitions };
