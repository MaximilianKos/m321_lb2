// src/services/orderService.js
const uuid = require('uuid');
const orderRepository = require('../repositories/orderRepository');
const productService = require('./productService');

function listOrders(userId) {
    if (userId) {
        return orderRepository.findByUserId(userId);
    }
    return orderRepository.findAll();
}

function createOrder(userId, cartItems, totalAmount) {
    if (!cartItems || cartItems.length === 0) {
        throw new Error('Warenkorb ist leer');
    }

    var order = {
        id: uuid.v4(),
        userId: userId,
        items: cartItems,        // Array von Produkten inkl. quantity
        totalAmount: totalAmount,
        status: 'confirmed',     // Da wir keine Bezahlung haben, direkt bestätigt
        createdAt: new Date().toISOString()
    };

    cartItems.forEach((item) => {
        productService.decreaseStock(item.productId, item.quantity);
    });

    return orderRepository.save(order);
}

module.exports = {
    listOrders: listOrders,
    createOrder: createOrder
};