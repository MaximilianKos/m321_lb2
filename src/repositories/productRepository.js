// src/repositories/productRepository.js
const fs = require('fs');
const path = require('path');

const productsFilePath = path.join(__dirname, '../../data/products.json');

function readProducts() {
  if (!fs.existsSync(productsFilePath)) {
    fs.writeFileSync(productsFilePath, JSON.stringify([]), 'utf8');
  }
  const raw = fs.readFileSync(productsFilePath, 'utf8');
  return JSON.parse(raw || '[]');
}

function writeProducts(products) {
  fs.writeFileSync(productsFilePath, JSON.stringify(products, null, 2), 'utf8');
}

module.exports = {
  findAll() {
    return readProducts();
  },
  findById(id) {
    const products = readProducts();
    return products.find(p => p.id === id) || null;
  },
  save(product) {
    const products = readProducts();
    const index = products.findIndex(p => p.id === product.id);
    if (index >= 0) {
      products[index] = product;
    } else {
      products.push(product);
    }
    writeProducts(products);
    return product;
  },
  deleteById(id) {
    let products = readProducts();
    products = products.filter(p => p.id !== id);
    writeProducts(products);
  }
};
