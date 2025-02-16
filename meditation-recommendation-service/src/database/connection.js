// import mongoose from 'mongoose';
// import config from '../config/config.js';
// import { MongoDBAtlasVectorSearch } from "@langchain/mongodb"
// import { MongoClient } from "mongodb";
// import llmModelConfig from '../config/llmModelConfig.js';

// const connectDB = async () => {
//     if (!config.MONGODB_URI) {
//         console.error("MONGODB_URI is undefined. Check your .env file.");
//         process.exit(1);
//     }

//     try {
//         conn = await mongoose.connect(config.MONGODB_URI, {
//             dbName: "test",
//         });
//         console.log(`MongoDB Connected: ${conn.connection.host}`);
//     } catch (error) {
//         console.error(`MongoDB Connection Error: ${error.message}`);
//         process.exit(1); 
//     }
// };


// const client = new MongoClient(config.MONGODB_URI || "");

// const collection = client
//   .db(config.MONGODB_ATLAS_DB_NAME)
//   .collection(config.MONGODB_ATLAS_COLLECTION_NAME);

// const vectorStore = new MongoDBAtlasVectorSearch(llmModelConfig.embeddings, {
//   collection: collection,
//   indexName: "default", // The name of the Atlas search index. Defaults to "default"
//   textKey: "content", // The name of the collection field containing the raw content. Defaults to "text"
//   embeddingKey: "embedding", // The name of the collection field containing the embedded text. Defaults to "embedding"
// });

// export default vectorStore;


import mongoose from 'mongoose';
import config from '../config/config.js';
import { MongoDBAtlasVectorSearch } from "@langchain/mongodb";
import llmModelConfig from '../config/llmModelConfig.js';

let vectorStore;

const connectDB = async () => {
    if (!config.MONGODB_URI) {
        console.error("MONGODB_URI is undefined. Check your .env file.");
        process.exit(1);
    }

    try {
        await mongoose.connect(config.MONGODB_URI, { dbName: "test" });
        console.log("Successfully connected to MongoDB.");

        const collection = mongoose.connection.db.collection(config.MONGODB_ATLAS_COLLECTION_NAME);

        if (!vectorStore) {
            vectorStore = new MongoDBAtlasVectorSearch(llmModelConfig.embeddings, {
                collection: collection,
                indexName: "default",
                textKey: "content",
                embeddingKey: "embedding",
            });
            console.log("Successfully connected to MongoDB Atlas Vector Store.");
        }
    } catch (error) {
        console.error(`MongoDB Connection Error: ${error.message}`);
        process.exit(1);
    }
};

export { connectDB, vectorStore};
