import mongoose from 'mongoose';
import config from '../config/config.js';

const connectDB = async () => {
    if (!config.MONGODB_URI) {
        console.error("❌ MONGODB_URI is undefined. Check your .env file.");
        process.exit(1);
    }

    try {
        const conn = await mongoose.connect(config.MONGODB_URI, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
        });
        console.log(`✅ MongoDB Connected: ${conn.connection.host}`);
    } catch (error) {
        console.error(`❌ MongoDB Connection Error: ${error.message}`);
        process.exit(1); 
    }
};

export default connectDB;