const { db, admin } = require("../config/firebase");
const { body, validationResult } = require("express-validator");
const createError = require("http-errors");
const multer = require("multer");
const { v4: uuidv4 } = require("uuid");

// Konfigurasi Multer untuk menangani unggahan file
const storage = multer.memoryStorage();
const upload = multer({ storage });

// GET /stories
exports.getStories = async (req, res, next) => {
  try {
    const snapshot = await db
      .collection("stories")
      .orderBy("createdAt", "desc")
      .get();

    const stories = [];

    for (const doc of snapshot.docs) {
      const { likedBy = [], mediaPaths, ...storyData } = doc.data(); // Menghapus atribut mediaPaths
      const likedByUsernames = await Promise.all(
        likedBy.map(async (userId) => {
          const userDoc = await db.collection("users").doc(userId).get();
          return userDoc.exists ? userDoc.data().username : null; // Ambil username
        })
      );

      stories.push({
        id: doc.id,
        ...storyData,
        likedBy: likedByUsernames.filter((username) => username !== null), // Filter jika username tidak ditemukan
      });
    }

    res.status(200).json(stories);
  } catch (error) {
    next(createError(500, "Error fetching stories", { error: error.message }));
  }
};

// POST /stories - Menambahkan story baru
exports.addStory = [
  upload.array("media"), // Middleware untuk file unggahan
  body("content").notEmpty().withMessage("Content is required"),
  body("rating")
    .isFloat({ min: 0, max: 5 })
    .withMessage("Rating must be between 0 and 5"),
  async (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return next(
        createError(400, "Validation failed", { details: errors.array() })
      );
    }

    const userId = req.user.id; // Ambil userId dari token
    const { content, rating } = req.body;
    const files = req.files;

    if (!files || files.length === 0) {
      return next(createError(400, "At least one media file is required"));
    }

    try {
      const bucket = admin.storage().bucket();
      const mediaPaths = [];
      const mediaURLs = [];

      for (const file of files) {
        const filePath = `story-media/${uuidv4()}-${Date.now()}.${
          file.mimetype.split("/")[1]
        }`;
        const blob = bucket.file(filePath);
        const blobStream = blob.createWriteStream({
          metadata: { contentType: file.mimetype },
        });

        await new Promise((resolve, reject) => {
          blobStream.on("error", reject);
          blobStream.on("finish", () => {
            mediaPaths.push(filePath);
            const mediaURL = `https://storage.googleapis.com/${bucket.name}/${filePath}`;
            mediaURLs.push(mediaURL);
            resolve();
          });
          blobStream.end(file.buffer);
        });
      }

      const storyId = uuidv4();

      await db.collection("stories").doc(storyId).set({
        id: storyId,
        userId,
        content,
        rating: parseFloat(rating),
        media: mediaURLs,
        mediaPaths,
        likes: 0,
        createdAt: new Date(),
      });

      res
        .status(201)
        .json({ message: "Story added successfully", storyId, mediaURLs });
    } catch (error) {
      next(createError(500, "Error adding story", { error: error.message }));
    }
  },
];

// PUT /stories/:storyId - Mengedit story
exports.editStory = async (req, res, next) => {
  const { storyId } = req.params;
  const { content, rating } = req.body;

  try {
    const userId = req.user.id; // Ambil userId dari token
    const storyRef = db.collection("stories").doc(storyId);
    const storyDoc = await storyRef.get();

    if (!storyDoc.exists) {
      return res.status(404).json({ error: "Story not found" });
    }

    const storyData = storyDoc.data();

    if (storyData.userId !== userId) {
      return res
        .status(403)
        .json({ error: "You can only edit your own story" });
    }

    const updatedData = {
      content: content || storyData.content,
      rating: rating ? parseFloat(rating) : storyData.rating,
      updatedAt: new Date(),
    };

    await storyRef.update(updatedData);

    res
      .status(200)
      .json({ message: "Story updated successfully", updatedData });
  } catch (error) {
    next(createError(500, "Error editing story", { error: error.message }));
  }
};

// DELETE /stories/:id
exports.deleteStory = async (req, res, next) => {
  const { storyId } = req.params;

  try {
    const userId = req.user.id; // Ambil userId dari token
    const storyRef = db.collection("stories").doc(storyId);
    const storyDoc = await storyRef.get();

    if (!storyDoc.exists) {
      return next(createError(404, "Story not found"));
    }

    const storyData = storyDoc.data();

    if (storyData.userId !== userId) {
      return res
        .status(403)
        .json({ error: "You can only delete your own story" });
    }

    const mediaPaths = storyData.mediaPaths || [];
    const bucket = admin.storage().bucket();

    await Promise.all(
      mediaPaths.map(async (filePath) => {
        try {
          const file = bucket.file(filePath);
          await file.delete();
          console.log(`Deleted file from Cloud Storage: ${filePath}`);
        } catch (error) {
          console.error(`Failed to delete file: ${filePath}`, error.message);
        }
      })
    );

    await storyRef.delete();

    res.status(200).json({ message: "Story deleted successfully" });
  } catch (error) {
    console.error("Error in deleteStory:", error);
    next(createError(500, "Error deleting story", { error: error.message }));
  }
};

