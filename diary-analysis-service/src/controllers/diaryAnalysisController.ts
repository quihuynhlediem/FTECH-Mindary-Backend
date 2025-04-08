import { Request, Response } from "express";
import { analyzeDiaryEntry, getDiaryAnalysis, updateDiaryAnalysis, deleteDiaryAnalysis } from "../services/diaryAnalysisService";

export const diaryAnalysisResult = async (req: Request, res: Response): Promise<void> => {
    try {
        const { userId, diary, diaryId } = req.body;
        if (!userId || !diary || !diaryId) {
            res.status(400).json({ message: "Missing required fields: userId, diary, or diaryId." });
            return;
        }

        const result = await analyzeDiaryEntry(userId, diaryId, diary, req.file);

        res.status(200).json(result);
    } catch (error) {
        res.status(500).json({ message: error.message || "Internal server error" });
    }
};

export const getAnalysisResult = async (req: Request, res: Response): Promise<void> => { 
 	try {
            const userId = req.query.userId as string;
            // const date = new Date(params.date as string);
          // const diaryId = req.query.diaryId as string;
          const date = req.query.createdAt as string;
            // let previousDate = new Date(params.date as string);
          // previousDate = new Date(previousDate.setDate(date.getDate() - 7));
          

    
            const result = await getDiaryAnalysis(userId, date);
            res.status(200).json(result);
        } catch (error) {
            res.status(500).json({ message: error.message || "Internal server error" });
        }

}

// // Get Analysis Result by diaryId
// export const getAnalysisResult = async (req: Request, res: Response): Promise<void> => {
//     try {
//         const { diaryId } = req.params;
//         if (!diaryId) {
//             res.status(400).json({ message: "Missing diaryId." });
//             return;
//         }

//         const result = await getDiaryAnalysis(diaryId);
//         if (!result) {
//             res.status(404).json({ message: "Analysis result not found." });
//             return;
//         }

//         res.status(200).json(result);
//     } catch (error) {
//         res.status(500).json({ message: error.message || "Internal server error" });
//     }
// };

// // Update Analysis Result
// export const updateAnalysisResult = async (req: Request, res: Response): Promise<void> => {
//     try {
//         const { diaryId } = req.params;
//         const { updatedData } = req.body;

//         if (!diaryId || !updatedData) {
//             res.status(400).json({ message: "Missing diaryId or updated data." });
//             return;
//         }

//         const result = await updateDiaryAnalysis(diaryId, updatedData);

//         res.status(200).json(result);
//     } catch (error) {
//         res.status(500).json({ message: error.message || "Internal server error" });
//     }
// };

// // Delete Analysis Result
// export const deleteAnalysisResult = async (req: Request, res: Response): Promise<void> => {
//     try {
//         const { diaryId } = req.params;

//         if (!diaryId) {
//             res.status(400).json({ message: "Missing diaryId." });
//             return;
//         }

//         await deleteDiaryAnalysis(diaryId);

//         res.status(200).json({ message: "Analysis result deleted successfully." });
//     } catch (error) {
//         res.status(500).json({ message: error.message || "Internal server error" });
//     }
// };
