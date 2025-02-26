import swaggerJsdoc from 'swagger-jsdoc'
import config from "./config/config.js";
import swaggerUi from "swagger-ui-express";

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
                url: `http:/localhost:${8084}`,
            }
        ]
    },
    apis: ['./src/routes/*.js'],
}

const swaggerSpec = swaggerJsdoc(options);

const swaggerDocs = (app, port) => {
    console.log(swaggerSpec)
    app.use('/meditation-recommendation-service', swaggerUi.serve, swaggerUi.setup(swaggerSpec, {explorer: true}))
    app.get('/docs.json', (req, res) => {
        res.setHeader('Content-Type', 'application/json')
        res.send(swaggerSpec)
    })
}

export default swaggerDocs;