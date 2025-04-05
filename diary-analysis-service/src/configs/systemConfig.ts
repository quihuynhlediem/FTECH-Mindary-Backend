import dotenv from 'dotenv';

dotenv.config();

const config = {
    NODE_ENV: process.env.NODE_ENV || 'development',
    PORT: process.env.PORT || 8085,
    MONGODB_URI: process.env.MONGODB_URI,
    MONGODB_ATLAS_DB_NAME: process.env.MONGODB_ATLAS_DB_NAME,
    MONGODB_ATLAS_COLLECTION_NAME: process.env.MONGODB_ATLAS_COLLECTION_NAME,
    GEMINI_API_KEY: process.env.GEMINI_API_KEY,
    JWT_SECRET: process.env.JWT_SECRET,
    JWT_ALGORITHM: 'HS256',
    SERVICE_TITLE: 'Diary Analysis Service',
    SERVICE_VERSION: '1.0.0',
    SERVICE_DESCRIPTION: 'This is the API documentation for Diary Analysis Service.',
    RABBITMQ_CONNECTION_STRING: 'amqp://guest:guest@localhost:5672',
    EXCHANGE_NAME: 'diary_exchange',
    ANALYSIS_ROUTING_KEY: 'diary_analysis_routing_key',
    RESULT_ROUTING_KEY: 'diary_analysis_result_routing_key',
    DIARY_ENTRY_QUEUE: 'diary_analysis',
    RESULT_QUEUE: 'diary_analysis_result',
};

export default config;