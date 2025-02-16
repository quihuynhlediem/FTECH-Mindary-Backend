import config from '../config/config.js';
import { ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings } from '@langchain/google-genai';
import { TaskType } from '@google/generative-ai';
import { HarmBlockThreshold, HarmCategory } from "@google/generative-ai";

const llm = new ChatGoogleGenerativeAI({
	modelName: "gemini-1.5-flash",
	temperature: 0, // 0.6 in MTan sample
	apiKey: config.GEMINI_API_KEY,
	safetySettings: [
		{
		  category: HarmCategory.HARM_CATEGORY_HARASSMENT,
		  threshold: HarmBlockThreshold.BLOCK_LOW_AND_ABOVE,
		},
	],
});

const embeddings = new GoogleGenerativeAIEmbeddings({
    modelName: "text-embedding-004",
    apiKey: config.GEMINI_API_KEY,
    taskType: TaskType.RETRIEVAL_DOCUMENT,  // TaskType.SEMANTIC_SIMILARITY??
});


export default { llm, embeddings };
