const express = require('express');
const router = express.Router();
const Restaurant = require('../models/Restaurant');

// POST /restaurants/info - Save restaurant address and timings
// Add this endpoint to return restaurant ID after saving info
router.post('/info', async (req, res) => {
  const { email, address, openTime, closeTime } = req.body;

  if (!email || !address || !openTime || !closeTime) {
      return res.status(400).json({
          success: false,
          message: 'All fields are required'
      });
  }

  try {
      const restaurant = await Restaurant.findOne({ email });
      if (!restaurant) {
          return res.status(404).json({
              success: false,
              message: 'Restaurant not found'
          });
      }

      restaurant.address = address;
      restaurant.openTime = openTime;
      restaurant.closeTime = closeTime;
      await restaurant.save();

      res.status(200).json({
          success: true,
          message: 'Restaurant info saved',
          _id: restaurant._id,  // Include restaurant ID in response
          address: restaurant.address,
          openTime: restaurant.openTime,
          closeTime: restaurant.closeTime
      });
  } catch (err) {
      res.status(500).json({
          success: false,
          message: 'Failed to save info',
          error: err.message
      });
  }
});


module.exports = router;