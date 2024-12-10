const tf = require('@tensorflow/tfjs-node');
const { getFirestore } = require('firebase-admin/firestore');
const db = getFirestore();

let destinationModel;
// Load model once during server startup
(async () => {
    try {
        destinationModel = await tf.loadLayersModel('https://storage.googleapis.com/travelmate-capstone-42b6f.firebasestorage.app/model/model.json');
        console.log("Model loaded successfully.");
    } catch (error) {
        console.error("Error loading model:", error);
    }
})();

const kabupatenToIdx = {
    'Kupang': 0,
    'Timor Tengah Selatan': 1,
    'Kota Kupang': 2,
    'Belu': 3,
    'Manggarai Barat': 4,
    'Manggarai': 5,
    'Rote Ndao': 6,
    'Ngada': 7,
    'Manggarai Timur': 8,
    'Sumba Barat Daya': 9,
    'Sumba Timur': 10,
    'Ende': 11,
    'Sumba Barat': 12,
    'Nagekeo': 13,
    'Sabu Raijua': 14
};

const jenisToIdx = {
    'Air Terjun': 0,
    'Danau': 1,
    'Taman': 2,
    'Tugu': 3,
    'Pantai': 4,
    'Sungai': 5,
    'Pulau': 6,
    'Gua': 7,
    'Desa Wisata': 8,
    'Taman Nasional': 9,
    'Batu Karang': 10,
    'Gunung': 11,
    'Bukit': 12,
    'Goa': 13,
    'Wisata Alam': 14
};

const jenisGroups = {
    perairan: ['Air Terjun', 'Danau', 'Sungai', 'Pantai', 'Pulau'],
    pegunungan: ['Gunung', 'Bukit'],
    taman: ['Taman', 'Taman Nasional'],
    berbatu: ['Batu Karang', 'Gua', 'Goa'],
    wisataAlam: ['Wisata Alam', 'Desa Wisata'],
    ikon: ['Tugu'],
};

// Fungsi untuk membersihkan dan mengonversi string harga menjadi angka
function cleanPrice(priceStr) {
    if (typeof priceStr === "string") {
        const cleaned = priceStr.replace(/Rp|\.|\s/g, "").trim(); // Menghapus "Rp", titik, dan spasi
        if (cleaned.includes("-")) {
            const [low, high] = cleaned.split("-").map(Number);
            return (low + high) / 2 || 0; // Ambil rata-rata jika rentang
        }
        return parseFloat(cleaned) || 0; // Konversi ke float
    }
    return parseFloat(priceStr) || 0; // Jika bukan string, langsung konversi
}

// Fungsi untuk mengategorikan harga destinasi
function categorizeDestinationPrice(price) {
    if (price === 0) return 0; // Gratis
    if (price <= 20000) return 1; // Terjangkau
    if (price <= 50000) return 2; // Menengah
    return 3; // Premium
}

// Fungsi normalisasi nilai
function normalize(value, min, max) {
    if (max - min === 0) return 0; // Avoid division by zero
    return (value - min) / (max - min);
}

/**
 * Recommend destinations based on user preferences.
 * @param {Object} preferences - User preferences including kabupaten, jenis, and price.
 * @returns {Array} - List of recommended destinations.
 */
