// src/routes/userRoutes.js
var express = require('express');
var router = express.Router();
var userService = require('../services/userService');
var authMiddleware = require('../middleware/authMiddleware');

// Aktuell eingeloggter User
router.get('/me', authMiddleware.ensureAuthenticated, function (req, res) {
  var sessionUser = req.session.user;
  var user = userService.getUserById(sessionUser.id);
  if (!user) {
    return res.status(404).json({ error: 'Not found' });
  }

  // Passwort-Hash nicht zurückgeben
  res.json({
    id: user.id,
    email: user.email,
    role: user.role,
    active: user.active
  });
});

// Profil-Update Route (POST /users/profile)
// Sicherstellen, dass nur der eingeloggte User sein eigenes Profil ändern kann
router.post('/profile', authMiddleware.ensureAuthenticated, function (req, res) {
    const userId = req.session.user.id; // ID sicher aus der Session holen

    userService.updateUser(userId, req.body);

    // Session-Daten aktualisieren, falls sich z.B. die Email geändert hat
    req.session.user.email = req.body.email;

    res.redirect('/profile');
});


module.exports = router;
