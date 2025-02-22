
import express, { Express, Request, Response , Application } from 'express';
import dotenv from 'dotenv';
import cors from "cors"
import compression from 'compression';
import cookieParser from 'cookie-parser';
import bodyParser from 'body-parser';
import morgan from "morgan";
import mongoose from 'mongoose';
import http from "http";
import router from './router/index';

//For env File 
dotenv.config();

const app: Application = express();
const port = process.env.PORT || 8000;

app.use(
    cors({
        credentials: true,
    })
);

app.use(compression());
app.use(cookieParser());
app.use(express.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(morgan("common"));

const server = http.createServer(app);

// app.get('/', (req: Request, res: Response) => {
//   res.send('Welcome to Express & TypeScript Server');
// });

mongoose.Promise = Promise;
mongoose
        .connect(process.env.MONGO_URI)
        .then(() => {
            server.listen(port, () => {
                console.log('MongoDB is connected')
                console.log(`Server is connected at http://localhost:${port}`);
            });
        })
        .catch((error) => console.log(`${error}. SERVER IS NOT CONNECTING`));

app.use('/api', router());