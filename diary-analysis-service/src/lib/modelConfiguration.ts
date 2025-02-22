import { ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings } from "@langchain/google-genai";
import { HarmBlockThreshold, HarmCategory } from "@google/generative-ai";
import dotenv from 'dotenv';
dotenv.config();

export const llmModel = new ChatGoogleGenerativeAI({
	modelName: "gemini-1.5-pro",
	temperature: 0.6,
	apiKey: process.env.GEMINI_API_KEY,
	safetySettings: [
		{
		  category: HarmCategory.HARM_CATEGORY_HARASSMENT,
		  threshold: HarmBlockThreshold.BLOCK_LOW_AND_ABOVE,
		},
	],
});

export const embedding = new GoogleGenerativeAIEmbeddings({
	apiKey: process.env.GEMINI_API_KEY,
});