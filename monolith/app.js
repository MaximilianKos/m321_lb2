// app.js
var express = require('express');
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');
var session = require('express-session');
var path = require('path');

var pageRoutes = require('./src/routes/pageRoutes');
var authRoutes = require('./src/routes/authRoutes');
var productRoutes = require('./src/routes/productRoutes');
var adminRoutes = require('./src/routes/adminRoutes');
var userRoutes = require('./src/routes/userRoutes');
var cartRoutes = require('./src/routes/cartRoutes');
var orderRoutes = require('./src/routes/orderRoutes');
var errorHandler = require('./src/middleware/errorHandler');

var app = express();

// View-Engine konfigurieren
app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'src/views'));

// Statische Dateien (CSS, Bilder, ...)
app.use(express.static(path.join(__dirname, 'public')));

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(session({
    secret: 'dev-secret-key',
    resave: false,
    saveUninitialized: false
}));

// HTML-Seiten
app.use('/', pageRoutes);

// API-Routen (JSON)
app.use('/auth', authRoutes);
app.use('/products', productRoutes);
app.use('/admin', adminRoutes);
app.use('/users', userRoutes);
app.use('/cart', cartRoutes);
app.use('/orders', orderRoutes);

app.get('/health', function (req, res) {
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

app.use(errorHandler);

var PORT = process.env.PORT || 3000;
app.listen(PORT, function () {
    console.log('Shop monolith listening on port ' + PORT);
});