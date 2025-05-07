import amqplib from "amqplib";
import config from "./systemConfig";

let channel: any;

const connectToRabbitMQ = async ()=> {
    try {
        const connection = await amqplib.connect(config.RABBITMQ_CONNECTION_STRING); // Adjust connection string
        channel = await connection.createChannel();

        await channel.assertExchange(config.EXCHANGE_NAME, 'topic');
        await channel.assertQueue(config.DIARY_ENTRY_QUEUE);
        await channel.assertQueue(config.RESULT_QUEUE);
        await channel.bindQueue(config.DIARY_ENTRY_QUEUE, config.EXCHANGE_NAME, config.ANALYSIS_ROUTING_KEY);
        await channel.bindQueue(config.RESULT_QUEUE, config.EXCHANGE_NAME, config.RESULT_ROUTING_KEY);

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