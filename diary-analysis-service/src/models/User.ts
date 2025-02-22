import mongoose from "mongoose";
// import bcrypt from 'bcryptjs';

export interface IUser extends Document {
    username: string;
    email: string;
    password: string;
    matchPassword(enteredPassword: string): Promise<boolean>;
}


const userSchema = new mongoose.Schema(
    {
        username: {
            type: String,
            required: [true, "Please provide username"],
        },
        email: {
            type: String,
            required: [true, "Please provide your email"],
            unique: true
        },
        authentication: {
            password: {
                type: String,
                required: [true, "Please provide your password"],
                select: false,
            },
            salt: {
                type: String,
                select: false,
            },
            sessionToken: {
                type: String,
                select: false,
            }
        }
    },
    {timestamps: true}
);

export const User = mongoose.models.users || mongoose.model("users", userSchema);

export const getUsers = () => User.find();
export const getUserByEmail = (email: string) => User.findOne({ email });
export const getUserBySessionToken = (sessionToken: string) => User.findOne({
       'authentication.sessionToken': sessionToken,
});
export const getUserById = (id: string) => User.findById(id);
export const createUser = (values : Record<string,any>) => new User(values)
.save().then((user: any) => user.toObject());

export const deleteUserById = (id: string) => User.findOneAndDelete({_id:id});
export const updateUserById = (id: string, values: Record<string,any>) => User.findByIdAndUpdate(id,values);