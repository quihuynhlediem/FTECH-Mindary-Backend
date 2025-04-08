import mongoose from "mongoose";

const diarySchema = new mongoose.Schema(
    {
        userId: {
            type: String,
        },
        diaryId: {
            type: String,
        },
        emotionObjects: [
            {
                type: Object,
                emotionLevel: {
                    type: String,
                },
                emotionCategory: {
                    type: String,
                },
                emotionSummary: {
                    type: String,
                }
            }
        ],
        correlationObjects: [
            {
                type: Object,
                name: {
                    type: String,
                },
                description: {
                    type: String,
                },
            }
        ],

        symptomObjects: [
            {
                type: Object,
                name: {
                    type: String,
                },
                risk: {
                    type: String,
                },
                description: {
                    type: String,
                },
                suggestions: {
                    type: String,
                }
            }
        ],
    },
    {timestamps: true}
);

const DiaryAnalysisResult = mongoose.models.diaries || mongoose.model("diaries", diarySchema);

export default DiaryAnalysisResult;