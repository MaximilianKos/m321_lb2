// src/repositories/userRepository.js
const fs = require('fs');
const path = require('path');

const usersFilePath = path.join(__dirname, '../../data/users.json');

function readUsersFromFile() {
  if (!fs.existsSync(usersFilePath)) {
    fs.writeFileSync(usersFilePath, JSON.stringify([]), 'utf8');
  }
  const raw = fs.readFileSync(usersFilePath, 'utf8');
  return JSON.parse(raw || '[]');
}

function writeUsersToFile(users) {
  fs.writeFileSync(usersFilePath, JSON.stringify(users, null, 2), 'utf8');
}

module.exports = {
  findAll() {
    return readUsersFromFile();
  },

  findById(id) {
    const users = readUsersFromFile();
    return users.find(u => u.id === id) || null;
  },

  findByEmail(email) {
    const users = readUsersFromFile();
    return users.find(u => u.email === email) || null;
  },

  save(user) {
    const users = readUsersFromFile();
    const existingIndex = users.findIndex(u => u.id === user.id);
    if (existingIndex >= 0) {
      users[existingIndex] = user;
    } else {
      users.push(user);
    }
    writeUsersToFile(users);
    return user;
  },

  deleteById(id) {
    let users = readUsersFromFile();
    users = users.filter(u => u.id !== id);
    writeUsersToFile(users);
  }
};
