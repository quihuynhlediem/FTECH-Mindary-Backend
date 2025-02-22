import { Request, Response } from 'express'
import { authentication, random } from '../helper';
import jwt from 'jsonwebtoken';
import { createUser, getUserByEmail } from "../models/User";

const generateToken = (id: string) => {
  return jwt.sign(
    { id }, 
    process.env.JWT_SECRET, 
    {
      expiresIn: '30d',
  });
};

export const login = async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;

    if(!email || !password) {
      res.sendStatus(400);
      return;
    }

    const user = await getUserByEmail(email);

    if(!user) {
      res.sendStatus(400);
      return;
    }

    if(!await user.matchPassword(password)){
      res.sendStatus(403);
      return;
    }

    res.status(200).json({user, token: generateToken(user._id.toString())}).end();
    return;
  } catch (error) {
    console.log(error);
    res.sendStatus(400);
    return;
  }
};

export const register = async (req: Request, res: Response) => {
  try {
    const { email, password, username } = req.body;

    if (!email || !password || !username) {
      res.sendStatus(400);
      return;
    }

    const existingUser = await getUserByEmail(email);

    if (existingUser) {
      res.status(400).json({ message: 'User already exists' });
      return;
    }
    const salt = random();
    const user = await createUser({
      email,
      username,
      authentication: {
        salt,
        password: authentication(salt, password),
      },
    });

    res.status(200).json({user, token: generateToken(user._id.toString())});
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: 'Server error' });
    return;
  }
};