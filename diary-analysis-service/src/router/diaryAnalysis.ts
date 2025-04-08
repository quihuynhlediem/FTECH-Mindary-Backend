import { upload } from "../lib/multerSetup";
// import { diaryAnalysisResult, getAnalysisResult, updateAnalysisResult, deleteAnalysisResult } from "../controllers/diaryAnalysisController";
import { diaryAnalysisResult, getAnalysisResult} from "../controllers/diaryAnalysisController";
import express, { Router } from "express";

export default (router: express.Router) => {
  router.post("/diary/analyze", upload.single('image'), diaryAnalysisResult);
  router.get("/diary/", getAnalysisResult);
  // router.put("/diary/:diaryId", updateAnalysisResult);
  // router.delete("/diary/:diaryId", deleteAnalysisResult);
  return router;
};