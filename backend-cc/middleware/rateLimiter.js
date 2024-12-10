const rateLimit = require('express-rate-limit');

const apiLimiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1 jam
  max: 200, // Maksimal 200 permintaan per 1 jam
  message: { message: 'Too many requests, please try again later.' },
});

module.exports = { apiLimiter };