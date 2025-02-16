import { text } from 'express';
import mongoose from 'mongoose';

const meditationSchema = new mongoose.Schema(
    {
        _id: { 
            type: String, 
            required: true 
        },
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
        // }
    }, { 
        timestamps: true, 
    }
);

//meditationSchema.index({text: "content" });

const Meditation = mongoose.model('Meditation', meditationSchema);
export default Meditation;
