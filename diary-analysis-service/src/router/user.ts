import express, {Router} from "express";
import { getUserProfile } from "../controllers/user";

export default (router: express.Router) => {
  router.get("/user/profile", getUserProfile);
};