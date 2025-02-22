import { upload } from "../lib/multerSetup";
import { analyze } from "../controllers/diary";
import express, {Router} from "express";


export default (router: express.Router) => {
  router.post("/diary/analyze", upload.single('image'), analyze);
};