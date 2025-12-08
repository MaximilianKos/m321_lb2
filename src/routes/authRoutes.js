var express = require('express');
var router = express.Router();
var authService = require('../services/authService');

router.post('/register', function (req, res, next) {
    var email = req.body.email;
    var password = req.body.password;

    try {
        authService.registerCustomer(email, password);
        // Erfolg: Zur Login-Seite
        res.redirect('/login');
    } catch (e) {
        // Fehler: Seite neu laden mit Fehlermeldung
        res.render('auth/register', {
            user: req.session ? req.session.user : null,
            error: 'Benutzer existiert bereits oder Eingabe ungültig'
        });
    }
});

router.post('/login', function (req, res) {
    var email = req.body.email;
    var password = req.body.password;

    try {
        var user = authService.login(email, password);
        req.session.user = user;
        // Erfolg: Zur Startseite
        res.redirect('/');
    } catch (e) {
        // Fehler: Seite neu laden mit Fehlermeldung
        res.render('auth/login', {
            user: null,
            error: 'Ungültige Zugangsdaten'
        });
    }
});

router.post('/logout', function (req, res) {
    req.session.destroy(function () {
        // Erfolg: Zur Login-Seite
        res.redirect('/login');
    });
});

module.exports = router;