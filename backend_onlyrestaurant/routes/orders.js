const express = require('express');
const router = express.Router();
const Order = require('../models/Order');
const twilio = require('twilio');

// Twilio setup
const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const client = new twilio(accountSid, authToken);

// GET /orders - Fetch all orders
router.get('/', async (req, res) => {
  try {
    const orders = await Order.find().sort({ createdAt: -1 });
    res.status(200).json(orders);
  } catch (err) {
    res.status(500).json({ message: 'Failed to fetch orders', error: err.message });
  }
});

// GET /orders/:id - Fetch order by ID
router.get('/:id', async (req, res) => {
  try {
    const order = await Order.findById(req.params.id);
    if (!order) return res.status(404).json({ message: 'Order not found' });
    res.status(200).json(order);
  } catch (err) {
    res.status(500).json({ message: 'Error fetching order', error: err.message });
  }
});

// POST /orders - Create a new order
router.post('/', async (req, res) => {
  const { customerName, phoneNumber, address, items, totalAmount } = req.body;

  if (!customerName || !phoneNumber || !address || !items || !totalAmount) {
    return res.status(400).json({ message: 'All fields are required' });
  }

  try {
    const newOrder = new Order({
      customerName,
      phoneNumber,
      address,
      items,
      totalAmount
    });

    await newOrder.save();
    res.status(201).json({ message: 'Order created', order: newOrder });
  } catch (err) {
    res.status(500).json({ message: 'Error creating order', error: err.message });
  }
});

// POST /orders/complete - Mark order complete
router.post('/complete', async (req, res) => {
  const { orderId } = req.body;

  try {
    const order = await Order.findById(orderId);
    if (!order) return res.status(404).send('Order not found');

    try {
      const message = await client.messages.create({
        body: `Hi ${order.customerName}, your order has been completed. Thanks for choosing us!`,
        from: '+15169850191',
        to: order.phoneNumber
      });
      console.log('SMS sent:', message.sid);
    } catch (smsErr) {
      console.error('SMS error:', smsErr.message);
    }

    await Order.findByIdAndDelete(orderId);
    res.status(200).send('Order Completed and Message Sent');
  } catch (err) {
    res.status(500).json({ message: 'Error completing order', error: err.message });
  }
});

module.exports = router;
