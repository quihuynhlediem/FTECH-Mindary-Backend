import config from '../config/config.js';
import { ChatGoogleGenerativeAI, GoogleGenerativeAIEmbeddings } from '@langchain/google-genai';
import { TaskType } from '@google/generative-ai';
import { HarmBlockThreshold, HarmCategory } from "@google/generative-ai";
import { ChatPromptTemplate } from "@langchain/core/prompts";

const prompt = ChatPromptTemplate.fromMessages([
	[
	  "human",
	  "You are an assistant for question-answering tasks. Use the following pieces of retrieved context to answer the question. If you don't know the answer, just say that you don't know. Use three sentences maximum and keep the answer concise. Question: {question} Context: {context} Answer:"
	],
  ]);

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

export default { llm, embeddings, prompt };
