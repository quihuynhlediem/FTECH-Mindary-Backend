import { getChannel } from "../config/rabbitmqConnection.js";
import config from "../config/config.js";

const receiveDiary = async () => {
    const channel = getChannel();
    if (!channel) {
        console.error('RabbitMQ channel not available.');
        return;
    }

    await channel.consume(config.DIARY_ENTRY_QUEUE, (msg) => {
        if (msg !== null) {
            try {
                const diaryData = msg.content.toString(); // Convert Buffer to string
                const parsedData = JSON.parse(diaryData); // Parse JSON if applicable
                console.log("Received Diary Entry:", parsedData);

                channel.ack(msg); // Acknowledge message after processing
            } catch (error) {
                console.error("Error parsing message:", error);
            }
        }
    }, { noAck: false }); // Ensure messages are acknowledged after processing
};

export { receiveDiary };
