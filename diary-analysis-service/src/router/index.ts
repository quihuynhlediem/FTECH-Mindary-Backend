import express from "express";
import diary from "./diary";

const router = express.Router();

export default (): express.Router => {
  diary(router);
  return router;
};