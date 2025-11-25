var express = require('express');
var router = express.Router();
var productService = require('../services/productService');
var authMiddleware = require('../middleware/authMiddleware');

// Öffentliche Endpunkte
router.get('/', function (req, res) {
    var products = productService.listProducts();
    res.json(products);
});

router.get('/:id', function (req, res) {
    var product = productService.getProduct(req.params.id);
    if (!product) {
        return res.status(404).json({ error: 'Not found' });
    }
    res.json(product);
});

// Admin: Erstellen
router.post('/', authMiddleware.ensureRole('admin'), function (req, res) {
    productService.createProduct(req.body);
    res.redirect('/admin/products');
});

// Admin: Update via Form (POST)
router.post('/:id/update', authMiddleware.ensureRole('admin'), function (req, res) {
    productService.updateProduct(req.params.id, req.body);
    res.redirect('/admin/products');
});

// Admin: Delete via Form (POST)
router.post('/:id/delete', authMiddleware.ensureRole('admin'), function (req, res) {
    productService.deleteProduct(req.params.id);
    res.redirect('/admin/products');
});

// (Optional) REST-API Endpunkte für PUT/DELETE können parallel bestehen bleiben,
// falls du später per AJAX zugreifen willst.
router.put('/:id', authMiddleware.ensureRole('admin'), function (req, res) {
    var product = productService.updateProduct(req.params.id, req.body);
    res.json(product);
});

router.delete('/:id', authMiddleware.ensureRole('admin'), function (req, res) {
    productService.deleteProduct(req.params.id);
    res.status(204).send();
});

module.exports = router;