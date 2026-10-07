/**
 * Database Connection Pool Configuration
 * Uses mysql2/promise for connection pooling and transaction support.
 */

const mysql = require('mysql2/promise');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '../.env') });

const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '3306', 10),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD !== undefined ? process.env.DB_PASSWORD : 'root',
  database: process.env.DB_NAME || 'construction_management_db',
  waitForConnections: true,
  connectionLimit: 15,
  queueLimit: 0,
  enableKeepAlive: true,
  keepAliveInitialDelay: 0,
  dateStrings: true // Return DATE / DATETIME as ISO strings rather than JS Date to preserve local date values
});

// Quick connection health check
async function testConnection() {
  try {
    const connection = await pool.getConnection();
    console.log(`[Database] Successfully connected to MySQL (${process.env.DB_NAME || 'construction_management_db'}).`);
    connection.release();
    return true;
  } catch (error) {
    console.error(`[Database Warning] Unable to connect to MySQL database: ${error.message}`);
    console.error('Make sure MySQL is running and verify DB_USER/DB_PASSWORD in backend/.env');
    return false;
  }
}

module.exports = {
  pool,
  query: (sql, params) => pool.query(sql, params),
  getConnection: () => pool.getConnection(),
  testConnection
};
