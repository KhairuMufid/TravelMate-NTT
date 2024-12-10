const { db } = require('../config/firebase');
const { recommendHotels } = require('../services/mlService');

async function getHotels(req, res) {
    try {
        // Get destinationId from request parameters
        const { destinationId } = req.params;

        // Validate input
        if (!destinationId) {
            return res.status(400).json({ error: "Missing destinationId parameter." });
        }

        // Fetch recommended hotels
        const hotels = await recommendHotels(destinationId);
        return res.status(200).json({ hotels });
    } catch (error) {
        console.error('Error in getHotels:', error);
        res.status(error.status || 500).json({ error: error.message });
    }
}

async function getHotelMapsLink (req, res, next) {
    const { hotelId } = req.params;
  
    try {
      // Ambil data hotel berdasarkan ID
      const hotelRef = db.collection("hotel").doc(hotelId);
      const hotelDoc = await hotelRef.get();
  
      if (!hotelDoc.exists) {
        return res.status(404).json({ error: "Hotel not found" });
      }
  
      const { nama_hotel } = hotelDoc.data();
  
      // Encode nama hotel untuk membuat URL Google Maps
      const encodedHotelName = encodeURIComponent(nama_hotel);
      const mapsLink = `https://www.google.com/maps/search/?api=1&query=${encodedHotelName};`
  
      res.status(200).json({ mapsLink });
    } catch (error) {
      next(createError(500, "Error generating Google Maps link", { error: error.message }));
    }
  };
  

module.exports = { getHotels, getHotelMapsLink };