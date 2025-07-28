const mongoose = require('mongoose');

const menuItemSchema = new mongoose.Schema({
  dishName: { 
    type: String, 
    required: true,
    trim: true
  },
  price: { 
    type: Number, 
    required: true,
    min: 0
  },
  imageUrl: { 
    type: String, 
    required: true
  },
  category: {
    type: String,
    enum: ['appetizer', 'main', 'dessert', 'beverage'],
    default: 'main'
  },
  isAvailable: {
    type: Boolean,
    default: true
  },
  description: String
}, { timestamps: true });

const restaurantSchema = new mongoose.Schema({
  name: { type: String, required: true },
  email: { 
    type: String, 
    required: true, 
    unique: true,
    match: [/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/, 'Please fill a valid email address']
  },
  password: { type: String, required: true },
  address: { type: String },
  openTime: { type: String },
  closeTime: { type: String },
  menu: [menuItemSchema]
}, {
  timestamps: true
});

module.exports = mongoose.model('Restaurant', restaurantSchema);