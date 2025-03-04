import { Request, Response } from 'express'
import { ChatPromptTemplate } from "@langchain/core/prompts"
import { llmModel } from "../lib/modelConfiguration"
import { z, ZodVoid } from "zod";
import sharp from 'sharp';
import { uploadToS3 } from '../lib/awsConfiguration';
import DiaryAnalysisResult from '../models/DiaryAnalysisResult';

const emotionAnalyzePrompt = ChatPromptTemplate.fromTemplate(
    "You are a helpful and enthusiastic psychological therapist. You can analyze the following personal diary entry carefully.\
    Step 1: Identify specific factors mentioned in the diary entry such as events (e.g., “big presentation at work”), people (e.g., “spent time with family”), environments (e.g., “felt calm at the park”), activities (e.g., “went for a run”), and internal thoughts or beliefs (e.g., “felt I wasn’t good enough”) that may be influencing the user’s mood.\
    Differentiate between short-term influences (temporary factors such as stressful meetings or uplifting social interactions, etc. that might only appear once or twice but lead to immediate mood changes) and long-term patterns (recurring themes that indicates ongoing relationship stress or frequent self-doubt such as ongoing relationship stress or frequent self-doubt, etc.)\
    As you analyze the user’s journal, don’t just look for patterns that affect user's emotion, not only treat them like data points. Consider how these patterns resonate emotionally. For instance, repetitive negative interactions might increase sadness or anxiety, whereas routine exercise might boost positivity. Reflect on how each pattern relates to their emotional experiences.\
    Step 2: Instead of just pointing out correlations, show the user that you care about their well-being. For example, if their anxiety spikes after social events, gently acknowledge it: “It looks like socializing has been a little tough for you. I know it can be draining sometimes, but it’s okay to take time for yourself.” or if family gatherings seem to cause stress, gently acknowledge this with supportive language: “I can see that family gatherings have been a bit challenging lately. It’s understandable to feel this way, and taking a little time for yourself afterward might help you recharge.”\
    Step 3: When suggesting patterns, be mindful not to overwhelm the user. Frame your insights in a way that offers comfort and guidance. Say things like, “It seems like mornings have been particularly hard for you. Maybe we could try to create a morning routine to help ease into the day.” or “It looks like extended screen time may leave you feeling drained. Setting small breaks away from screens could help keep your energy up throughout the day.” when they find that too much screen time affects their mood negatively.\
    Step 4: Provide compassionate insights that guide the user toward understanding how certain activities are influencing their emotions and offer a supportive suggestion to help them manage. For instance, if the user mentions feeling irritable after a meeting or social gathering, try suggesting: “It sounds like social situations are a bit draining for you. Taking a moment for yourself after these interactions could help you recharge and feel more balanced.\
    User diary:\
    {input}"
);

const emotionAnalyzeSchema = z.object({
    emotionLevel: z.string().describe("You analyze the following personal diary entry carefully. Rate the mood on a scale from 1 to 5, where 1 is extremely negative and 5 is extremely positive. Additionally, classify the mood into categories (e.g., happy, sad, anxious, calm, etc.). Not only detect emotions, but also consider how the user talks about their day—are they using soft language, or are they clearly in distress? For example, if the user says, “I’m feeling overwhelmed” or “I can’t catch a break,” this indicates a deeper emotional struggle."),
    category: z.array(z.string().describe("Classify the mood into categories (e.g., happy, sad, anxious, calm, etc.).")),
    summary: z.string().describe("Gently compare today’s emotions with previous days. If the user has been improving, offer encouraging words like, “You’ve had a tough few days, but it seems like you’re making progress!” If things are getting harder, say, “It looks like things have been rough lately. It’s okay to have those days, and I’m here with you. Create a summary that feels empathetic, validating the user’s feelings. Avoid robotic phrasing—focus on making them feel understood and supported."),
});

const emotionAnalyzeOutput = llmModel.withStructuredOutput(emotionAnalyzeSchema, {
    name: "emotion",
});

const emotionChain = emotionAnalyzePrompt.pipe(emotionAnalyzeOutput);

const correlationAnalyzePrompt = ChatPromptTemplate.fromTemplate(
    "You are a helpful and enthusiastic psychological therapist. You can analyze the following personal diary entry carefully.\
    Step 1: Identify specific factors mentioned in the diary entry such as events (e.g., “big presentation at work”), people (e.g., “spent time with family”), environments (e.g., “felt calm at the park”), activities (e.g., “went for a run”), and internal thoughts or beliefs (e.g., “felt I wasn’t good enough”) that may be influencing the user’s mood.\
    With each factor your recognize, you should label them as short-term or long-term\
    As you analyze the user’s journal, don’t just look for patterns that affect user's emotion, not only treat them like data points. Consider how these patterns resonate emotionally. For instance, repetitive negative interactions might increase sadness or anxiety, whereas routine exercise might boost positivity. Reflect on how each pattern relates to their emotional experiences.\
    Step 2: Instead of just pointing out correlations, show the user that you care about their well-being. For example, if their anxiety spikes after social events, gently acknowledge it: “It looks like socializing has been a little tough for you. I know it can be draining sometimes, but it’s okay to take time for yourself.” or if family gatherings seem to cause stress, gently acknowledge this with supportive language: “I can see that family gatherings have been a bit challenging lately. It’s understandable to feel this way, and taking a little time for yourself afterward might help you recharge.”\
    Step 3: When suggesting patterns, be mindful not to overwhelm the user. Frame your insights in a way that offers comfort and guidance. Say things like, “It seems like mornings have been particularly hard for you. Maybe we could try to create a morning routine to help ease into the day.” or “It looks like extended screen time may leave you feeling drained. Setting small breaks away from screens could help keep your energy up throughout the day.” when they find that too much screen time affects their mood negatively.\
    Step 4: Provide compassionate insights that guide the user toward understanding how certain activities are influencing their emotions and offer a supportive suggestion to help them manage. For instance, if the user mentions feeling irritable after a meeting or social gathering, try suggesting: “It sounds like social situations are a bit draining for you. Taking a moment for yourself after these interactions could help you recharge and feel more balanced.\
    User diary:\
    {input}"
)

