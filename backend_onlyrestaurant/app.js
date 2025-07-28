
const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const cors = require('cors');

// Initialize the app
const app = express();

// Middleware
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cors());

// MongoDB connection string
const mongoUri = 'mongodb+srv://gangdevpooniya1115:Gp7073408522@restaurantdetails.8truj.mongodb.net/restaurant_list?retryWrites=true&w=majority&appName=restaurantdetails';

mongoose.connect(mongoUri)
  .then(() => {
    console.log('Connected to MongoDB Atlas');
  })
  .catch(err => {
    console.error('Error connecting to MongoDB:', err.message);
  });

// Import routes
const restaurantRoutes = require('./routes/restaurant');
const authRoutes = require('./routes/auth');
const orderRoutes = require('./routes/orders');
const menuRoutes = require('./routes/menu');

app.use('/restaurants', restaurantRoutes);
app.use('/restaurants', authRoutes);
app.use('/orders', orderRoutes);
app.use('/restaurants', menuRoutes);

// Start the server
const PORT = 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});