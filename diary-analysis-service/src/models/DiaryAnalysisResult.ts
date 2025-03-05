import mongoose from "mongoose";

const emotionSchema = new mongoose.Schema(
    {
        emotionLevel: {
            type: String,
        },
        category: [{
            type: String,
        }],
        summary: {
            type: String,
        }
    },
    {
        _id: false
    }
)

const correlationSchema = new mongoose.Schema(
    {
        name: {
            type: String,
        },
        description: {
            type: String,
        },
    },
    {
        _id: false
    }
)

const symptomSchema = new mongoose.Schema(
    {
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
    },
    {
        _id: false
    }
)

const diarySchema = new mongoose.Schema(
    {
        senderId: {
            type: String,
        },
        diaryId: {
            type: String,
        },
        emotion: emotionSchema,
        correlations: [correlationSchema],
        symptoms: [symptomSchema],
    },
    {timestamps: true}
);

diarySchema.index({ senderId: 1 })
diarySchema.index({ diaryId: 1 })

const DiaryAnalysisResult = mongoose.models.diaries || mongoose.model("diaries", diarySchema);

export default DiaryAnalysisResult;