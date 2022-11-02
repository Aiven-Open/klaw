import { setupServer } from "msw/node";
import { handlers } from "src/domain/api-mocks/handlers";

export const server = setupServer(...handlers);
