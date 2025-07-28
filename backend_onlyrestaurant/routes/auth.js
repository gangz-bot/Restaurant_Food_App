const express = require('express');
const router = express.Router();
const Restaurant = require('../models/Restaurant');
const bcrypt = require('bcrypt');
const saltRounds = 10;

// Register (restaurant)
router.post('/register', async (req, res) => {
  const { email, password, name } = req.body;

  console.log('Registration attempt:', { email, name });

  // Validation
  if (!email || !password || !name) {
    return res.status(400).json({
      success: false,
      message: 'All fields are required: email, password, name'
    });
  }

  try {
    // Check if restaurant already exists
    const existing = await Restaurant.findOne({ email });
    if (existing) {
      return res.status(400).json({
        success: false,
        message: 'Restaurant already exists'
      });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, saltRounds);

    // Create new restaurant
    const restaurant = new Restaurant({
      email,
      password: hashedPassword,
      name
    });

    // Save to database
    await restaurant.save();

    // Successful response with restaurant ID
    res.status(201).json({
      success: true,
      message: 'Restaurant registered successfully',
      _id: restaurant._id,
      email: restaurant.email,
      name: restaurant.name
    });

  } catch (err) {
    console.error('Registration error:', err);
    res.status(500).json({
      success: false,
      message: 'Registration failed',
      error: err.message
    });
  }
});

// Login (restaurant)
router.post('/login', async (req, res) => {
  const { email, password } = req.body;

  try {
    const restaurant = await Restaurant.findOne({ email });
    if (!restaurant) {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials'
      });
    }

    // Compare hashed password
    const match = await bcrypt.compare(password, restaurant.password);
    if (!match) {
      return res.status(401).json({
        success: false,
        message: 'Invalid credentials'
      });
    }

    // Successful login
    res.status(200).json({
      success: true,
      message: 'Login successful',
      _id: restaurant._id,
      email: restaurant.email,
      name: restaurant.name
    });

  } catch (err) {
    console.error('Login error:', err);
    res.status(500).json({
      success: false,
      message: 'Login failed',
      error: err.message
    });
  }
});

module.exports = router;