async function recommendDestinations(preferences) {
    try {
        const { kabupaten, jenis, price } = preferences;

        // Fetch destinations from Firestore
        const snapshot = await db.collection("destinations").get();
        const destinations = snapshot.docs.map(doc => doc.data());

        if (destinations.length === 0) {
            throw new Error("No destinations found in the database.");
        }

        // Map kabupaten and jenis to indices
        // const kabupatenToIdx = {
        //     'Kupang': 0, 'Timor Tengah Selatan': 1, 'Kota Kupang': 2, 'Belu': 3,
        //     'Manggarai Barat': 4, 'Manggarai': 5, 'Rote Ndao': 6, 'Ngada': 7,
        //     'Manggarai Timur': 8, 'Sumba Barat Daya': 9, 'Sumba Timur': 10,
        //     'Ende': 11, 'Sumba Barat': 12, 'Nagekeo': 13, 'Sabu Raijua': 14
        // };

        // const jenisToIdx = {
        //     'Air Terjun': 0, 'Danau': 1, 'Taman': 2, 'Tugu': 3, 'Pantai': 4,
        //     'Sungai': 5, 'Pulau': 6, 'Gua': 7, 'Desa Wisata': 8, 'Taman Nasional': 9,
        //     'Batu Karang': 10, 'Gunung': 11, 'Bukit': 12, 'Goa': 13, 'Wisata Alam': 14
        // };

        const kabupatenIdx = kabupatenToIdx[kabupaten];
        const jenisIdx = jenisToIdx[jenis];

        if (kabupatenIdx === undefined || jenisIdx === undefined) {
            throw new Error("Invalid kabupaten or jenis in user preferences.");
        }

        // Normalize user input price
        const normalizedPrice = parseFloat(price);

        // Identify the category group for the selected jenis
        const jenisGroup = Object.values(jenisGroups).find(group => group.includes(jenis));
        if (!jenisGroup) {
            throw new Error(`Jenis "${jenis}" does not belong to any predefined group.`);
        }

        // Prepare tensors for model input
        const kabupatenTensor = tf.tensor2d(Array(destinations.length).fill(kabupatenIdx), [destinations.length, 1]);
        const jenisTensor = tf.tensor2d(Array(destinations.length).fill(jenisIdx), [destinations.length, 1]);
        const priceTensor = tf.tensor2d(Array(destinations.length).fill(normalizedPrice), [destinations.length, 1]);
        const ratingsTensor = tf.tensor2d(destinations.map(d => normalize(d.Rating || 0, 0, 5)), [destinations.length, 1]);
        const reviewsTensor = tf.tensor2d(destinations.map(d => normalize(d.jumlah_reviewer || 0, 0, 10000)), [destinations.length, 1]);

        // Predict scores using the model
        const predictions = destinationModel.predict([kabupatenTensor, jenisTensor, priceTensor, ratingsTensor, reviewsTensor]);
        let scores;
        if (Array.isArray(predictions)) {
            scores = await predictions[0].array();
            predictions[0].dispose();
        } else if (predictions instanceof tf.Tensor) {
            scores = await predictions.array();
            predictions.dispose();
        } else {
            throw new Error("Model did not return a valid tensor.");
        }

        // Attach scores to destinations
        const destinationsWithScores = destinations.map((destination, index) => ({
            ...destination,
            score: scores[index][0],
        }));

        // Filter and prioritize results based on preferences
        const filteredDestinations = destinationsWithScores
            .map(destination => {
                const destinationPrice = cleanPrice(destination.estimasi_harga_tiket);
                const destinationPriceCategory = categorizeDestinationPrice(destinationPrice);
                let matchCount = 0;

                // Check matches
                if (destination.kabupaten_kota === kabupaten) matchCount++;
                if (jenisGroup.includes(destination.jenis_objek)) matchCount++;
                if (destinationPriceCategory === categorizeDestinationPrice(price)) matchCount++;

                return { ...destination, matchCount };
            })
            .sort((a, b) => b.matchCount - a.matchCount || b.score - a.score) // Prioritize by matchCount, then score
            .slice(0, 10); // Limit to top 10

        return filteredDestinations;
    } catch (error) {
        console.error("Error in recommendDestinations:", error);
        throw { status: 500, message: error.message };
    }
}


/**
 * Function to recommend similar destinations.
 * @param {String} destinationId - ID of the destination clicked by the user.
 */

