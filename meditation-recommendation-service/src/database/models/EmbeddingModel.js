import { text } from 'express';
import mongoose from 'mongoose';

const embeddingSchema = new mongoose.Schema(
    {
        
        _id: { 
            type: String, 
            required: true 
        },
        useCase: { 
            type: String, 
            required: true 
        },
        embedding: { 
            type: [Number], 
            required: true 
        },
        _meditationId: { 
            type: mongoose.Schema.Types.ObjectId,
            ref: 'Meditation',
            required: true, 
        },
    }, { 
        timestamps: true, 
    }
);

const Embedding = mongoose.model('Embedding', embeddingSchema);
export default Embedding;
