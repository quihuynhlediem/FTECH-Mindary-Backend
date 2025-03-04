export interface DiaryAnalysisDto {
    senderId: string
    diaryId: string,
    emotionObjects: [Emotion],
    correlationObjects: [Correlation],
    symptomObjects: [Symptom]
}

export interface Emotion {
    emotionLevel: string,
    emotionCategory: string,
    emotionSummary: string
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