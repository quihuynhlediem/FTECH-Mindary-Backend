import { Request, Response } from "express";
import { analyzeDiaryEntry } from "../services/diaryAnalysisService";

export const diaryAnalysisResult = async (req: Request, res: Response): Promise<void> => {
    try {
        const { userId, diary, diaryId } = req.body;
        if (!userId || !diary || !diaryId) {
            res.status(400).json({ message: "Missing required fields: userId, diary, or diaryId." });
            return; // Ensure function execution stops here
        }

        const result = await analyzeDiaryEntry(userId, diaryId, diary, req.file);

        res.status(200).json(result);
    } catch (error) {
        res.status(500).json({ message: error.message || "Internal server error" });
    }
};
