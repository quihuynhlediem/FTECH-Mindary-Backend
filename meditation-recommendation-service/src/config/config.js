import dotenv from 'dotenv';

dotenv.config(); 

const config = {
    NODE_ENV: process.env.NODE_ENV || 'development',
    PORT: process.env.PORT || 8084,
    MONGODB_URI: process.env.MONGODB_URI,
    MONGODB_ATLAS_DB_NAME: process.env.MONGODB_ATLAS_DB_NAME,
    MONGODB_ATLAS_COLLECTION_NAME: process.env.MONGODB_ATLAS_COLLECTION_NAME,
    GEMINI_API_KEY: process.env.GEMINI_API_KEY,
    JWT_SECRET: process.env.JWT_SECRET,
    JWT_ALGORITHM: 'HS256',
    SERVICE_TITLE: 'Meditation Recommendation Service',
    SERVICE_VERSION: '1.0.0',
    SERVICE_DESCRIPTION: 'This is the API documentation for Meditation Recommendation Service.'
};

export default config;