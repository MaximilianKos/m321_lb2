var express = require('express');
var router = express.Router();
var userService = require('../services/userService');
var authMiddleware = require('../middleware/authMiddleware');

router.use(authMiddleware.ensureRole('admin'));

// JSON-API Endpoint (bleibt ggf. für externe Clients)
router.get('/users', function (req, res) {
    var users = userService.listUsers();
    res.json(users);
});

// Form-Action: Aktivieren
router.post('/users/:id/activate', function (req, res) {
    userService.setUserActive(req.params.id, true);
    // Redirect zurück zur Liste
    res.redirect('/admin/users');
});

// Form-Action: Deaktivieren
router.post('/users/:id/deactivate', function (req, res) {
    userService.setUserActive(req.params.id, false);
    // Redirect zurück zur Liste
    res.redirect('/admin/users');
});

module.exports = router;