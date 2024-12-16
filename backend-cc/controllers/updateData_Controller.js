const { admin, db } = require('../config/firebase');
const bcrypt = require('bcrypt');
const { body, param, validationResult } = require('express-validator');
const createError = require('http-errors');
const multer = require('multer');

// Konfigurasi multer untuk file upload
const storage = multer.memoryStorage(); 
const upload = multer({ storage });


// GET User by ID
/**
 * GET /user
 * Get the authenticated user's data.
 * @param {Request} req
 * @param {Response} res
 */
exports.getUser = async (req, res, next) => {
  try {
    const userId = req.user.id; // Extract userId from the token

    // Fetch user data from Firestore
    const userRef = db.collection('users').doc(userId);
    const doc = await userRef.get();

    if (!doc.exists) {
      return res.status(404).json({ message: "User not found." });
    }

    res.status(200).json({ user: doc.data() });
  } catch (error) {
    console.error("Error in getUser:", error);
    next(createError(500, "Error fetching user data", { error: error.message }));
  }
};

// Update Foto Profil
exports.updateProfilePicture = [
  upload.single('photo'),
  async (req, res, next) => {
    const userId = req.user.id; // Ambil userId dari token
    const file = req.file;

    if (!file) {
      return next(createError(400, 'No file uploaded'));
    }

    try {
      const bucket = admin.storage().bucket();
      const userRef = db.collection('users').doc(userId);
      const doc = await userRef.get();

      if (!doc.exists) {
        throw createError(404, 'User not found');
      }

      const userData = doc.data();
      const oldPhotoURL = userData.photoURL;

      // Hapus file lama jika ada
      if (oldPhotoURL) {
        const oldFilePath = oldPhotoURL.split(`https://storage.googleapis.com/${bucket.name}/`)[1];
        const oldBlob = bucket.file(oldFilePath);

        await oldBlob.delete().catch((err) => {
          console.error(`Error deleting old profile picture: ${err.message}`);
        });
      }

      // Upload file baru
      const filePath = `profile-pictures/${userId}-${Date.now()}.${file.mimetype.split('/')[1]}`;
      const blob = bucket.file(filePath);
      const blobStream = blob.createWriteStream({
        metadata: { contentType: file.mimetype },
      });

      blobStream.on('error', (error) => {
        throw createError(500, 'Error uploading file', { error });
      });

      blobStream.on('finish', async () => {
        const photoURL = `https://storage.googleapis.com/${bucket.name}/${filePath}`;
        await userRef.update({ photoURL });

        res.status(200).json({
          message: 'Profile picture updated successfully',
          photoURL,
        });
      });

      blobStream.end(file.buffer);
    } catch (error) {
      next(createError(500, 'Error updating profile picture', { error }));
    }
  },
];

// Ganti Username
exports.updateUsername = [
  body('newUsername')
    .isLength({ min: 3 })
    .withMessage('Username must be at least 3 characters long'),
  async (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return next(createError(400, 'Validation failed', { details: errors.array() }));
    }

    const userId = req.user.id; // Ambil userId dari token
    const { newUsername } = req.body;

    try {
      const userRef = db.collection('users').doc(userId);
      const doc = await userRef.get();

      if (!doc.exists) {
        throw createError(404, 'User not found');
      }

      await userRef.update({ username: newUsername });
      res.status(200).json({ message: 'Username updated successfully' });
    } catch (error) {
      next(createError(500, 'Error updating username', { error: error.message }));
    }
  },
];

// Reset Password
exports.resetPassword = [
  body('oldPassword').notEmpty().withMessage('Old password is required'),
  body('newPassword')
    .isLength({ min: 6 })
    .withMessage('New password must be at least 6 characters long'),
  body('confirmPassword')
    .custom((value, { req }) => value === req.body.newPassword)
    .withMessage('Passwords do not match'),
  async (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return next(createError(400, 'Validation failed', { details: errors.array() }));
    }

    const userId = req.user.id; // Ambil userId dari token
    const { oldPassword, newPassword } = req.body;

    try {
      const userRef = db.collection('users').doc(userId);
      const doc = await userRef.get();

      if (!doc.exists) {
        throw createError(404, 'User not found');
      }

      const userData = doc.data();

      const isOldPasswordValid = await bcrypt.compare(oldPassword, userData.password);
      if (!isOldPasswordValid) {
        throw createError(401, 'Old password is incorrect');
      }

      const hashedPassword = await bcrypt.hash(newPassword, 10);
      await userRef.update({ password: hashedPassword });

      res.status(200).json({ message: 'Password reset successfully' });
    } catch (error) {
      next(createError(500, 'Error resetting password', { error: error.message }));
    }
  },
];