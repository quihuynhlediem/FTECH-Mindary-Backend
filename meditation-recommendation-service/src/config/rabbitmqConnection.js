import amqplib from "amqplib";
import config from "./config.js";

let channel;

async function connectToRabbitMQ() {
    try {
        const connection = await amqplib.connect('amqp://guest:guest@localhost:5672'); // Adjust connection string
        channel = await connection.createChannel();

        await channel.assertExchange(config.EXCHANGE_NAME, 'topic');
        await channel.assertQueue(config.DIARY_ENTRY_QUEUE);
        await channel.assertQueue(config.FEEDBACK_QUEUE);
        await channel.bindQueue(config.DIARY_ENTRY_QUEUE, config.EXCHANGE_NAME, config.ANALYSIS_ROUTING_KEY);
        await channel.bindQueue(config.FEEDBACK_QUEUE, config.EXCHANGE_NAME, config.FEEDBACK_ROUTING_KEY);

        console.log('RabbitMQ connected.');
        return channel;

    } catch (error) {
        console.error('Error connecting to RabbitMQ:', error);
        return null;
    }
}

function getChannel() {
    return channel;
}

export {
    connectToRabbitMQ,
    getChannel,
};