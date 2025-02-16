import Meditation from '../database/models/MeditationModel.js';
import llmModelConfig from '../config/llmModelConfig.js';
import { vectorStore } from '../database/connection.js';
import { Document } from "@langchain/core/documents";
import { v4 as uuidv4 } from "uuid";

const createMeditation = async (title, content) => {
    const meditationId = uuidv4();

    await vectorStore.addDocuments(
        [{ 
            pageContent: content, 
            metadata: { title } }],
        { 
            ids: [meditationId] 
        }
    );
    return { success: true, message: "Meditation stored successfully!", id: meditationId };
};

const createMulipleMeditations = async (meditationsData) => {
    //return await Meditation.insertMany(meditationsData);
    const meditationsWithIds = meditationsData.map(
        meditation => ({
        ...meditation,
        meditationId: uuidv4() 
    }));

    const documents = meditationsData.map(
        (meditation, index) => ({
        pageContent: meditation.content, 
        metadata: { 
            title: meditation.title,
        }
    }));


    await vectorStore.addDocuments(
        documents,
        { 
            ids: meditationsWithIds.map(m => m.meditationId) 
        }
    );
};

const getAllMeditations = async () => {
    return await Meditation.find();
};

const getMeditationById = async (meditationId) => {
    return await Meditation.findById(meditationId);
};

const updateMeditation = async (meditationId, newTitle, newContent) => {
    try {
        const doc = new Document({
            pageContent: newContent,
            metadata: { title: newTitle },
        });
        await vectorStore.addDocuments(
            [doc], 
            { ids: [meditationId] } 
        );

        return { success: true, message: "Meditation updated successfully!" };
    } catch (error) {
        console.error("Error updating meditation:", error);
        return { success: false, message: "Failed to update meditation." };
    }
};

const deleteMeditation = async (meditationId) => {
    await vectorStore.delete({ ids: [meditationId] });
};

export default { 
    createMeditation, 
    createMulipleMeditations, 
    getAllMeditations, 
    getMeditationById, 
    updateMeditation, 
    deleteMeditation 
};