const correlationAnalyzeSchema = z.object({
    correlations: z.array(z.object({
        name: z.string().describe("List of factor with short-term or long-term labels"),
        description: z.string().describe("Include the compassionate insights for the correlation factor"),
    }))
});

const correlationAnalyzeOutput = llmModel.withStructuredOutput(correlationAnalyzeSchema, {
    name: "correlation",
});

const correlationChain = correlationAnalyzePrompt.pipe(correlationAnalyzeOutput);

const mentalHealthAnalyzePrompt = ChatPromptTemplate.fromTemplate(
"You are a helpful and enthusiastic psychological therapist. As you read through the user’s journal entries, you should detect any signs or language that may indicate potential symptoms of mental health disorders (e.g., anxiety, depression, stress). \
			Where applicable, assess the severity of risk (e.g., mild, moderate, or high) based on recurring or extreme patterns in the text. \
			Don’t just look for symptom keywords—listen to how the user is describing their struggles. If they talk about exhaustion or sadness, consider how long these feelings have been with them, but approach it gently. \
			Step 1: Instead of flagging symptoms in a clinical way, use soft, comforting language. If you notice signs of burnout, say, “I’ve noticed you’ve been really tired lately, even when you’ve had enough sleep. It sounds like your body is asking for a bit of a break, and that’s okay.”\
			Step 2: Be mindful of how often these emotions are recurring, but express concern in a caring manner. For example, if sadness persists, say, “It seems like you’ve been feeling down for a little while now. It’s completely understandable to feel this way, but I just want to make sure you’re taking care of yourself.”\
			Step 3: If the user shows signs of needing help, gently encourage them to reach out without sounding alarming. Say things like, “If you’re finding it hard to cope, talking to someone could really help lighten the load. I’m here for you too, and we can take it step by step.”\
			Step 4: Always offer kindness and support. Suggest self-care strategies, but also reassure the user that seeking help is okay if they need it. \
User Diary:\
{input}"
)

const mentalHealthAnalyzeSchema = z.object({
    symptoms: z.array(
        z.object({
            name: z.string().describe("List of symptom name"),
            risk: z.string().describe("Include the severity level"),
            description: z
                .string()
                .describe("Explanation for the cause of the symptom"),
            suggestions: z.string().describe("Support healthcare strategies"),
        }),
    ),
});

const mentalHealthAnalyzeOutput = llmModel.withStructuredOutput(mentalHealthAnalyzeSchema, {
    name: "symptoms",
});

const mentalHealthChain = mentalHealthAnalyzePrompt.pipe(mentalHealthAnalyzeOutput);

export const analyzeDiaryEntry = async (userId: string, diaryId: string, input: string, uploadFile?: Express.Multer.File) => {
    try {
        // Invoke AI model
        const correlationAnalyzeResult = await correlationChain.invoke({ input });
        const emotionAnalyzeResult = await emotionChain.invoke({ input });
        const mentalHealthAnalyzeResult = await mentalHealthChain.invoke({ input });

        let imageUrl: string | undefined;
        if (uploadFile) {
            const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
            const imageName = `${uniqueSuffix}-${uploadFile.originalname}`;
            const fileBuffer = await sharp(uploadFile.buffer).jpeg({ quality: 80 }).toBuffer();
            imageUrl = await uploadToS3(fileBuffer, imageName, uploadFile.mimetype);
        }

        // Create a new diary analysis record
        const newDiary = new DiaryAnalysisResult({
            senderId: userId,
            diaryId: diaryId,
            emotionObjects: [{
                emotionLevel: emotionAnalyzeResult.emotionLevel,
                emotionCategory: emotionAnalyzeResult.category,
                emotionSummary: emotionAnalyzeResult.summary,
            }],
            correlationObjects: correlationAnalyzeResult.correlations.map(correlation => ({
                name: correlation.name,
                description: correlation.description,
            })),
            symptomObjects: mentalHealthAnalyzeResult.symptoms.map(symptom => ({
                name: symptom.name,
                risk: symptom.risk,
                description: symptom.description,
                suggestions: symptom.suggestions,
            })),
            imageLink: imageUrl ? [imageUrl] : [],
        });

        await newDiary.save();

        return {
            success: true,
            message: "Diary analysis completed successfully",
            result: {
                emotion: emotionAnalyzeResult,
                correlation: correlationAnalyzeResult,
                mentalHealth: mentalHealthAnalyzeResult,
            },
        };
    } catch (error) {
        throw new Error(error.message || "Error processing diary analysis");
    }
};