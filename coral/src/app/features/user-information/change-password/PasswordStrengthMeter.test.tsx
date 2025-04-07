import { act, cleanup, render, screen } from "@testing-library/react";
import { PasswordStrengthMeter } from "src/app/features/user-information/change-password/PasswordStrengthMeter";

describe("<PasswordStrengthMeter />", () => {
  beforeEach(() => {
    // making sure there's no leftover DOM
    // from previous render with timeout
    jest.clearAllTimers();
    cleanup();

    jest.useFakeTimers();
  });

  it("does not render the alert to announce when prop active is false", async () => {
    render(<PasswordStrengthMeter active={false} password="xx" />);
    act(() => {
      jest.runAllTimers();
    });

    const announcement = screen.queryByRole("alert");

    expect(announcement).not.toBeInTheDocument();
  });

  it("announces nothing while no password is set met", async () => {
    render(<PasswordStrengthMeter active={true} password="" />);
    act(() => {
      jest.runAllTimers();
    });

    const announcement = screen.queryByRole("alert");

    expect(announcement).not.toBeInTheDocument();
  });

  it("announces lowercase met", async () => {
    render(<PasswordStrengthMeter active={true} password="xx" />);
    act(() => {
      jest.runAllTimers();
    });

    const announcement = await screen.findByRole("alert");

    expect(announcement).toHaveTextContent(/Lowercase letter: Met/i);
    expect(announcement).toHaveTextContent(/8\+ characters: Not met/i);
    expect(announcement).toHaveTextContent(/Uppercase letter: Not met/i);
    expect(announcement).toHaveTextContent(/Number: Not met/i);
    expect(announcement).toHaveTextContent(/Special character: Not met/i);
  });

  it("announces lowercase and number met", async () => {
    render(<PasswordStrengthMeter active={true} password="pass1" />);
    act(() => {
      jest.runAllTimers();
    });

    const announcement = await screen.findByRole("alert");

    expect(announcement).toHaveTextContent(/Lowercase letter: Met/i);
    expect(announcement).toHaveTextContent(/Number: Met/i);
    expect(announcement).toHaveTextContent(/8\+ characters: Not met/i);
    expect(announcement).toHaveTextContent(/Uppercase letter: Not met/i);
    expect(announcement).toHaveTextContent(/Special character: Not met/i);
  });

  it("announces lowercase, uppercase, number", async () => {
    render(<PasswordStrengthMeter active={true} password="Pass1" />);
    act(() => {
      jest.runAllTimers();
    });

    const announcement = await screen.findByRole("alert");

    expect(announcement).toHaveTextContent(/Uppercase letter: Met/i);
    expect(announcement).toHaveTextContent(/Lowercase letter: Met/i);
    expect(announcement).toHaveTextContent(/Number: Met/i);
    expect(announcement).toHaveTextContent(/8\+ characters: Not met/i);
    expect(announcement).toHaveTextContent(/Special character: Not met/i);
  });

  it("announces lowercase, uppercase, number, special char'", async () => {
    render(<PasswordStrengthMeter active={true} password="Pass1!" />);
    act(() => {
      jest.runAllTimers();
    });

    const announcement = await screen.findByRole("alert");

    expect(announcement).toHaveTextContent(/Uppercase letter: Met/i);
    expect(announcement).toHaveTextContent(/Lowercase letter: Met/i);
    expect(announcement).toHaveTextContent(/Number: Met/i);
    expect(announcement).toHaveTextContent(/Special character: Met/i);
    expect(announcement).toHaveTextContent(/8\+ characters: Not met/i);
  });

  it("announces all requirements met", async () => {
    render(<PasswordStrengthMeter active={true} password="Pass1!more" />);
    act(() => {
      jest.runAllTimers();
    });

    const announcement = await screen.findByRole("alert");

    expect(announcement).toHaveTextContent(/8\+ characters: Met/i);
    expect(announcement).toHaveTextContent(/Uppercase letter: Met/i);
    expect(announcement).toHaveTextContent(/Lowercase letter: Met/i);
    expect(announcement).toHaveTextContent(/Number: Met/i);
    expect(announcement).toHaveTextContent(/Special character: Met/i);
  });
});
