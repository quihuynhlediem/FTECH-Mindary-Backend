import config from "../config/config.js";
import jwt from "jsonwebtoken"

const decodeJWT = (req, res, next) => {
    const authHeader = req.header("Authorization")
    const token = authHeader && authHeader.split(' ')[1]

    jwt.verify(token, config.JWT_SECRET, {algorithms: [config.JWT_ALGORITHM]}, (err, decoded) => {
        if (err) {
            console.error("JWT Verification Error:", err);
            return res.status(403).json({message: "Failed to authenticate token", error: err.message})
        }
        console.log(decoded.userId);
        req.user = decoded;
        next();
    })
};

const authorizeUser = (req, res, next) => {
    if (!req.user) {
        console.warn("req.user is unexpectedly missing. Ensure verifyJWT middleware is used before this.");
        return res.status(401).json({ message: 'Authentication required' });
    }

    const pathUserId = String(req.params.userId);
    const jwtUserId = req.user.userId;

    if (pathUserId !== jwtUserId) {
        console.warn(`ensureUserMatchesParam: Authorization failed: userId in path (${pathUserIdString}) does not match username in JWT (${jwtUsername})`);
        return res.status(403).json({message: "Forbidden. You are not authorized to access this resource for this user."})
    }
};

module.exports = {
    decodeJWT,
    authorizeUser
}