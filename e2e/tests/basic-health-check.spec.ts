import { test, expect } from "@playwright/test";

const superAdminUserName = "superadmin";

// ⚠️ The password is the default password set in application.properties
// because that is the one that will work in GitHub.
// If the tests run e.g. locally, where 'superadmin' has a different password,
// this test will fail, because it would need to use the local password.
// We need to make sure that Klaw runs on clean DBs for E2E tests.
// For this basic health check, this is sufficient.
const superAdminPassword = "welcometoklaw";
test("has title", async ({ page }) => {
  await page.goto("/");

  await expect(page).toHaveTitle(
    "Klaw | Kafka Self-service Topic Management Portal",
  );
});

test("user can login with default superadmin user", async ({ page }) => {
  await page.goto("/");
  const loader = page.locator(".preloader");
  await expect(loader).not.toBeVisible();

  await page.getByPlaceholder("Username").fill(superAdminUserName);
  await page.getByPlaceholder("Password").fill(superAdminPassword);
  await page.getByRole("button", { name: "Continue" }).click();

  const profileForSuperAdmin = await page.getByRole("button", {
    name: "superadmin",
    exact: true,
  });

  await expect(profileForSuperAdmin).toBeVisible();
});

test("coral is build", async ({ page }) => {
  await page.goto("/");
  const loader = page.locator(".preloader");
  await expect(loader).not.toBeVisible();

  await page.getByPlaceholder("Username").fill(superAdminUserName);
  await page.getByPlaceholder("Password").fill(superAdminPassword);
  await page.getByRole("button", { name: "Continue" }).click();

  const profileForSuperAdmin = await page.getByRole("button", {
    name: "superadmin",
    exact: true,
  });

  await expect(profileForSuperAdmin).toBeVisible();

  await page.goto("/coral/");

  const coralSuperAdminDialog = await page.getByRole("dialog", {
    name: /you're currently logged in as superadmin\./i,
  });

  await expect(coralSuperAdminDialog).toBeVisible();
});
