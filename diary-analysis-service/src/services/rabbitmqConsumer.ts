import { getChannel } from "../configs/rabbitmqConfig";
import config from "../configs/systemConfig";
import { DiaryDto } from "../types/diary";
import { analyzeDiaryEntry } from "./diaryAnalysisService";
import {publishAnalysisResult} from "./rabbitmqPublisher";

const receiveDiary = async () => {
    const channel = getChannel();
    if (!channel) {
        console.error('RabbitMQ channel not available.');
        return;
    }

    await channel.consume(config.DIARY_ENTRY_QUEUE, async (msg: any) => {
        if (msg !== null) {
            try {
                const diaryData: string = msg.content.toString(); // Convert Buffer to string
                const parsedData: DiaryDto = JSON.parse(diaryData); // Parse JSON if applicable
                console.log("Received Diary Entry:", parsedData.content);

                const diaryAnalysisResult = await analyzeDiaryEntry(parsedData.userId, parsedData.id, parsedData.content)
                console.log("Diary Analysis Result:" + diaryAnalysisResult)

                await publishAnalysisResult(diaryAnalysisResult)
                console.log("Published Analysis Result")

                channel.ack(msg); // Acknowledge message after processing
            } catch (error) {
                console.error("Error parsing message:", error);
            }
        }
    }, { noAck: false }); // Ensure messages are acknowledged after processing
};

export { receiveDiary };