// POST /stories/:storyId/like - Menambahkan atau menghapus like pada story
exports.toggleLikeStory = async (req, res, next) => {
  const { storyId } = req.params;

  try {
    const userId = req.user.id; // Ambil userId dari token
    const storyRef = db.collection("stories").doc(storyId);
    const storyDoc = await storyRef.get();

    if (!storyDoc.exists) {
      return res.status(404).json({ error: "Story not found" });
    }

    const storyData = storyDoc.data();
    const likedBy = storyData.likedBy || [];

    if (likedBy.includes(userId)) {
      await storyRef.update({
        likes: admin.firestore.FieldValue.increment(-1),
        likedBy: admin.firestore.FieldValue.arrayRemove(userId),
      });

      return res.status(200).json({ message: "Like removed successfully" });
    } else {
      await storyRef.update({
        likes: admin.firestore.FieldValue.increment(1),
        likedBy: admin.firestore.FieldValue.arrayUnion(userId),
      });

      return res.status(200).json({ message: "Story liked successfully" });
    }
  } catch (error) {
    next(createError(500, "Error toggling like", { error: error.message }));
  }
};

// POST /stories/:storyId/comments - Menambahkan komentar pada story
exports.addComment = async (req, res, next) => {
  const { storyId } = req.params;
  const { content } = req.body;
  const userId = req.user.id; // Ambil userId dari token

  if (!content) {
    return next(createError(400, "Comment content is required"));
  }

  try {
    const storyRef = db.collection("stories").doc(storyId);
    const storyDoc = await storyRef.get();

    if (!storyDoc.exists) {
      return res.status(404).json({ error: "Story not found" });
    }

    const commentId = uuidv4();

    // Menambahkan komentar ke subkoleksi "comments"
    const commentRef = storyRef.collection("comments").doc(commentId);
    await commentRef.set({
      id: commentId,
      userId,
      content,
      createdAt: new Date(),
    });

    res.status(201).json({ message: "Comment added successfully", commentId });
  } catch (error) {
    next(createError(500, "Error adding comment", { error: error.message }));
  }
};

// GET /stories/:storyId/comments - Mendapatkan semua komentar pada story
exports.getComments = async (req, res, next) => {
  const { storyId } = req.params;

  try {
    const storyRef = db.collection("stories").doc(storyId);
    const storyDoc = await storyRef.get();

    if (!storyDoc.exists) {
      return res.status(404).json({ error: "Story not found" });
    }

    // Ambil semua dokumen di subkoleksi "comments"
    const commentsSnapshot = await storyRef
      .collection("comments")
      .orderBy("createdAt", "asc")
      .get();

    const comments = commentsSnapshot.docs.map((doc) => ({
      id: doc.id,
      ...doc.data(),
    }));

    res.status(200).json(comments);
  } catch (error) {
    next(createError(500, "Error fetching comments", { error: error.message }));
  }
};

// PUT /stories/:storyId/comments/:commentId - Mengedit komentar
exports.editComment = async (req, res, next) => {
  const { storyId, commentId } = req.params;
  const { content } = req.body;
  const userId = req.user.id; // Ambil userId dari token

  if (!content) {
    return next(createError(400, "Comment content is required"));
  }

  try {
    const commentRef = db
      .collection("stories")
      .doc(storyId)
      .collection("comments")
      .doc(commentId);
    const commentDoc = await commentRef.get();

    if (!commentDoc.exists) {
      return res.status(404).json({ error: "Comment not found" });
    }

    const commentData = commentDoc.data();

    if (commentData.userId !== userId) {
      return res
        .status(403)
        .json({ error: "You can only edit your own comment" });
    }

    await commentRef.update({
      content,
      updatedAt: new Date(),
    });

    res.status(200).json({ message: "Comment updated successfully" });
  } catch (error) {
    next(createError(500, "Error editing comment", { error: error.message }));
  }
};

// DELETE /stories/:storyId/comments/:commentId - Menghapus komentar
exports.deleteComment = async (req, res, next) => {
  const { storyId, commentId } = req.params;
  const userId = req.user.id; // Ambil userId dari token

  try {
    const commentRef = db
      .collection("stories")
      .doc(storyId)
      .collection("comments")
      .doc(commentId);
    const commentDoc = await commentRef.get();

    if (!commentDoc.exists) {
      return res.status(404).json({ error: "Comment not found" });
    }

    const commentData = commentDoc.data();

    if (commentData.userId !== userId) {
      return res
        .status(403)
        .json({ error: "You can only delete your own comment" });
    }

    await commentRef.delete();

    res.status(200).json({ message: "Comment deleted successfully" });
  } catch (error) {
    next(createError(500, "Error deleting comment", { error: error.message }));
  }
};