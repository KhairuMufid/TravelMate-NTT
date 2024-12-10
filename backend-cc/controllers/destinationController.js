const { db } = require('../config/firebase');
const { recommendDestinations, recommendSimilarDestinations } = require('../services/mlService');

// GET /destinations
/**
 * Controller to handle destination recommendations based on user preferences.
 * @param {Request} req
 * @param {Response} res
 */
async function getAllDestinations(req, res) {
    try {
        const { preferences } = req.body;

        // Validate input
        if (!preferences || preferences.length !== 3) {
            return res.status(400).json({
                error: "Invalid input: preferences should include kabupaten, jenis, and price.",
            });
        }

        const [kabupaten, jenis, price] = preferences;

        // Call the recommendation service
        const recommendations = await recommendDestinations({ kabupaten, jenis, price });
        return res.status(200).json({ recommendations });
    } catch (error) {
        console.error("Error in getAllDestinations:", error);
        res.status(error.status || 500).json({ error: error.message });
    }
}

async function getSimilarDestinations(req, res) {
    try {
        const { destinationId } = req.params;

        // Validate input
        if (!destinationId) {
            return res.status(400).json({ error: "Missing destinationId parameter." });
        }

        // Call the recommendation function
        const recommendations = await recommendSimilarDestinations(destinationId);

        // Return the recommendations
        return res.status(200).json({ recommendations });
    } catch (error) {
        console.error("Error in getSimilarDestinations:", error);
        res.status(error.status || 500).json({ error: error.message });
    }
}

/**
 * Get all destinations by jenis_objek.
 * @param {Object} req - Express request object.
 * @param {Object} res - Express response object.
 */
async function getDestinationsByType(req, res) {
    try {
        const { jenis } = req.params;

        if (!jenis) {
            return res.status(400).json({ error: "Missing jenis parameter." });
        }

        // Fetch destinations from Firestore with matching jenis_objek
        const snapshot = await db.collection('destinations')
            .where('jenis_objek', '==', jenis)
            .get();

        if (snapshot.empty) {
            return res.status(404).json({ error: `No destinations found for jenis_objek: ${jenis}` });
        }

        // Map Firestore documents to response format
        const destinations = snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data(),
        }));

        res.status(200).json({ destinations });
    } catch (error) {
        console.error("Error in getDestinationsByType:", error);
        res.status(500).json({ error: error.message });
    }
}

// GET /destinations/:destinationId/maps-link - Mendapatkan link Google Maps untuk destinasi
async function getDestinationMapsLink (req, res, next) {
    const { destinationId } = req.params;
  
    try {
      // Ambil data destinasi berdasarkan ID
      const destinationRef = db.collection("destinations").doc(destinationId);
      const destinationDoc = await destinationRef.get();
  
      if (!destinationDoc.exists) {
        return res.status(404).json({ error: "Destination not found" });
      }
  
      const { nama_objek } = destinationDoc.data();
  
      // Encode nama_objek untuk membuat URL Google Maps
      const encodedDestinationName = encodeURIComponent(nama_objek);
      const mapsLink = `https://www.google.com/maps/search/?api=1&query=${encodedDestinationName};`
  
      res.status(200).json({ mapsLink });
    } catch (error) {
      next(createError(500, "Error generating Google Maps link", { error: error.message }));
    }
  };

module.exports = { getAllDestinations, getSimilarDestinations, getDestinationsByType, getDestinationMapsLink };