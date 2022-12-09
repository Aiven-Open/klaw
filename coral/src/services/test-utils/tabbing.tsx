import userEvent from "@testing-library/user-event";

async function tabThroughForward(times: number) {
  for (let i = times; i > 0; i--) {
    await userEvent.tab();
  }
}

async function tabThroughBackward(times: number) {
  for (let i = times; i > 0; i--) {
    await userEvent.tab({ shift: true });
  }
}

async function tabNavigateTo({
  targetElement,
}: {
  targetElement: HTMLElement;
}) {
  while (document.activeElement !== targetElement) {
    await userEvent.tab();
  }
}

export { tabThroughForward, tabThroughBackward, tabNavigateTo };
