import express from 'express';
import MeditationController from '../controllers/meditationController.js';

const router = express.Router();

/**
 * @openapi
 * /meditation/create:
 *   post:
 *     summary: Create a new meditation
 *     description: Creates a new meditation record.
 *     tags:
 *       - Meditations
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               title:
 *                 type: string
 *                 description: Title of the meditation.
 *               content:
 *                 type: string
 *                 description: Content of the meditation.
 *             example:
 *               title: "Mindfulness Meditation"
 *               content: "Focus on your breath..."
 *     responses:
 *       201:
 *         description: Meditation created successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: object # Define your Meditation object schema in a real app
 *               example:
 *                 id: "uuid-example"
 *                 title: "Mindfulness Meditation"
 *                 content: "Focus on your breath..."
 *       400:
 *         description: Bad request - Input validation failed or other client error.
 *       500:
 *         description: Internal server error.
 */
router.post('/create', MeditationController.createMeditation);

/**
 * @openapi
 * /meditation/create/multiple:
 *   post:
 *     summary: Create multiple meditations in bulk
 *     description: Creates multiple meditation records at once.
 *     tags:
 *       - Meditations
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: array
 *             items:
 *               type: object
 *               properties:
 *                 title:
 *                   type: string
 *                   description: Title of a meditation.
 *                 content:
 *                   type: string
 *                   description: Content of a meditation.
 *             example:
 *               - title: "Walking Meditation"
 *                 content: "Focus on the sensation of walking..."
 *               - title: "Body Scan Meditation"
 *                 content: "Bring awareness to different parts of your body..."
 *     responses:
 *       201:
 *         description: Meditations created successfully
 *         content:
 *           application/json:
 *             schema:
 *               type: array # Assuming you return an array of meditations
 *               items:
 *                 type: object # Define your Meditation object schema
 *                 # Example is simplified, refine with your actual Meditation object
 *                 example:
 *                   id: "uuid-example-1"
 *                   title: "Walking Meditation"
 *                   content: "Focus on the sensation of walking..."
 *       400:
 *         description: Bad request - Input validation failed or other client error.
 *       500:
 *         description: Internal server error.
 */
router.post('/create/multiple', MeditationController.createMulipleMeditations);

router.get('/get/all', MeditationController.getAllMeditations);
router.get('/get/:id', MeditationController.getMeditationById);
router.post('/get/recommended', MeditationController.getRecommendedMeditation);

router.delete('/delete/:id', MeditationController.deleteMeditation);

router.put('/update/:id', MeditationController.updateMeditation);

export default router;