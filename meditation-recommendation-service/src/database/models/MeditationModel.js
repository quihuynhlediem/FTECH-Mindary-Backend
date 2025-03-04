import { text } from 'express';
import mongoose from 'mongoose';

const meditationSchema = new mongoose.Schema(
    {
        title: { 
            type: String, 
            required: true, 
            trim: true 
        },
        content: { 
            type: String, 
            required: true, 
            trim: true 
        },
        // embedding: { 
        //     type: [Number], 
        //     required: true 
        // },
    }, { 
        timestamps: true, 
    }
);

const Meditation = mongoose.model('Meditation', meditationSchema);
export default Meditation;
