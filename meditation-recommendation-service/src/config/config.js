import dotenv from 'dotenv';

dotenv.config(); 

const config = {
    NODE_ENV: process.env.NODE_ENV || 'development',
    PORT: process.env.PORT || 8083,
    MONGODB_URI: process.env.MONGODB_URI,
    MONGODB_ATLAS_DB_NAME: process.env.MONGODB_ATLAS_DB_NAME,
    MONGODB_ATLAS_COLLECTION_NAME: process.env.MONGODB_ATLAS_COLLECTION_NAME,
    GEMINI_API_KEY: process.env.GEMINI_API_KEY,
};

export default config;