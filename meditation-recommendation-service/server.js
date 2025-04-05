import express from 'express';
import config from './src/config/config.js';
import { connectDB } from './src/database/connection.js';
// import routes from './src/routes/index.js';
import router from './src/routes/index.js'
import swaggerDocs from "./src/config/swaggerConfig.js"
import cors from "cors"
import { connectToRabbitMQ } from "./src/config/rabbitmqConnection.js";
import { receiveDiary } from "./src/services/rabbitmqConsumer.js";

const app = express();
app.use(cors());
app.use(express.json());
app.use('/api/v1', router);

connectDB();

app.get('/', (req, res) => {
  res.send('Hello World!');
});

app.listen(config.PORT, async () => {
  console.log(`Server started at http://localhost:${config.PORT}`);
  swaggerDocs(app, config.PORT)
  const channel = await connectToRabbitMQ();
  if (channel) {
    receiveDiary(); // Start consuming messages after successful connection
  } else {
    console.error("Failed to connect to RabbitMQ.");
  }
  // receiveDiary();
});

