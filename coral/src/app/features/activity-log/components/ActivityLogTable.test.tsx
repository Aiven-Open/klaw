import { cleanup, render, screen, within } from "@testing-library/react";
import {
  ActivityLogTable,
  ActivityLogTableProps,
} from "src/app/features/activity-log/components/ActivityLogTable";
import { ActivityLog } from "src/domain/requests/requests-types";
import { mockIntersectionObserver } from "src/services/test-utils/mock-intersection-observer";

const mockedActivityLogs: ActivityLog[] = [
  {
    req_no: 1001,
    tenantId: 101,
    activityName: "TopicRequest",
    activityType: "Create",
    activityTime: "2023-06-17T13:52:14.646+00:00",
    activityTimeString: "17-Jun-2023 13:52:14",
    details: "testtopic12345",
    user: "muralibasani",
    teamId: 1005,
    env: "1",
    envName: "DEV",
    team: "Ospo",
    totalNoPages: "1",
    currentPage: "1",
    allPageNos: ["1", ">", ">>"],
  },
];

describe("ActivityLogsTable", () => {
  function renderFromProps(props?: Partial<ActivityLogTableProps>): void {
    render(
      <ActivityLogTable
        activityLogs={mockedActivityLogs}
        ariaLabel={"Activity log"}
        {...props}
      />
    );
  }

  function getNthRow(nth: number): HTMLElement {
    return screen.getAllByRole("row")[nth];
  }

  beforeEach(() => {
    mockIntersectionObserver();
  });

  afterEach(cleanup);

  it("shows a message to user in case there are no activityLogs that match the search criteria", () => {
    renderFromProps({ activityLogs: [] });
    screen.getByText("No activity log");
    screen.getByText("No activity log matched your criteria.");
  });

  it("has column to describe the activity name ", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[0]
    ).toHaveTextContent("Activity");
    expect(within(getNthRow(1)).getAllByRole("cell")[0]).toHaveTextContent(
      "TopicRequest"
    );
  });

  it("has column to describe the environment", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[1]
    ).toHaveTextContent("Environment");
    expect(within(getNthRow(1)).getAllByRole("cell")[1]).toHaveTextContent(
      "DEV"
    );
  });

  it("has column to describe the user", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[2]
    ).toHaveTextContent("User");
    expect(within(getNthRow(1)).getAllByRole("cell")[2]).toHaveTextContent(
      "muralibasani"
    );
  });

  it("has column to describe the team", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[3]
    ).toHaveTextContent("Team");
    expect(within(getNthRow(1)).getAllByRole("cell")[3]).toHaveTextContent(
      "Ospo"
    );
  });

  it("has column to describe the details", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[4]
    ).toHaveTextContent("Details");
    expect(within(getNthRow(1)).getAllByRole("cell")[4]).toHaveTextContent(
      "testtopic12345"
    );
  });

  it("has column to describe the timestamp when the request was made", () => {
    renderFromProps();
    expect(
      within(getNthRow(0)).getAllByRole("columnheader")[5]
    ).toHaveTextContent("Date");
    expect(within(getNthRow(1)).getAllByRole("cell")[5]).toHaveTextContent(
      "17-Jun-2023 13:52:14 UTC"
    );
  });
});
