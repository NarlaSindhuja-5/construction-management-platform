/**
 * Report Controller
 * Delivers detailed printable analytical reports for projects, expenses, and site progress.
 */

const {
  getProjectReport,
  getExpensesReport,
  getProgressReport
} = require('../services/reportService');

async function getProjectReportHandler(req, res, next) {
  try {
    const { projectId } = req.params;
    const report = await getProjectReport(projectId);
    return res.status(200).json({ success: true, report });
  } catch (error) {
    next(error);
  }
}

async function getExpensesReportHandler(req, res, next) {
  try {
    const { projectId } = req.params;
    const report = await getExpensesReport(projectId === 'all' ? null : projectId);
    return res.status(200).json({ success: true, report });
  } catch (error) {
    next(error);
  }
}

async function getProgressReportHandler(req, res, next) {
  try {
    const { projectId } = req.params;
    const progress = await getProgressReport(projectId);
    return res.status(200).json({ success: true, count: progress.length, progress });
  } catch (error) {
    next(error);
  }
}

module.exports = {
  getProjectReport: getProjectReportHandler,
  getExpensesReport: getExpensesReportHandler,
  getProgressReport: getProgressReportHandler
};