async function recommendSimilarDestinations(destinationId) {
    try {
        // Fetch source destination
        const sourceDoc = await db.collection('destinations').doc(destinationId).get();
        if (!sourceDoc.exists) throw new Error('Source destination not found');

        // Preprocess source destination
        const sourceRaw = sourceDoc.data();
        const sourcePrice = cleanPrice(sourceRaw.estimasi_harga_tiket);

        const source = {
            ...sourceRaw,
            kabupaten_encoded: kabupatenToIdx[sourceRaw.kabupaten_kota],
            jenis_encoded: jenisToIdx[sourceRaw.jenis_objek],
            price_normalized: categorizeDestinationPrice(sourcePrice) / 3,
        };

        if (
            source.kabupaten_encoded === undefined ||
            source.jenis_encoded === undefined ||
            source.price_normalized === undefined
        ) {
            throw new Error("Source destination has missing or invalid encoding.");
        }

        // Identify the category group for the source jenis_objek
        const jenisGroup = Object.values(jenisGroups).find(group => group.includes(source.jenis_objek));
        if (!jenisGroup) {
            throw new Error(`Jenis "${source.jenis_objek}" does not belong to any predefined group.`);
        }

        // Fetch all destinations
        const snapshot = await db.collection('destinations').get();
        const destinations = snapshot.docs.map(doc => ({
            ...doc.data(),
            id: doc.id,
        }));

        if (destinations.length === 0) {
            throw new Error("No destinations found in the database.");
        }

        // Preprocess all destinations
        const ratings = destinations.map(d => parseFloat(d.Rating?.replace(",", ".")) || 0);
        const reviews = destinations.map(d => parseFloat(d.jumlah_reviewer?.replace(",", "")) || 0);
        const prices = destinations.map(d => cleanPrice(d.estimasi_harga_tiket));

        const ratingMin = Math.min(...ratings);
        const ratingMax = Math.max(...ratings);
        const reviewMax = Math.max(...reviews);

        const processedDestinations = destinations.map(d => {
            const price = cleanPrice(d.estimasi_harga_tiket);
            return {
                ...d,
                kabupaten_encoded: kabupatenToIdx[d.kabupaten_kota],
                jenis_encoded: jenisToIdx[d.jenis_objek],
                price_normalized: categorizeDestinationPrice(price) / 3,
                rating_normalized: normalize(d.Rating || 0, ratingMin, ratingMax),
                reviews_normalized: normalize(d.jumlah_reviewer || 0, 0, reviewMax),
            };
        });

        processedDestinations.forEach(d => {
            if (
                d.kabupaten_encoded === undefined ||
                d.jenis_encoded === undefined ||
                d.price_normalized === undefined
            ) {
                throw new Error(`Destination ID ${d.id} has missing or invalid encoding.`);
            }
        });

        // Prepare tensors for model input
        const kabupatenTensor = tf.tensor2d(processedDestinations.map(d => d.kabupaten_encoded), [destinations.length, 1]);
        const jenisTensor = tf.tensor2d(processedDestinations.map(d => d.jenis_encoded), [destinations.length, 1]);
        const priceTensor = tf.tensor2d(processedDestinations.map(d => d.price_normalized), [destinations.length, 1]);
        const ratingTensor = tf.tensor2d(processedDestinations.map(d => d.rating_normalized), [destinations.length, 1]);
        const reviewsTensor = tf.tensor2d(processedDestinations.map(d => d.reviews_normalized), [destinations.length, 1]);

        // Predict similarities
        const predictions = destinationModel.predict([
            tf.tensor2d([source.kabupaten_encoded], [1, 1]),
            tf.tensor2d([source.jenis_encoded], [1, 1]),
            tf.tensor2d([source.price_normalized], [1, 1]),
            ratingTensor,
            reviewsTensor,
        ]);

        let scores;
        if (Array.isArray(predictions)) {
            scores = await predictions[0].array();
            predictions[0].dispose();
        } else if (predictions instanceof tf.Tensor) {
            scores = await predictions.array();
            predictions.dispose();
        } else {
            throw new Error("Model did not return a valid tensor or array.");
        }

        // Attach scores and perform filtering
        const destinationsWithScores = processedDestinations.map((destination, index) => ({
            ...destination,
            score: scores[index],
        }));

        const filteredDestinations = destinationsWithScores
            .filter(d => d.id !== destinationId)
            .map(destination => {
                // Calculate matching attributes count
                let matchCount = 0;
                if (destination.kabupaten_kota === source.kabupaten_kota) matchCount++;
                if (jenisGroup.includes(destination.jenis_objek)) matchCount++;
                if (categorizeDestinationPrice(cleanPrice(destination.estimasi_harga_tiket)) === categorizeDestinationPrice(sourcePrice)) {
                    matchCount++;
                }
                return { ...destination, matchCount };
            })
            .sort((a, b) => b.matchCount - a.matchCount || b.score - a.score) // Sort by matchCount, then score
            .slice(0, 76);

        if (filteredDestinations.length === 0) {
            throw new Error("No similar destinations found.");
        }

        return filteredDestinations;
    } catch (error) {
        console.error("Error in recommendSimilarDestinations:", error);
        throw { status: 500, message: error.message };
    }
}

/**
 * Function to recommend hotels based on a destination.
 * @param {String} destinationId - ID of the destination.
 */
async function recommendHotels(destinationId) {
    try {
        // Fetch destination details
        const destinationDoc = await db.collection('destinations').doc(destinationId).get();
        if (!destinationDoc.exists) throw new Error('Destination not found');
        const destination = destinationDoc.data();

        // Normalize kabupaten_kota to handle "Kupang" and "Kota Kupang"
        const destinationKabupaten = destination.kabupaten_kota === "Kupang" ? "Kota Kupang" : destination.kabupaten_kota;
        
        // Fetch all hotels
        const snapshot = await db.collection('hotel').get();
        const hotels = snapshot.docs.map(doc => ({
            ...doc.data(),
            id: doc.id,
        }));

        if (hotels.length === 0) {
            throw new Error('No hotels found in the database.');
        }

        // Filter hotels based on kabupaten_kota
        const filteredHotels = hotels.filter(hotel => {
            const hotelKabupaten = hotel.kabupaten_kota === "Kupang" ? "Kota Kupang" : hotel.kabupaten_kota;
            return hotelKabupaten === destinationKabupaten;
        });

        if (filteredHotels.length === 0) {
            throw new Error(`No matching hotels found for kabupaten_kota: ${destinationKabupaten}.`);
        }

        // Sort hotels by rating_hotel (descending) and jumlah_reviewer (descending)
        const sortedHotels = filteredHotels.sort((a, b) => {
            const ratingA = parseFloat(a.rating_hotel?.replace(",", ".") || "0");
            const ratingB = parseFloat(b.rating_hotel?.replace(",", ".") || "0");
            const reviewerA = parseInt(a.jumlah_reviewer || "0", 10);
            const reviewerB = parseInt(b.jumlah_reviewer || "0", 10);

            // Sort by rating_hotel first, then by jumlah_reviewer
            if (ratingB !== ratingA) return ratingB - ratingA;
            return reviewerB - reviewerA;
        });

        // Return top 5 hotels
        return sortedHotels.slice(0, 417);
    } catch (error) {
        console.error('Error in recommendHotels:', error);
        throw { status: 500, message: error.message };
    }
}

module.exports = { recommendDestinations, recommendSimilarDestinations, recommendHotels };