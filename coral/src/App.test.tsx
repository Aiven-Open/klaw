import App from "./App"
import { render, screen } from "@testing-library/react";


describe("pp.tsx", ()=> {
  it("shows a headline", ()=> {
    render(<App />)
    const heading = screen.getByRole("heading")

    expect(heading).toHaveTextContent("Hello Klaw 👋")
  })
})