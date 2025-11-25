// src/services/productService.js
const { v4: uuidv4 } = require('uuid');
const productRepository = require('../repositories/productRepository');

function listProducts() {
  return productRepository.findAll();
}

function getProduct(id) {
  return productRepository.findById(id);
}

function createProduct(data) {
  const product = {
    id: uuidv4(),
    name: data.name,
    description: data.description || '',
    price: data.price,
    stock: data.stock || 0
  };
  return productRepository.save(product);
}

function updateProduct(id, data) {
  const existing = productRepository.findById(id);
  if (!existing) {
    return null;
  }
  existing.name = data.name || existing.name;
  existing.description = data.description || existing.description;
  existing.price = (data.price !== undefined ? data.price : existing.price);
  existing.stock = (data.stock !== undefined ? data.stock : existing.stock);
  return productRepository.save(existing);
}

function deleteProduct(id) {
  productRepository.deleteById(id);
}

module.exports = {
  listProducts,
  getProduct,
  createProduct,
  updateProduct,
  deleteProduct
};
