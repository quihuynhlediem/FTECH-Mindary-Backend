import Meditation from '../database/models/MeditationModel.js';
import config from '../config/config.js';
import { GoogleGenerativeAIEmbeddings } from '@langchain/google-genai';

const embeddingsModel = new GoogleGenerativeAIEmbeddings({
    apiKey: config.GEMINI_API_KEY,
});

const generateEmbedding = async (data) => {
    const result = await embeddingsModel.generateEmbedding();
    return result;
};

const createMeditation = async (meditationData) => {
    return await Meditation.save(meditationData);
};

const createMulipleMeditations = async (meditationsData) => {
    return await Meditation.insertMany(meditationsData);
};

const getAllMeditations = async () => {
    return await Meditation.find();
};

const getMeditationById = async (meditationId) => {
    return await Meditation.findById(meditationId);
};

const updateMeditation = async (meditationId, meditationData) => {
    return await Meditation.findByIdAndUpdate(meditationId, meditationData, { new: true, overwrite: true, runValidators: true });
};

const deleteMeditation = async (meditationId) => {
    return await Meditation.findByIdAndDelete(meditationId);
};

export default { 
    createMeditation, 
    createMulipleMeditations, 
    getAllMeditations, 
    getMeditationById, 
    updateMeditation, 
    deleteMeditation 
};