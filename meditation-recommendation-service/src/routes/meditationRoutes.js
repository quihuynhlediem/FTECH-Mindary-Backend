import express from 'express';
import MeditationController from '../controllers/meditationController.js';

const router = express.Router();

router.post('/create', MeditationController.createMeditation);
router.post('/create/multiple', MeditationController.createMultipleMeditations);

router.get('/get/all', MeditationController.getAllMeditations);
router.get('/get/:id', MeditationController.getMeditationById);
router.post('/get/recommended', MeditationController.getRecommendedMeditation);

router.delete('/delete/:id', MeditationController.deleteMeditation);

router.put('/update/:id', MeditationController.updateMeditation);

export default router;