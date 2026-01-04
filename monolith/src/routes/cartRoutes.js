// src/routes/cartRoutes.js
var express = require('express');
var router = express.Router();
var productService = require('../services/productService');
var orderService = require('../services/orderService');
var authMiddleware = require('../middleware/authMiddleware');

// Helper: Warenkorb initialisieren
function getCart(req) {
  if (!req.session.cart) {
    req.session.cart = {
      items: [],     // { productId, name, price, quantity }
      total: 0
    };
  }
  return req.session.cart;
}

// Warenkorb anzeigen
router.get('/', function (req, res) {
  var cart = getCart(req);
  res.render('cart/index', {
    user: req.session.user,
    cart: cart
  });
});

// Produkt hinzufügen (POST von Produktseite)
router.post('/add', function (req, res) {
  var productId = req.body.productId;
  var product = productService.getProduct(productId);

  if (product) {
    var cart = getCart(req);
    var existingItem = cart.items.find(function(i) { return i.productId === productId; });

    if (existingItem) {
      existingItem.quantity++;
    } else {
      cart.items.push({
        productId: product.id,
        name: product.name,
        price: parseFloat(product.price),
        quantity: 1
      });
    }
    
    // Total neu berechnen
    cart.total = cart.items.reduce(function(acc, item) {
      return acc + (item.price * item.quantity);
    }, 0);
  }

  res.redirect('/cart'); // Oder zurück zur Produktseite
});

// Produkt entfernen
router.post('/remove', function (req, res) {
  var productId = req.body.productId;
  var cart = getCart(req);

  cart.items = cart.items.filter(function(item) {
    return item.productId !== productId;
  });

  cart.total = cart.items.reduce(function(acc, item) {
      return acc + (item.price * item.quantity);
  }, 0);

  res.redirect('/cart');
});

// Checkout (Bestellung aufgeben)
router.post('/checkout', authMiddleware.ensureAuthenticated, function (req, res) {
  var cart = getCart(req);
  
  try {
    if (cart.items.length === 0) {
      throw new Error('Leer');
    }
    
    // Order Service aufrufen
    orderService.createOrder(req.session.user.id, cart.items, cart.total);
    
    // Warenkorb leeren
    req.session.cart = { items: [], total: 0 };
    
    res.redirect('/orders');
  } catch (e) {
    console.error(e);
    res.redirect('/cart');
  }
});

module.exports = router;
