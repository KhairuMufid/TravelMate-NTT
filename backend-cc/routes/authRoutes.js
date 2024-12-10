const express = require('express');
const { register, login, googleSignIn, logout, refreshToken } = require('../controllers/authController');
const { updateProfilePicture,  updateUsername, resetPassword, getUserById } = require('../controllers/updateData_Controller');
const { submitAssessment } = require('../controllers/assessmentController');
const { getWishlist, addWishlist, removeWishlist } = require('../controllers/wishlistController');
const {
  getStories,
  addStory,
  editStory,
  deleteStory,
  toggleLikeStory,
  addComment,
  editComment,
  deleteComment,
  getComments,
} = require("../controllers/storyController");
const passport = require('passport');
const { verifyToken } = require('../middleware/authMiddleware');
const { validateRevokeToken } = require('../middleware/revokeMiddleware');
const { uploadDestinations, uploadHotels, uploadHotelPicture, uploadDestinationPicture} = require('../controllers/uploadController');
const { getAllDestinations, getSimilarDestinations, getDestinationsByType, getDestinationMapsLink  } = require('../controllers/destinationController');
const { getHotels, getHotelMapsLink } = require('../controllers/hotelController');

const multer = require('multer');

const router = express.Router();

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 5 * 1024 * 1024 }, // Maksimal 5MB
});

// Registrasi (email + password)
router.post('/register', register);

// Login
router.post('/login', login);

// Regis/login with Google
router.post('/google-signin', googleSignIn);

// Logout
router.post('/logout', validateRevokeToken, logout);

// Refresh Token
router.post('/refresh-token', refreshToken);

// Redirect user to Google's OAuth 2.0 consent screen
router.get('/google', passport.authenticate('google', { scope: ['profile', 'email'] }));

// Handle Google OAuth 2.0 callback
router.get('/google/callback',
  passport.authenticate('google', { failureRedirect: '/login-failed' }),
  (req, res) => {
    res.status(200).json({ message: 'Login successful', user: req.user });
  }
);

router.get('/login-failed', (req, res) => {
  res.status(401).json({ message: 'Google Login failed' });
});

// Get user by ID
router.get('/user/:id', verifyToken, getUserById);

// Update foto profil
router.put('/user/:id/profile-picture', verifyToken, updateProfilePicture);

// Ganti username
router.put('/user/:id/username', verifyToken, updateUsername);

// Reset password
router.put('/user/:id/password', verifyToken, resetPassword);

// user assessment
router.post('/assessment', verifyToken, submitAssessment);

// data destinasi
router.post('/recommend-destinations', verifyToken, getAllDestinations);

// detail data destinasi
router.get('/similar-destinations/:destinationId', getSimilarDestinations);

// Endpoint untuk rekomendasi hotel berdasarkan destinasi
router.get('/recommend-hotels/:destinationId', getHotels);

// mendapatkan destinasi berdasarkan jenis objek
router.get('/destinations/:jenis', getDestinationsByType);

// mendapatkan link destinasi 
router.get("/destinations/:destinationId/maps-link", getDestinationMapsLink);

// mendapatkan link hotel 
router.get("/hotels/:hotelId/maps-link", getHotelMapsLink);

// Get wishlist user tertentu
router.get('/wishlist/:userId', getWishlist);

// menambahkan wishlist
router.post('/wishlist', verifyToken, addWishlist);

// menghapus wishlist
router.delete('/wishlist/:id', verifyToken, removeWishlist);

// Endpoint untuk menambahkan story baru
router.post("/stories", verifyToken, addStory);

// Endpoint untuk mendapatkan semua story
router.get("/stories", getStories);

// mengedit story user
router.put("/stories/:storyId", verifyToken, editStory);

// Endpoint untuk menghapus story
router.delete("/stories/:id", verifyToken, deleteStory);

// Like story
router.post("/stories/:storyId/like", verifyToken, toggleLikeStory);

// menambahkan comment
router.post("/stories/:storyId/comments", verifyToken, addComment);

// mendapatkan seluruh comment dari story spesifik
router.get("/stories/:storyId/comments", getComments);

// mengedit comment
router.put("/stories/:storyId/comments/:commentId", verifyToken, editComment);

// menghapus comment 
router.delete("/stories/:storyId/comments/:commentId", verifyToken, deleteComment);

// upload
router.post('/upload/destinations', uploadDestinations);

router.post('/upload/hotels', uploadHotels);

router.post('/upload/hotel/:hotelId', upload.single('file'), uploadHotelPicture);

router.post('/upload/destination/:destinationId', upload.single('file'), uploadDestinationPicture);





module.exports = router;