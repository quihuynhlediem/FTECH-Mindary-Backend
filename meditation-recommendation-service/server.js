import express from 'express';
import config from './src/config/config.js';
import { connectDB } from './src/database/connection.js';
import routes from './src/routes/index.js';

const app = express();
app.use(express.json());
app.use('/api/v1', routes);

connectDB();

app.get('/', (req, res) => {
  res.send('Hello World!');
});

app.listen(config.PORT, () => {
  console.log(`Server started at http://localhost:${config.PORT}`);
});

