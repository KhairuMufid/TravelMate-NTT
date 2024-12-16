const { db } = require('../config/firebase');
const { param, body, validationResult } = require('express-validator');
const createError = require('http-errors');
const { v4: uuidv4 } = require('uuid');

/**
 * GET /wishlist
 * Get the wishlist of the authenticated user.
 * @param {Request} req
 * @param {Response} res
 */
exports.getWishlist = async (req, res, next) => {
  try {
    const userId = req.user.id; // Ambil userId dari token

    // Ambil data wishlist dari Firestore
    const snapshot = await db.collection('wishlists').where('userId', '==', userId).get();
    if (snapshot.empty) {
      return res.status(404).json({ message: 'No wishlist found for the user.' });
    }

    const wishlist = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));

    // Ambil detail destinasi berdasarkan destinationId
    const destinationDetails = await Promise.all(
      wishlist.map(async (item) => {
        const destinationRef = db.collection('destinations').doc(item.destinationId);
        const destinationDoc = await destinationRef.get();

        if (destinationDoc.exists) {
          return { ...item, destination: destinationDoc.data() };
        } else {
          console.warn(`Destination with ID ${item.destinationId} not found`);
          return { ...item, destination: null };
        }
      })
    );

    res.status(200).json(destinationDetails);
  } catch (error) {
    console.error('Error in getWishlist:', error);
    next(createError(500, 'Error fetching wishlist', { error: error.message }));
  }
};


// POST /wishlist
exports.addWishlist = [
  body('nama_objek').notEmpty().withMessage('Nama objek is required'),
  async (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return next(createError(400, 'Validation failed', { details: errors.array() }));
    }

    try {
      const userId = req.user.id; // Ambil userId dari token
      const { nama_objek } = req.body;

      // Cari destinationId berdasarkan nama_objek di koleksi destinations
      const destinationSnapshot = await db
        .collection('destinations')
        .where('nama_objek', '==', nama_objek)
        .get();

      if (destinationSnapshot.empty) {
        return next(createError(404, 'Destination not found'));
      }

      // Ambil destinationId dari dokumen yang ditemukan
      const destinationDoc = destinationSnapshot.docs[0]; // Ambil dokumen pertama yang cocok
      const destinationId = destinationDoc.id; // ID dokumen Firestore

      const wishlistId = uuidv4();
      await db.collection('wishlists').doc(wishlistId).set({
        id: wishlistId,
        userId,
        destinationId,
        addedAt: new Date(),
      });

      res.status(201).json({ message: 'Wishlist added successfully', wishlistId });
    } catch (error) {
      next(createError(500, 'Error adding to wishlist', { error: error.message }));
    }
  },
];



// DELETE /wishlist/:id
exports.removeWishlist = async (req, res, next) => {
  const { wishlistId } = req.params;

  try {
    // Ambil userId dari token
    const userId = req.user.id;

    // Ambil wishlist dari Firestore
    const wishlistRef = db.collection('wishlists').doc(wishlistId);
    const wishlistDoc = await wishlistRef.get();

    if (!wishlistDoc.exists) {
      return res.status(404).json({ error: 'Wishlist not found' });
    }

    const wishlistData = wishlistDoc.data();

    // Periksa apakah wishlist ini milik user yang sedang login
    if (wishlistData.userId !== userId) {
      return res.status(403).json({ error: 'You can only delete your own wishlist' });
    }

    // Hapus wishlist
    await wishlistRef.delete();

    res.status(200).json({ message: 'Wishlist deleted successfully' });
  } catch (error) {
    next(createError(500, 'Error deleting wishlist', { error: error.message }));
  }
};