import { test, expect } from "@playwright/test";

test("has title", async ({ page }) => {
  await page.goto("/");

  await expect(page).toHaveTitle(
    "Klaw | Kafka Self-service Topic Management Portal",
  );
});

test("has login form", async ({ page }) => {
  await page.goto("/");

  const userNameInput = page.getByPlaceholder("Username");
  await expect(userNameInput).toBeVisible();
  await expect(page.getByPlaceholder("Password")).toBeVisible();
});
