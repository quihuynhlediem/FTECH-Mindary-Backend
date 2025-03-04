import express from "express";

import authentication from "./authentication";
import user from "./user";
import diary from "./diaryAnalysis";
// import users from "./users";

const router = express.Router();

export default (): express.Router => {
  authentication(router);
  user(router);
  diary(router);
//   users(router)
  return router;
};