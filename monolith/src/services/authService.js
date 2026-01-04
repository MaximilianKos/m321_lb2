// src/services/authService.js
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const userRepository = require('../repositories/userRepository');

function registerCustomer(email, password) {
  const existing = userRepository.findByEmail(email);
  if (existing) {
    throw new Error('User already exists');
  }

  const passwordHash = bcrypt.hashSync(password, 10);
  const user = {
    id: uuidv4(),
    email: email,
    passwordHash: passwordHash,
    role: 'customer', // oder 'admin'
    active: true
  };
  return userRepository.save(user);
}

function login(email, password) {
  const user = userRepository.findByEmail(email);
  if (!user || !user.active) {
    throw new Error('Invalid credentials');
  }
  const valid = bcrypt.compareSync(password, user.passwordHash);
  if (!valid) {
    throw new Error('Invalid credentials');
  }
  return { id: user.id, email: user.email, role: user.role };
}

module.exports = {
  registerCustomer,
  login
};
