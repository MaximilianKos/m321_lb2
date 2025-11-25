// src/repositories/orderRepository.js
var fs = require('fs');
var path = require('path');

var ordersFilePath = path.join(__dirname, '../../data/orders.json');

function ensureFile() {
    if (!fs.existsSync(ordersFilePath)) {
        fs.writeFileSync(ordersFilePath, JSON.stringify([]), 'utf8');
    }
}

function readOrders() {
    ensureFile();
    var raw = fs.readFileSync(ordersFilePath, 'utf8');
    if (!raw) {
        return [];
    }
    return JSON.parse(raw);
}

function writeOrders(orders) {
    fs.writeFileSync(ordersFilePath, JSON.stringify(orders, null, 2), 'utf8');
}

module.exports = {
    findAll: function () {
        return readOrders();
    },

    findByUserId: function (userId) {
        var orders = readOrders();
        // Filtert alle Bestellungen eines bestimmten Users
        return orders.filter(function(o) {
            return o.userId === userId;
        });
    },

    save: function (order) {
        var orders = readOrders();
        orders.push(order);
        writeOrders(orders);
        return order;
    }
};