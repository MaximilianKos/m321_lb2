
// src/routes/orderRoutes.js
var express = require('express');
var router = express.Router();
var orderService = require('../services/orderService');
var authMiddleware = require('../middleware/authMiddleware');

router.use(authMiddleware.ensureAuthenticated);

// Meine Bestellungen anzeigen
router.get('/', function (req, res) {
  var userId = req.session.user.id;
  var orders = orderService.listOrders(userId);
  
  // Sortieren: Neueste zuerst
  orders.sort(function(a, b) {
    return new Date(b.createdAt) - new Date(a.createdAt);
  });

  res.render('orders/index', {
    user: req.session.user,
    orders: orders
  });
});

module.exports = router;
