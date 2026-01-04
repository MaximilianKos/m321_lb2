// src/routes/pageRoutes.js
var express = require('express');
var router = express.Router();
var productService = require('../services/productService');
var userService = require('../services/userService');
var authMiddleware = require('../middleware/authMiddleware');

// Helper für Template-User
function getSessionUser(req) {
  return req.session && req.session.user ? req.session.user : null;
}

// Startseite: Produktliste
router.get('/', function (req, res) {
  var products = productService.listProducts();
  res.render('index', {
    user: getSessionUser(req),
    products: products
  });
});

// Login-Seite
router.get('/login', function (req, res) {
  res.render('auth/login', {
    user: getSessionUser(req),
    error: null
  });
});

// Login-Form verarbeitet POST /auth/login, hier nur Fehler-Redirect:
router.get('/login-error', function (req, res) {
  res.render('auth/login', {
    user: getSessionUser(req),
    error: 'Login fehlgeschlagen. Bitte prüfe deine Eingaben.'
  });
});

// Registrierungsseite
router.get('/register', function (req, res) {
  res.render('auth/register', {
    user: getSessionUser(req),
    error: null
  });
});

// Produkt-Detailseite
router.get('/products/:id', function (req, res) {
  var product = productService.getProduct(req.params.id);
  if (!product) {
    return res.status(404).render('404', {
      user: getSessionUser(req),
      message: 'Produkt nicht gefunden'
    });
  }
  res.render('products/detail', {
    user: getSessionUser(req),
    product: product
  });
});

// Profilseite für eingeloggten User
router.get('/profile', authMiddleware.ensureAuthenticated, function (req, res) {
  var sessionUser = req.session.user;
  var user = userService.getUserById(sessionUser.id);
  if (!user) {
    return res.status(404).render('404', {
      user: getSessionUser(req),
      message: 'Benutzer nicht gefunden'
    });
  }

  res.render('users/profile', {
    user: sessionUser,
    fullUser: user
  });
});

// Admin: Benutzerübersicht
router.get('/admin/users', authMiddleware.ensureRole('admin'), function (req, res) {
  var users = userService.listUsers();
  res.render('admin/users', {
    user: getSessionUser(req),
    users: users
  });
});

// Admin: Produktverwaltung (Liste)
router.get('/admin/products', authMiddleware.ensureRole('admin'), function (req, res) {
  var products = productService.listProducts();
  res.render('admin/products', {
    user: getSessionUser(req),
    products: products
  });
});

// Admin: Formular neues Produkt
router.get('/admin/products/new', authMiddleware.ensureRole('admin'), function (req, res) {
  res.render('admin/productForm', {
    user: getSessionUser(req),
    product: null
  });
});

// Admin: Formular bestehendes Produkt bearbeiten
router.get('/admin/products/:id/edit', authMiddleware.ensureRole('admin'), function (req, res) {
  var product = productService.getProduct(req.params.id);
  if (!product) {
    return res.status(404).render('404', {
      user: getSessionUser(req),
      message: 'Produkt nicht gefunden'
    });
  }
  res.render('admin/productForm', {
    user: getSessionUser(req),
    product: product
  });
});

module.exports = router;
