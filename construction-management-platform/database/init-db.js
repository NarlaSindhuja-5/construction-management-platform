/**
 * Database Initialization Script
 * Creates construction_management_db, creates all tables, and seeds initial data.
 */

const fs = require('fs');
const path = require('path');

// Resolve packages from backend/node_modules if running from database folder
let mysql, dotenv;
try {
  mysql = require('mysql2/promise');
  dotenv = require('dotenv');
} catch (e) {
  mysql = require(path.join(__dirname, '../backend/node_modules/mysql2/promise'));
  dotenv = require(path.join(__dirname, '../backend/node_modules/dotenv'));
}

dotenv.config({ path: path.join(__dirname, '../backend/.env') });

const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '3306', 10),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD !== undefined ? process.env.DB_PASSWORD : 'root',
  multipleStatements: true
};

async function initDatabase() {
  console.log('----------------------------------------------------');
  console.log('Connecting to MySQL Server at ' + dbConfig.host + ':' + dbConfig.port + ' as ' + dbConfig.user + '...');
  
  let connection;
  try {
    connection = await mysql.createConnection(dbConfig);
    console.log('Connected to MySQL server successfully.');

    // 1. Read and run schema.sql
    const schemaPath = path.join(__dirname, 'schema.sql');
    console.log('Executing schema.sql...');
    const schemaSql = fs.readFileSync(schemaPath, 'utf8');
    await connection.query(schemaSql);
    console.log('Database and all 13 tables created successfully.');

    // 2. Read and run seed.sql
    const seedPath = path.join(__dirname, 'seed.sql');
    console.log('Executing seed.sql...');
    const seedSql = fs.readFileSync(seedPath, 'utf8');
    await connection.query(seedSql);
    console.log('Seed data inserted successfully.');

    console.log('Database initialization completed successfully!');
    console.log('----------------------------------------------------');
  } catch (err) {
    console.error('Database initialization failed:', err.message);
    console.error('Please verify your DB_HOST, DB_USER, DB_PASSWORD in backend/.env');
    process.exit(1);
  } finally {
    if (connection) {
      await connection.end();
    }
  }
}

if (require.main === module) {
  initDatabase();
}

module.exports = { initDatabase };
