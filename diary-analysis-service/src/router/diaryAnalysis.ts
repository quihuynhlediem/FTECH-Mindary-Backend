import { upload } from "../lib/multerSetup";
import { diaryAnalysisResult } from "../controllers/diaryAnalysisController";
import express, { Router } from "express";

export default (router: express.Router) => {
  router.post("/diary/analyze", upload.single('image'), diaryAnalysisResult);
  return router;
};