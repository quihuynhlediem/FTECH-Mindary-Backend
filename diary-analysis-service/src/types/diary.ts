export interface DiaryDto {
    id: string,
    userId: string,
    content: string,
}

export interface DiaryAnalysisDto {
    senderId: string,
    diaryId: string,
    emotion: Emotion,
    correlations: [Correlation],
    symptoms: [Symptom]
}

export interface Emotion {
    emotionLevel: string,
    category: [string],
    summary: string,
}

export interface Correlation {
    name: string,
    description: string,
}

export interface Symptom {
    name: string,
    risk: string,
    description: string,
    suggestions: string,
}