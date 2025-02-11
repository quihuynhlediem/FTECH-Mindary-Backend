import express from 'express';
import MeditationController from '../controllers/meditationController.js';

const router = express.Router();

router.post('/create', MeditationController.createMeditation);
router.post('/create/multiple', MeditationController.createMulipleMeditations);

router.get('/get/all', MeditationController.getAllMeditations);
router.get('/get/:id', MeditationController.getMeditationById);

router.delete('/delete/:id', MeditationController.deleteMeditation);

router.put('/update/:id', MeditationController.updateMeditation);


export default router;