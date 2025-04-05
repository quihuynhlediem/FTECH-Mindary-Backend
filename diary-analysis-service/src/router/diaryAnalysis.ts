
// import { diaryAnalysisResult, getAnalysisResult, updateAnalysisResult, deleteAnalysisResult } from "../controllers/diaryAnalysisController";
import { diaryAnalysisResult } from "../controllers/diaryAnalysisController";
import express, { Router } from "express";

export default (router: express.Router) => {
  router.post("/diary/analyze", diaryAnalysisResult);
  // router.get("/diary/:diaryId", getAnalysisResult);
  // router.put("/diary/:diaryId", updateAnalysisResult);
  // router.delete("/diary/:diaryId", deleteAnalysisResult);
  return router;
};