const { saveDestinations, saveHotels } = require('../services/firebaseService');
const { admin, db } = require('../config/firebase');

const bucket = admin.storage().bucket();

async function uploadDestinations(req, res) {
    try {
        const data = req.body; // Ambil data JSON langsung dari body request
        if (!Array.isArray(data)) {
            return res.status(400).json({ error: 'Invalid data format. Expected an array of destinations.' });
        }
        const result = await saveDestinations(data);
        res.status(200).json(result);
    } catch (error) {
        console.error('Error in uploadDestinations:', error);
        res.status(500).json({ error: error.message });
    }
}

async function uploadHotels(req, res) {
    try {
        const data = req.body; // Ambil data JSON langsung dari body request
        if (!Array.isArray(data)) {
            return res.status(400).json({ error: 'Invalid data format. Expected an array of hotels.' });
        }
        const result = await saveHotels(data);
        res.status(200).json(result);
    } catch (error) {
        console.error('Error in uploadHotels:', error);
        res.status(500).json({ error: error.message });
    }
}

/**
 * Upload gambar untuk destinasi.
 * @route POST /upload/destination/:destinationId
 */
async function uploadDestinationPicture(req, res) {
    try {
        const { destinationId } = req.params;
        const bucket = admin.storage().bucket();
        // Validasi input
        if (!destinationId) {
            return res.status(400).json({ error: "Missing destinationId parameter." });
        }

        const file = req.file;
        if (!file) {
            return res.status(400).json({ error: "No file uploaded." });
        }

        // Fetch destinasi dari Firestore
        const destinationDoc = await db.collection('destinations').doc(destinationId).get();
        if (!destinationDoc.exists) {
            return res.status(404).json({ error: "Destination not found." });
        }

        const destination = destinationDoc.data();
        const fileName = `destination-picture/${destination.nama_objek.replace(/ /g, "_")}_${Date.now()}.${file.originalname.split('.').pop()}`;

        // Upload file ke GCS
        const blob = bucket.file(fileName);
        const blobStream = blob.createWriteStream({
            resumable: false,
            metadata: { contentType: file.mimetype },
        });

        blobStream.on('error', (err) => {
            console.error(err);
            return res.status(500).json({ error: "Failed to upload file." });
        });

        blobStream.on('finish', async () => {
            const publicUrl = `https://storage.googleapis.com/${bucket.name}/${blob.name}`;

            // Tambahkan URL ke Firestore
            await db.collection('destinations').doc(destinationId).update({ picture_url: publicUrl });

            res.status(200).json({ message: "File uploaded successfully.", picture_url: publicUrl });
        });

        blobStream.end(file.buffer);
    } catch (error) {
        console.error("Error in uploadDestinationPicture:", error);
        res.status(500).json({ error: error.message });
    }
}

/**
 * Upload gambar untuk hotel.
 * @route POST /upload/hotel/:hotelId
 */
async function uploadHotelPicture(req, res) {
    try {
        const { hotelId } = req.params;

        // Validasi input
        if (!hotelId) {
            return res.status(400).json({ error: "Missing hotelId parameter." });
        }

        const file = req.file;
        if (!file) {
            return res.status(400).json({ error: "No file uploaded." });
        }

        // Fetch hotel dari Firestore
        const hotelDoc = await db.collection('hotel').doc(hotelId).get();
        if (!hotelDoc.exists) {
            return res.status(404).json({ error: "Hotel not found." });
        }

        const hotel = hotelDoc.data();
        const fileName = `hotel-picture/${hotel.nama_hotel.replace(/ /g, "_")}_${Date.now()}.${file.originalname.split('.').pop()}`;

        // Upload file ke GCS
        const blob = bucket.file(fileName);
        const blobStream = blob.createWriteStream({
            resumable: false,
            metadata: { contentType: file.mimetype },
        });

        blobStream.on('error', (err) => {
            console.error(err);
            return res.status(500).json({ error: "Failed to upload file." });
        });

        blobStream.on('finish', async () => {
            const publicUrl = `https://storage.googleapis.com/${bucket.name}/${blob.name}`;

            // Tambahkan URL ke Firestore
            await db.collection('hotel').doc(hotelId).update({ picture_url: publicUrl });

            res.status(200).json({ message: "File uploaded successfully.", picture_url: publicUrl });
        });

        blobStream.end(file.buffer);
    } catch (error) {
        console.error("Error in uploadHotelPicture:", error);
        res.status(500).json({ error: error.message });
    }
}


module.exports = { uploadDestinations, uploadHotels, uploadDestinationPicture, uploadHotelPicture };
