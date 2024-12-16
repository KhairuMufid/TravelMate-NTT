const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://travelmate-capstone-42b6f.firebaseio.com",
  storageBucket: "travelmate-capstone-42b6f.firebasestorage.app"
});

const db = admin.firestore(); // Inisialisasi Firestore

// Ekspor sebagai objek eksplisit
module.exports = {
  admin,
  db,
};