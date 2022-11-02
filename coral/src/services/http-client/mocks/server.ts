import { setupServer } from "msw/node";
import { handlers } from "src/services/http-client/mocks/handlers";

export const server = setupServer(...handlers);
