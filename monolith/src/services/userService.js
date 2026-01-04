// src/services/userService.js
const userRepository = require('../repositories/userRepository');
const bcrypt = require('bcryptjs');

function listUsers() {
    return userRepository.findAll();
}

function getUserById(id) {
    return userRepository.findById(id);
}

function updateUser(id, userData) {
    const user = userRepository.findById(id);
    if (!user) {
        return null;
    }

    // Nur erlaubte Felder aktualisieren
    if (userData.email) {
        user.email = userData.email;
    }

    // Passwort nur ändern, wenn eines eingegeben wurde
    if (userData.password && userData.password.trim() !== "") {
        user.passwordHash = bcrypt.hashSync(userData.password, 10);
    }

    // WICHTIG: Rolle darf hier nicht geändert werden (Sicherheitsrisiko),
    // es sei denn, wir bauen eine explizite Admin-Funktion dafür.
    // user.role = userData.role; // Auskommentiert lassen!

    userRepository.save(user);
    return user;
}

function setUserActive(id, active) {
  const user = userRepository.findById(id);
  if (!user) return null;
  user.active = active;
  return userRepository.save(user);
}

module.exports = {
    listUsers,
    getUserById,
    updateUser,
    setUserActive
};
