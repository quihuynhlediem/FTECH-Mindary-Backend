import MeditationService from '../services/meditationService.js';

const createMeditation = async (req, res) => {
    try {
        const meditation = await MeditationService.createMeditation(req.body.title, req.body.content);
        res.status(201).json(meditation);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
};

const createMulipleMeditations = async (req, res) => {
    try {
        const meditations = await MeditationService.createMulipleMeditations(req.body);
        res.status(201).json(meditations);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
};

const getAllMeditations = async (req, res) => {
    try {
        const meditations = await MeditationService.getAllMeditations();
        res.json(meditations);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

const getMeditationById = async (req, res) => {
    try {
        const meditation = await MeditationService.getMeditationById(req.params.id);
        if (!meditation) {
            res.status(404).json({ message: 'Meditation not found' });
            return;
        }
        res.json(meditation);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

const updateMeditation = async (req, res) => {
    try {
        const meditation = await MeditationService.updateMeditation(req.params.id, req.body.title, req.body.content);
        if (!meditation) {
            res.status(404).json({ message: 'Meditation not found' });
            return;
        }
        res.json(meditation);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

const deleteMeditation = async (req, res) => {
    try {
        const meditation = await MeditationService.deleteMeditation(req.params.id);
        if (!meditation) {
            res.status(404).json({ message: 'Meditation not found' });
            return;
        }
        res.status(204).send(); 
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

export default { 
    createMeditation, 
    createMulipleMeditations, 
    getAllMeditations, 
    getMeditationById, 
    updateMeditation, 
    deleteMeditation 
};
