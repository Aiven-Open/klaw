import { Flexbox } from "@aivenio/design-system";

const SideNavigation = () => {
  // The followin style is to illustrate sidenav height
  const style = { border: "1px solid red" };
  return (
    <Flexbox htmlTag="nav" style={style} height={"full"}>
      <ul>
        <li>
          <a href="#">navigation item 1</a>
        </li>
        <li>
          <a href="#">navigation item 2</a>
        </li>
        <li>
          <a href="#">navigation item 3</a>
        </li>
      </ul>
    </Flexbox>
  );
};

export default SideNavigation;
