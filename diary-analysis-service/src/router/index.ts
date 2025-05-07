import express from "express";
import diary from "./diaryAnalysis";
// import users from "./users";

const router = express.Router();

export default (): express.Router => {
  diary(router);
  return router;
};