
// import { diaryAnalysisResult, getAnalysisResult, updateAnalysisResult, deleteAnalysisResult } from "../controllers/diaryAnalysisController";
import { diaryAnalysisResult, getAnalysisResult} from "../controllers/diaryAnalysisController";
import express, { Router } from "express";

export default (router: express.Router) => {
<<<<<<< HEAD
  router.post("/diary/analyze", upload.single('image'), diaryAnalysisResult);
  router.get("/diary/", getAnalysisResult);
=======
  router.post("/diary/analyze", diaryAnalysisResult);
  // router.get("/diary/:diaryId", getAnalysisResult);
>>>>>>> main
  // router.put("/diary/:diaryId", updateAnalysisResult);
  // router.delete("/diary/:diaryId", deleteAnalysisResult);
  return router;
};