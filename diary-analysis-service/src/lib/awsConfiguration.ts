import dotenv from 'dotenv';
import { S3Client, PutObjectCommand } from "@aws-sdk/client-s3";
//For env File 
dotenv.config();

// AWS S3 set up
const bucketName = process.env.AWS_BUCKET;
const region = process.env.AWS_S3_REGION;
const accessKeyId = process.env.AWS_ACCESS_KEY_ID;
const secretAccessKey = process.env.AWS_SECRET_ACCESS_KEY;
const s3ClientObject: object = {
	region,
	credentials: {
		accessKeyId,
		secretAccessKey,
	},
};

const s3Client = new S3Client(s3ClientObject);

export const uploadToS3 = async (file: any, fileName: string, type: string) => {
	const fileBuffer = file;

	const params = {
		Bucket: bucketName,
		Key: `${fileName}`,
		Body: fileBuffer,
		ContentType: type,
	};
	const command = new PutObjectCommand(params);
	await s3Client.send(command);
	const publicUrl = `https://${bucketName}.s3.${region}.amazonaws.com/${params.Key}`;
	return publicUrl;
}