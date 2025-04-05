import swaggerJsdoc from 'swagger-jsdoc'
import swaggerUi from "swagger-ui-express";
import config from "./systemConfig";
import {Application} from "express";

const options = {
    definition: {
        openapi: '3.0.0',
        info: {
            title: config.SERVICE_TITLE,
            version: config.SERVICE_VERSION,
            description: config.SERVICE_DESCRIPTION
        },
        servers: [
            {
                url: `http:/localhost:${config.PORT}`,
            }
        ]
    },
    apis: ['./src/router/*.ts'],
}

const swaggerSpec = swaggerJsdoc(options);

const swaggerDocs = (app: Application) => {
    console.log(swaggerSpec)
    app.use('/meditation-recommendation-service', swaggerUi.serve, swaggerUi.setup(swaggerSpec, {explorer: true}))
    app.get('/docs.json', (req, res) => {
        res.setHeader('Content-Type', 'application/json')
        res.send(swaggerSpec)
    })
}

export default swaggerDocs;