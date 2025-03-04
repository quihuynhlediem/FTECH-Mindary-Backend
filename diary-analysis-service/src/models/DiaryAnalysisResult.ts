import mongoose from "mongoose";

const diarySchema = new mongoose.Schema(
    {
        senderId: {
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
        recommendations: [
            {
                type: Object,
                practice: {
                    type: String,
                },
                action: {
                    type: String,
                }
            }
        ],
        imageLink: [
            String
        ]
    },
    {timestamps: true}
);

const DiaryAnalysisResult = mongoose.models.diaries || mongoose.model("diaries", diarySchema);

export default DiaryAnalysisResult;