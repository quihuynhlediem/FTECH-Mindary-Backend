import {getChannel} from "../configs/rabbitmqConfig";
import config from "../configs/systemConfig";
import {DiaryAnalysisDto} from "../types/diary";

const publishAnalysisResult = async (analysisResult: DiaryAnalysisDto) => {
    const channel = getChannel()

    if (!channel) {
        console.error('RabbitMQ channel not available.');
        return;
    }
    try {
        channel.publish(
            config.EXCHANGE_NAME,
            config.RESULT_ROUTING_KEY,
            Buffer.from(JSON.stringify(analysisResult))
        )
    } catch (error) {
        console.error('Error publishing analysis result:', error);
    }
}

export { publishAnalysisResult }