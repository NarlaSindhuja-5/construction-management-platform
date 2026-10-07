/**
 * Dashboard Controller
 * Exposes live summary metrics, project progress bars, and intelligent alerts.
 */

const {
  getDashboardSummary,
  getProjectsProgress,
  getSystemAlerts
} = require('../services/dashboardService');

async function getSummary(req, res, next) {
  try {
    const summary = await getDashboardSummary();
    return res.status(200).json({ success: true, summary });
  } catch (error) {
    next(error);
  }
}

async function getProjects(req, res, next) {
  try {
    const projects = await getProjectsProgress();
    return res.status(200).json({ success: true, projects });
  } catch (error) {
    next(error);
  }
}

async function getAlerts(req, res, next) {
  try {
    const alerts = await getSystemAlerts();
    return res.status(200).json({ success: true, alerts });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  getSummary,
  getProjects,
  getAlerts
};
