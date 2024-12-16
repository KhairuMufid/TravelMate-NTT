const { db } = require('../config/firebase');
const { body, validationResult } = require('express-validator');
const createError = require('http-errors');
const { v4: uuidv4 } = require('uuid');

/**
 * POST /assessment
 * Save user assessment data to Firestore.
 * @param {Request} req
 * @param {Response} res
 */
async function submitAssessment(req, res) {
    try {
        const userId = req.user.id; // Ambil userId dari token
        const { preferences } = req.body;

        if (!preferences || preferences.length !== 3) {
            return res.status(400).json({
                error: "Invalid input: preferences (kabupaten, jenis, price) are required.",
            });
        }

        const [kabupaten, jenis, price] = preferences;

        // Save to Firestore
        const assessmentData = { userId, kabupaten, jenis, price, createdAt: new Date() };
        await db.collection("user_assessments").doc(userId).set(assessmentData);

        res.status(200).json({ message: "Assessment saved successfully" });
    } catch (error) {
        console.error("Error in submitAssessment:", error);
        res.status(500).json({ error: "Failed to save assessment", details: error.message });
    }
}

module.exports = { submitAssessment }