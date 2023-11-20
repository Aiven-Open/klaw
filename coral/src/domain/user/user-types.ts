import { KlawApiModel, Paginated, ResolveIntersectionTypes } from "types/utils";

type UserListApiResponse = ResolveIntersectionTypes<Paginated<User[]>>;

type User = KlawApiModel<"UserInfoModelResponse">;

export type { User, UserListApiResponse };
