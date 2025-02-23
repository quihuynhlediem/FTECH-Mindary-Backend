import MeditationService from '../services/meditationService.js';

const createMeditation = async (req, res) => {
    try {
        const { title, content } = req.body;

        if (!title || !content) {
            return res.status(400).json({ message: 'Title and content are required.' });
        }

        const meditation = await MeditationService.createMeditation(title, content);
        return res.status(201).json(meditation);
    } catch (error) {
        if (error.name === 'ValidationError') {
            return res.status(400).json({ message: error.message });
        }

        console.error('Error creating meditation:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

const createMultipleMeditations = async (req, res) => {
    try {
        const meditationsData = req.body;

        if (!Array.isArray(meditationsData) || meditationsData.length === 0) {
            return res.status(400).json({ message: 'Request body must be a non-empty array of meditations.' });
        }

        const meditations = await MeditationService.createMultipleMeditations(meditationsData);
        return res.status(201).json(meditations);
    } catch (error) {
        if (error.name === 'ValidationError') {
            return res.status(400).json({ message: error.message });
        }

        console.error('Error creating multiple meditations:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

const getAllMeditations = async (req, res) => {
    try {
        const meditations = await MeditationService.getAllMeditations();
        return res.status(200).json(meditations);
    } catch (error) {
        console.error('Error fetching meditations:', error);
        return res.status(500).json({ message: 'An unexpected error occurred while fetching meditations.' });
    }
};

const getMeditationById = async (req, res) => {
    try {
        const meditation = await MeditationService.getMeditationById(req.params.id);
        if (!meditation) {
            return res.status(404).json({ message: 'Meditation not found' });
        }
        return res.status(200).json(meditation);
    } catch (error) {
        console.error('Error fetching meditation by ID:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

const getRecommendedMeditation = async (req, res) => {
    try {
        const recommendedMeditation = await MeditationService.getRecommendedMeditation(req.body);
        
        if (!recommendedMeditation) {
            return res.status(404).json({ message: 'Recommended meditation not found' });
        }
        
        return res.status(200).json(recommendedMeditation);
    } catch (error) {
        console.error('Error fetching recommended meditation:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

const updateMeditation = async (req, res) => {
    try {
        const { title, content } = req.body;
        
        if (!title || !content) {
            return res.status(400).json({ message: 'Title and content are required.' });
        }
        
        const updatedMeditation = await MeditationService.updateMeditation(req.params.id, title, content);
        
        // if (!updatedMeditation) {
        //     return res.status(404).json({ message: 'Meditation not found' });
        // }
        
        return res.status(200).json(updatedMeditation);
    } catch (error) {
        console.error('Error updating meditation:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
    }
};

const deleteMeditation = async (req, res) => {
    try {
        await MeditationService.deleteMeditation(req.params.id);
        return res.status(204).send();
    } catch (error) {
        console.error('Error deleting meditation:', error);
        return res.status(500).json({ message: 'Internal Server Error' });
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
