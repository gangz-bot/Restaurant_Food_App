const express = require('express');
const router = express.Router();
const Restaurant = require('../models/Restaurant');

// Get all menu items for a restaurant
router.get('/:restaurantId', async (req, res) => {
  try {
    const restaurant = await Restaurant.findById(req.params.restaurantId)
      .select('menu -_id');
      
    if (!restaurant) {
      return res.status(404).json({ 
        success: false,
        message: 'Restaurant not found' 
      });
    }

    res.status(200).json({
      success: true,
      data: restaurant.menu
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      message: 'Failed to fetch menu',
      error: err.message
    });
  }
});


// Add menu item
router.post('/:restaurantId/menu', async (req, res) => {
    const { dishName, price, imageUrl } = req.body;
    const restaurantId = req.params.restaurantId;

    console.log('Adding menu item for restaurant:', restaurantId);

    if (!dishName || !price || !imageUrl) {
        return res.status(400).json({
            success: false,
            message: 'Dish name, price and image URL are required'
        });
    }

    try {
        const restaurant = await Restaurant.findById(restaurantId);
        if (!restaurant) {
            return res.status(404).json({
                success: false,
                message: 'Restaurant not found'
            });
        }

        const newItem = {
            dishName,
            price: parseFloat(price),
            imageUrl
        };

        restaurant.menu.push(newItem);
        await restaurant.save();

        const addedItem = restaurant.menu[restaurant.menu.length - 1];

        res.status(201).json({
            success: true,
            message: 'Menu item added successfully',
            data: addedItem
        });

    } catch (err) {
        console.error('Error adding menu item:', err);
        res.status(500).json({
            success: false,
            message: 'Failed to add menu item',
            error: err.message
        });
    }
});
// Add this endpoint to get menu items
router.get('/:restaurantId/menu', async (req, res) => {
    try {
        const restaurant = await Restaurant.findById(req.params.restaurantId)
            .select('menu -_id');
            
        if (!restaurant) {
            return res.status(404).json({
                success: false,
                message: 'Restaurant not found'
            });
        }

        res.status(200).json(restaurant.menu); // Directly return the menu array
    } catch (err) {
        console.error('Error fetching menu:', err);
        res.status(500).json({
            success: false,
            message: 'Failed to fetch menu',
            error: err.message
        });
    }
});



// Update menu item availability
router.patch('/:restaurantId/items/:itemId', async (req, res) => {
  try {
    const { isAvailable } = req.body;
    
    const restaurant = await Restaurant.findOneAndUpdate(
      { 
        _id: req.params.restaurantId,
        'menu._id': req.params.itemId 
      },
      { 
        $set: { 
          'menu.$.isAvailable': isAvailable 
        } 
      },
      { new: true }
    );

    if (!restaurant) {
      return res.status(404).json({
        success: false,
        message: 'Restaurant or menu item not found'
      });
    }

    res.status(200).json({
      success: true,
      message: 'Menu item updated successfully'
    });
  } catch (err) {
    res.status(500).json({
      success: false,
      message: 'Failed to update menu item',
      error: err.message
    });
  }
});

module.exports = router;