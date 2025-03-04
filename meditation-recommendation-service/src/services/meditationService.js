import Meditation from '../database/models/MeditationModel.js';
import llmModelConfig from '../config/llmModelConfig.js';
import { vectorStore } from '../database/connection.js';
import { Document } from "@langchain/core/documents";
import { v4 as uuidv4 } from "uuid";
import { buildSearchPrompt } from '../utils/prompt.js';
import Embedding from '../database/models/EmbeddingModel.js';
import mongoose, { Types } from 'mongoose';


// const createMeditation = async (title, content) => {
//     try {
//         const meditationId = uuidv4();

//         await vectorStore.addDocuments(
//             [{
//                 pageContent: content,
//                 metadata: { title }
//             }],
//             {
//                 ids: [meditationId]
//             }
//         );

//         return { success: true, message: "Meditation has been created successfully!", id: meditationId };
//     } catch (error) {
//         console.error("Error creating meditation:", error);
//         return { success: false, message: "An error occurred while creating the meditation. Please try again later." };
//     }
// };

const createMeditation = async (title, content) => {
    try {
        // Create a new meditation
        const meditation = await Meditation.create({ title, content });
        const messages = await llmModelConfig.prompt.invoke({
            question: 'Given the following meditation content in the context section. Describe when someone should use this meditation based on their emotional state.',
            context: content,
        });
        // console.log("LLM prompt:", messages);

        const useCase = await llmModelConfig.llm.invoke(messages);
        // console.log("LLM answer:", useCase.content);

        // Add the embedding data (Meditation's usecase) to the vectorStore
        const embeddingId = uuidv4();
        await vectorStore.addDocuments(
            [{
                pageContent: useCase.content,
                metadata: { _meditationId: meditation._id }
            }],
            {
                ids: [embeddingId]
            }
        );
        return { meditation };
    } catch (error) {
        console.error("Error creating meditation:", error);
        return { success: false, message: "An error occurred while creating the meditation. Please try again later." };
    }
};

// const createMultipleMeditations = async (meditationsData) => {
//     //return await Meditation.insertMany(meditationsData);
//     const meditationsWithIds = meditationsData.map(
//         meditation => ({
//         ...meditation,
//         meditationId: uuidv4() 
//     }));

//     const documents = meditationsData.map(
//         (meditation, index) => ({
//         pageContent: meditation.content, 
//         metadata: { 
//             title: meditation.title,
//         }
//     }));

//     await vectorStore.addDocuments(
//         documents,
//         { 
//             ids: meditationsWithIds.map(m => m.meditationId) 
//         }
//     );
// };

const createMultipleMeditations = async (meditationsData) => {
    try {
        // Generate a unique ID for each meditation
        const meditationsWithIds = meditationsData.map(meditation => ({
            ...meditation,
            meditationId: uuidv4() 
        }));

        // Prepare documents for vectorStore insertion
        const documents = meditationsData.map(meditation => ({
            pageContent: meditation.content, 
            metadata: { title: meditation.title }
        }));

        // Add the documents to vectorStore using the generated IDs
        await vectorStore.addDocuments(
            documents,
            { ids: meditationsWithIds.map(m => m.meditationId) }
        );

        return { 
            success: true, 
            message: "Multiple meditations have been created successfully.", 
            ids: meditationsWithIds.map(m => m.meditationId) 
        };
    } catch (error) {
        console.error("Error creating multiple meditations:", error);
        return { 
            success: false, 
            message: "An error occurred while creating multiple meditations. Please try again later." 
        };
    }
};


const getAllMeditations = async () => {
    try {
        const meditations = await Meditation.find();
        return {
            success: true,
            message: "All meditations retrieved successfully.",
            data: meditations
        };
    } catch (error) {
        console.error("Error retrieving meditations:", error);
        return {
            success: false,
            message: "An error occurred while retrieving meditations.",
            data: null
        };
    }
};

const getMeditationById = async (meditationId) => {
    try {
        const embedding = await Embedding.findById(meditationId);
        console.log('Embedding:', embedding);
        // const meditation = await Meditation.findById(meditationId);
        // if (!meditation) {
        //     return {
        //         success: false,
        //         message: "Meditation not found.",
        //         data: null
        //     };
        // }
        // return {
        //     success: true,
        //     message: "Meditation retrieved successfully.",
        //     data: meditation
        // };
    } catch (error) {
        console.error("Error retrieving meditation by ID:", error);
        return {
            success: false,
            message: "An error occurred while retrieving the meditation.",
            data: null
        };
    }
};

const getRecommendedMeditation = async (diaryAnalysis) => {
    try {
        const prompt = buildSearchPrompt({ diaryAnalysis });

        const retrievedMeditations = await vectorStore.similaritySearch(prompt, 10);
        if (!retrievedMeditations || retrievedMeditations.length === 0) {
            throw new Error("No meditations found matching the criteria.");
        }
        console.log("Retrieved meditations:", retrievedMeditations);

        const meditationsContent = retrievedMeditations
            .map(doc => doc.pageContent);
        if (!meditationsContent) {
            throw new Error("Retrieved meditations contain no content.");
        }
        console.log("Meditations content:", meditationsContent);

        const messages = await llmModelConfig.prompt.invoke({
            question: prompt,
            context: meditationsContent,
        });

        const answer = await llmModelConfig.llm.invoke(messages);
        console.log("LLM answer:", answer.content);

        return answer.toJSON().kwargs.content;
    } catch (error) {
        console.error("Error retrieving meditation:", error);
        return { success: false, message: `Failed to retrieve meditation.` };
    }
};

const updateMeditation = async (meditationId, newTitle, newContent) => {
    try {
        const doc = new Document({
            pageContent: newContent,
            metadata: { title: newTitle },
        });
        await vectorStore.addDocuments([doc], { ids: [meditationId] });

        return { success: true, message: "Meditation updated successfully!" };
    } catch (error) {
        console.error("Error updating meditation:", error);
        return { success: false, message: "Failed to update meditation." };
    }
};

// const deleteMeditation = async (meditationId) => {
//     return await vectorStore.delete({ ids: [meditationId] });
// };

const deleteMeditation = async (meditationId) => {
    try {
        // const embedding = await Embedding.findOne({ metadata: { meditationId } });
        // console.log('Embedding:', embedding);
        // await Meditation.deleteOne({ _id: meditationId });
        // await vectorStore.delete({ ids: [meditationId] });
        //console.log('Deleted meditation');
        const embedding = await Embedding.findOne({ metadata: { _meditationId: meditationId } });
        if (!embedding) {
            throw new Error('Embedding not found');
        }
        await Embedding.deleteOne({ _id: embedding._id });
        return;
    } catch (error) {
        console.error('Error deleting meditation:', error);
        throw new Error('Failed to delete meditation');
    }
};

export default { 
    createMeditation, 
    createMultipleMeditations, 
    getAllMeditations, 
    getMeditationById, 
    updateMeditation, 
    deleteMeditation,
    getRecommendedMeditation 
};