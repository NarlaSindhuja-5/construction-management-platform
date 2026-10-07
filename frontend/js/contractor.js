/**
 * E2E Construction Management System - Contractor Dashboard Logic
 * Vanilla JavaScript & Fetch API connected to real Spring Boot APIs.
 */

const ContractorApp = {
    currentUser: null,
    contractorProfile: null,
    projectsCache: [],
    currentSection: 'overview',

    /**
     * Entry point on DOM load
     */
    async init() {
        // Enforce route guard
        if (!window.AuthService || !window.AuthService.requireAuth(['CONTRACTOR', 'SITE_MANAGER'])) {
            return;
        }

        this.currentUser = AuthService.getUser();
        this.renderUserIdentity();
        this.bindEvents();

        // Load contractor company profile & verification status
        await this.loadContractorProfile();

        // Load project cache for dropdown selects across modules
        await this.loadProjectsCache();

        // Load unread notification badge
        await this.updateNotificationBadge();

        // Navigate to default or URL hash section
        const initialSection = window.location.hash ? window.location.hash.replace('#', '') : 'overview';
        this.switchSection(initialSection);
    },

    /**
     * Render user info in topbar and sidebar
     */
    renderUserIdentity() {
        if (!this.currentUser) return;
        const name = `${this.currentUser.firstName || ''} ${this.currentUser.lastName || ''}`.trim() || 'Contractor';
        const initial = (this.currentUser.firstName || 'C')[0].toUpperCase();

        document.getElementById('sidebarUserName').innerText = name;
        document.getElementById('sidebarUserRole').innerText = this.currentUser.role || 'CONTRACTOR';
        document.getElementById('sidebarUserAvatar').innerText = initial;
    },

    /**
     * Fetch contractor profile to check verification status and business details
     */
    async loadContractorProfile() {
        try {
            this.contractorProfile = await AuthService.apiFetch('/contractors/profile');
            const status = this.contractorProfile.verificationStatus || 'PENDING';
            this.updateVerificationBadge(status);
        } catch (e) {
            console.warn('Contractor profile not found or not initialized yet:', e);
            this.contractorProfile = null;
            this.updateVerificationBadge('PENDING');
        }
    },

    updateVerificationBadge(status) {
        const badge = document.getElementById('verificationPill');
        if (!badge) return;

        badge.className = `verification-pill ${status.toLowerCase()}`;
        let label = status;
        if (status === 'VERIFIED') label = 'Verified Contractor';
        else if (status === 'PENDING') label = 'Verification Pending';
        else if (status === 'REJECTED') label = 'Verification Rejected';

        badge.innerHTML = `
            <span style="display:inline-block; width:7px; height:7px; border-radius:50%; background:currentColor;"></span>
            <span>${label}</span>
        `;
    },

    /**
     * Cache active projects to populate dropdown selectors across all modals
     */
    async loadProjectsCache() {
        try {
            const list = await AuthService.apiFetch('/projects/my-projects');
            this.projectsCache = list || [];
            this.populateProjectSelects();
        } catch (e) {
            console.error('Failed to cache projects:', e);
            this.projectsCache = [];
        }
    },

    populateProjectSelects() {
        const selects = document.querySelectorAll('.project-selector-dropdown');
        selects.forEach(select => {
            const currentVal = select.value;
            const hasAllOption = select.dataset.allowAll === 'true';
            select.innerHTML = hasAllOption ? '<option value="">All Projects</option>' : '<option value="" disabled selected>-- Select Project --</option>';

            this.projectsCache.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.id;
                opt.textContent = `${p.name} (${p.city || p.location})`;
                select.appendChild(opt);
            });

            if (currentVal) select.value = currentVal;
        });
    },

    /**
     * Switch view between the 10 sections
     */
    switchSection(sectionId) {
        const validSections = [
            'overview', 'projects', 'labor', 'machinery', 'materials',
            'tenders', 'reports', 'attendance', 'notifications', 'profile'
        ];

        if (!validSections.includes(sectionId)) sectionId = 'overview';
        this.currentSection = sectionId;
        window.location.hash = sectionId;

        // Update sidebar links
        document.querySelectorAll('.sidebar-link').forEach(link => {
            link.classList.toggle('active', link.dataset.section === sectionId);
        });

        // Update views
        document.querySelectorAll('.section-view').forEach(view => {
            view.classList.toggle('active', view.id === `section-${sectionId}`);
        });

        // Update Topbar Title
        const titleMap = {
            overview: 'Contractor Operations Overview',
            projects: 'My Construction Projects',
            labor: 'Find & Hire Skilled Labor',
            machinery: 'Heavy Machinery & Equipment Rental',
            materials: 'Construction Materials Marketplace',
            tenders: 'Government & Corporate Tenders',
            reports: 'Daily Work Site Reports',
            attendance: 'Labor Attendance & Daily Timesheets',
            notifications: 'System Notifications & Alerts',
            profile: 'Contractor Company Profile'
        };
        document.getElementById('topbarPageTitle').innerText = titleMap[sectionId] || 'Contractor Dashboard';

        // Load section content dynamically
        this.loadSectionData(sectionId);
    },

    /**
     * Delegate section data loading
     */
    async loadSectionData(sectionId) {
        switch (sectionId) {
            case 'overview':
                await this.loadOverview();
                break;
            case 'projects':
                await this.loadProjects();
                break;
            case 'labor':
                await this.loadLabor();
                break;
            case 'machinery':
                await this.loadMachinery();
                break;
            case 'materials':
                await this.loadMaterials();
                break;
            case 'tenders':
                await this.loadTenders();
                break;
            case 'reports':
                await this.loadDailyReports();
                break;
            case 'attendance':
                await this.loadAttendance();
                break;
            case 'notifications':
                await this.loadNotifications();
                break;
            case 'profile':
                await this.loadProfileForm();
                break;
        }
    },

    /**
     * Bind all interactive events
     */
    bindEvents() {
        // Sidebar navigation clicks
        // Mobile sidebar & backdrop handlers
        const sidebar = document.getElementById('sidebar');
        const backdrop = document.getElementById('sidebarBackdrop');
        const toggleBtn = document.getElementById('mobileMenuToggle');

        const closeSidebar = () => {
            if (sidebar) sidebar.classList.remove('open');
            if (backdrop) backdrop.classList.remove('active');
        };

        document.querySelectorAll('.sidebar-link').forEach(btn => {
            btn.addEventListener('click', () => {
                const section = btn.dataset.section;
                if (section) this.switchSection(section);
                closeSidebar();
            });
        });

        if (toggleBtn) {
            toggleBtn.addEventListener('click', () => {
                const isOpen = sidebar.classList.toggle('open');
                if (backdrop) backdrop.classList.toggle('active', isOpen);
            });
        }

        if (backdrop) {
            backdrop.addEventListener('click', closeSidebar);
        }

        // Global Logout Button
        const logoutBtn = document.getElementById('globalLogoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                AuthService.logout();
            });
        }

        // Subtabs event delegation
        document.querySelectorAll('.subtab-btn').forEach(tab => {
            tab.addEventListener('click', (e) => {
                const parent = tab.closest('.content-card');
                parent.querySelectorAll('.subtab-btn').forEach(t => t.classList.remove('active'));
                tab.classList.add('active');

                const targetSub = tab.dataset.target;
                parent.querySelectorAll('.subtab-content').forEach(c => {
                    c.style.display = c.id === targetSub ? 'block' : 'none';
                });
            });
        });

        // Close modal handlers
        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay || e.target.classList.contains('modal-close-btn')) {
                    overlay.classList.remove('active');
                }
            });
        });
    },

    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
            UI.clearAlert(`${modalId}Alert`);
        }
    },

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.remove('active');
    },

    async updateNotificationBadge() {
        try {
            const summary = await AuthService.apiFetch('/notifications/unread-count');
            const count = summary ? summary.unreadCount : 0;
            const badge = document.getElementById('sidebarNotifBadge');
            if (badge) {
                badge.innerText = count;
                badge.style.display = count > 0 ? 'inline-block' : 'none';
            }
        } catch (e) {
            console.warn('Could not fetch notification unread count:', e);
        }
    },

    /* =========================================================================
       1. OVERVIEW SECTION
       ========================================================================= */
    async loadOverview() {
        try {
            // Load live metrics asynchronously in parallel
            const [projects, laborReqs, machineryBookings, materialOrders, tenders, notifSummary] = await Promise.all([
                AuthService.apiFetch('/projects/my-projects').catch(() => []),
                AuthService.apiFetch('/labor-requests/sent').catch(() => []),
                AuthService.apiFetch('/machinery/bookings/my-bookings').catch(() => []),
                AuthService.apiFetch('/material-orders/my-orders').catch(() => []),
                AuthService.apiFetch('/tenders/applications/my-applications').catch(() => []),
                AuthService.apiFetch('/notifications/unread-count').catch(() => ({ unreadCount: 0 }))
            ]);

            const activeProjectsCount = projects.filter(p => p.status === 'IN_PROGRESS').length;
            const pendingLabor = laborReqs.filter(r => r.status === 'PENDING').length;
            const activeBookings = machineryBookings.filter(b => b.status === 'ACCEPTED').length;
            const activeOrders = materialOrders.filter(o => o.status === 'ACCEPTED' || o.status === 'SHIPPED').length;

            document.getElementById('statTotalProjects').innerText = projects.length;
            document.getElementById('statActiveProjects').innerText = activeProjectsCount;
            document.getElementById('statLaborRequests').innerText = laborReqs.length;
            document.getElementById('statMachineryBookings').innerText = machineryBookings.length;
            document.getElementById('statMaterialOrders').innerText = materialOrders.length;
            document.getElementById('statTenderBids').innerText = tenders.length;
            document.getElementById('statUnreadAlerts').innerText = notifSummary.unreadCount || 0;

            // Render Recent Projects Preview Table
            const recentProjectsBody = document.getElementById('overviewRecentProjectsTable');
            if (recentProjectsBody) {
                if (projects.length === 0) {
                    recentProjectsBody.innerHTML = `<tr><td colspan="5" class="empty-state-box">No projects created yet. Click "+ New Project" to get started.</td></tr>`;
                } else {
                    recentProjectsBody.innerHTML = projects.slice(0, 5).map(p => `
                        <tr>
                            <td><strong>${this.escape(p.name)}</strong></td>
                            <td>${this.escape(p.city || p.location)}</td>
                            <td>$${Number(p.budget || 0).toLocaleString()}</td>
                            <td>
                                <div style="display: flex; align-items: center; gap: 0.5rem;">
                                    <div class="progress-track" style="flex: 1; max-width: 80px;">
                                        <div class="progress-fill" style="width: ${p.progressPercentage || 0}%"></div>
                                    </div>
                                    <span>${p.progressPercentage || 0}%</span>
                                </div>
                            </td>
                            <td><span class="status-badge ${(p.status || '').toLowerCase()}">${p.status || 'PLANNING'}</span></td>
                        </tr>
                    `).join('');
                }
            }

            // Render Recent Notifications Preview
            const notifs = await AuthService.apiFetch('/notifications?unreadOnly=false').catch(() => []);
            const recentNotifFeed = document.getElementById('overviewNotifFeed');
            if (recentNotifFeed) {
                if (notifs.length === 0) {
                    recentNotifFeed.innerHTML = `<div class="empty-state-box">No recent activity notifications.</div>`;
                } else {
                    recentNotifFeed.innerHTML = notifs.slice(0, 4).map(n => `
                        <div class="notif-row ${!n.read ? 'unread' : ''}" onclick="ContractorApp.switchSection('notifications')">
                            <div class="notif-icon icon-blue">
                                <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>
                            </div>
                            <div class="notif-body">
                                <div class="notif-title">
                                    <span>${this.escape(n.title)}</span>
                                    <span class="notif-time">${this.formatDate(n.createdAt)}</span>
                                </div>
                                <div class="notif-message">${this.escape(n.message)}</div>
                            </div>
                        </div>
                    `).join('');
                }
            }

        } catch (e) {
            console.error('Error loading overview:', e);
        }
    },

    /* =========================================================================
       2. MY PROJECTS SECTION
       ========================================================================= */
    async loadProjects() {
        const searchInput = document.getElementById('projectSearchInput');
        const statusSelect = document.getElementById('projectStatusFilter');
        const typeSelect = document.getElementById('projectTypeFilter');

        const search = searchInput ? searchInput.value.trim() : '';
        const status = statusSelect ? statusSelect.value : '';
        const projectType = typeSelect ? typeSelect.value : '';

        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (status) params.append('status', status);
        if (projectType) params.append('projectType', projectType);

        const tbody = document.getElementById('projectsTableBody');
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state-box"><span class="spinner"></span> Loading projects...</td></tr>`;

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const projects = await AuthService.apiFetch(`/projects/my-projects${query}`);
            this.projectsCache = projects;
            this.populateProjectSelects();

            if (!projects || projects.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" class="empty-state-box">No projects match your filter. Click "+ New Project" to add one.</td></tr>`;
                return;
            }

            tbody.innerHTML = projects.map(p => `
                <tr>
                    <td><strong>${this.escape(p.name)}</strong><br><small style="color:var(--text-muted);">${this.escape(p.projectType || 'OTHER')}</small></td>
                    <td>${this.escape(p.location)}, ${this.escape(p.city)} (${this.escape(p.state)})</td>
                    <td>$${Number(p.budget || 0).toLocaleString()}</td>
                    <td>
                        <div style="display:flex; align-items:center; gap:0.5rem;">
                            <div class="progress-track" style="flex:1; max-width:80px;">
                                <div class="progress-fill" style="width:${p.progressPercentage || 0}%"></div>
                            </div>
                            <span>${p.progressPercentage || 0}%</span>
                        </div>
                    </td>
                    <td><small>${this.formatDate(p.startDate)} &rarr; ${this.formatDate(p.expectedCompletionDate)}</small></td>
                    <td><span class="status-badge ${(p.status || '').toLowerCase()}">${p.status || 'PLANNING'}</span></td>
                    <td>
                        <button class="btn btn-outline" style="padding:0.3rem 0.65rem; font-size:0.8rem;" onclick="ContractorApp.viewProjectDetails(${p.id})">Details</button>
                    </td>
                </tr>
            `).join('');

        } catch (e) {
            console.error('Error loading projects:', e);
            tbody.innerHTML = `<tr><td colspan="7" class="empty-state-box" style="color:var(--danger);">Failed to load projects from server.</td></tr>`;
        }
    },

    async submitNewProject(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('newProjectModalAlert');
        UI.clearFieldErrors(form);

        const name = form.name.value.trim();
        const projectType = form.projectType.value;
        const location = form.location.value.trim();
        const city = form.city.value.trim();
        const state = form.state.value.trim();
        const budget = form.budget.value;
        const startDate = form.startDate.value;
        const expectedCompletionDate = form.expectedCompletionDate.value;
        const description = form.description.value.trim();
        const status = form.status.value;
        const progressPercentage = form.progressPercentage.value || 0;

        const payload = {
            name,
            projectType,
            location,
            city,
            state,
            budget: Number(budget),
            startDate: startDate || null,
            expectedCompletionDate: expectedCompletionDate || null,
            description,
            status: status || 'PLANNING',
            progressPercentage: Number(progressPercentage)
        };

        UI.setLoading('submitProjectBtn', true, 'Create Project');

        try {
            await AuthService.apiFetch('/projects', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            UI.showAlert('newProjectModalAlert', 'Project created successfully!', 'success');
            form.reset();
            setTimeout(() => {
                ContractorApp.closeModal('newProjectModal');
                ContractorApp.loadProjects();
                ContractorApp.loadProjectsCache();
            }, 600);

        } catch (error) {
            UI.setLoading('submitProjectBtn', false, 'Create Project');
            if (error.validationErrors) {
                Object.keys(error.validationErrors).forEach(field => {
                    const el = form[field];
                    if (el) UI.showFieldError(el, error.validationErrors[field]);
                });
            } else {
                UI.showAlert('newProjectModalAlert', error.message || 'Failed to create project', 'danger');
            }
        }
    },

    async viewProjectDetails(projectId) {
        this.openModal('projectDetailsModal');
        const container = document.getElementById('projectDetailsContent');
        container.innerHTML = '<div class="empty-state-box"><span class="spinner"></span> Loading project details...</div>';

        try {
            const [project, assignments, reports] = await Promise.all([
                AuthService.apiFetch(`/projects/${projectId}`),
                AuthService.apiFetch(`/projects/${projectId}/labor-assignments`).catch(() => []),
                AuthService.apiFetch(`/daily-reports?projectId=${projectId}`).catch(() => [])
            ]);

            container.innerHTML = `
                <div style="margin-bottom: 1.5rem;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                        <div>
                            <h3 style="font-size: 1.3rem; margin-bottom: 0.25rem;">${this.escape(project.name)}</h3>
                            <p style="color: var(--text-secondary);">${this.escape(project.location)}, ${this.escape(project.city)} (${this.escape(project.state)})</p>
                        </div>
                        <span class="status-badge ${(project.status || '').toLowerCase()}">${project.status}</span>
                    </div>
                    <p style="margin-top: 0.75rem; color: #334155;">${this.escape(project.description || 'No description provided.')}</p>
                </div>

                <div class="metrics-grid" style="grid-template-columns: repeat(3, 1fr); margin-bottom: 1.5rem;">
                    <div class="metric-card" style="padding: 1rem;">
                        <div class="metric-data">
                            <h4>Budget</h4>
                            <div class="metric-value" style="font-size: 1.3rem;">$${Number(project.budget || 0).toLocaleString()}</div>
                        </div>
                    </div>
                    <div class="metric-card" style="padding: 1rem;">
                        <div class="metric-data">
                            <h4>Progress</h4>
                            <div class="metric-value" style="font-size: 1.3rem;">${project.progressPercentage || 0}%</div>
                        </div>
                    </div>
                    <div class="metric-card" style="padding: 1rem;">
                        <div class="metric-data">
                            <h4>Workers Assigned</h4>
                            <div class="metric-value" style="font-size: 1.3rem;">${assignments.length}</div>
                        </div>
                    </div>
                </div>

                <h4 style="font-size: 1rem; margin-bottom: 0.75rem;">Assigned Labor Workforce (${assignments.length})</h4>
                ${assignments.length === 0 ? '<p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1.5rem;">No laborers assigned to this project yet.</p>' : `
                    <div class="table-responsive" style="margin-bottom: 1.5rem;">
                        <table class="custom-table">
                            <thead>
                                <tr><th>Laborer</th><th>Dates</th><th>Daily Wage</th><th>Status</th></tr>
                            </thead>
                            <tbody>
                                ${assignments.map(a => `
                                    <tr>
                                        <td><strong>${this.escape(a.laborerName || 'Laborer #' + a.laborerId)}</strong></td>
                                        <td>${this.formatDate(a.startDate)} &rarr; ${this.formatDate(a.endDate)}</td>
                                        <td>$${a.dailyWage || '--'}</td>
                                        <td><span class="status-badge ${(a.status || '').toLowerCase()}">${a.status}</span></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `}

                <h4 style="font-size: 1rem; margin-bottom: 0.75rem;">Recent Daily Work Reports (${reports.length})</h4>
                ${reports.length === 0 ? '<p style="color: var(--text-muted); font-size: 0.85rem;">No daily reports submitted for this project yet.</p>' : `
                    <div class="table-responsive">
                        <table class="custom-table">
                            <thead>
                                <tr><th>Date</th><th>Workers</th><th>Progress %</th><th>Expenses</th><th>Summary</th></tr>
                            </thead>
                            <tbody>
                                ${reports.slice(0, 5).map(r => `
                                    <tr>
                                        <td>${this.formatDate(r.date)}</td>
                                        <td>${r.numberOfWorkers} workers</td>
                                        <td>${r.progressPercentage || 0}%</td>
                                        <td>$${Number(r.expenses || 0).toLocaleString()}</td>
                                        <td>${this.escape(r.workDescription || '')}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `}
            `;

        } catch (e) {
            container.innerHTML = `<div class="empty-state-box" style="color:var(--danger);">Failed to load details: ${e.message}</div>`;
        }
    },

    /* =========================================================================
       3. FIND LABOR SECTION
       ========================================================================= */
    async loadLabor() {
        const skill = document.getElementById('laborSkillFilter').value;
        const location = document.getElementById('laborLocationFilter').value.trim();
        const maxWage = document.getElementById('laborMaxWageFilter').value;

        const params = new URLSearchParams();
        if (skill) params.append('skill', skill);
        if (location) params.append('location', location);
        if (maxWage) params.append('maxDailyWage', maxWage);

        const container = document.getElementById('laborMarketGrid');
        container.innerHTML = '<div class="empty-state-box"><span class="spinner"></span> Searching verified laborers...</div>';

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const laborers = await AuthService.apiFetch(`/laborers/search${query}`);

            if (!laborers || laborers.length === 0) {
                container.innerHTML = '<div class="empty-state-box"><div class="empty-state-title">No laborers found</div><div class="empty-state-desc">Try clearing or adjusting your search filters.</div></div>';
            } else {
                container.innerHTML = laborers.map(l => `
                    <div class="market-card">
                        <div>
                            <div class="market-card-top">
                                <div>
                                    <div class="market-card-title">${this.escape(l.laborerName || l.fullName || 'Trade Specialist')}</div>
                                    <div class="market-card-subtitle">${this.escape(l.city || l.location || 'Site Location')}</div>
                                </div>
                                <span class="status-badge ${(l.availability || 'available').toLowerCase()}">${l.availability || 'AVAILABLE'}</span>
                            </div>
                            <div class="market-specs">
                                <div class="market-spec-item">
                                    <span>Primary Trade:</span>
                                    <span>${this.escape(l.skill || l.primarySkill || 'General Labor')}</span>
                                </div>
                                <div class="market-spec-item">
                                    <span>Experience:</span>
                                    <span>${l.yearsOfExperience || 0} years</span>
                                </div>
                                <div class="market-spec-item">
                                    <span>Rating:</span>
                                    <span>★ ${l.rating || '5.0'} / 5.0</span>
                                </div>
                            </div>
                        </div>
                        <div class="market-card-bottom">
                            <div class="market-price">
                                $${l.dailyWage || 150} <span>/ day</span>
                            </div>
                            <button class="btn btn-primary" style="padding: 0.45rem 1rem; font-size: 0.85rem;" onclick="ContractorApp.openHireLaborModal(${l.id}, '${this.escape(l.laborerName || 'Laborer')}', ${l.dailyWage || 150})">
                                Hire Laborer
                            </button>
                        </div>
                    </div>
                `).join('');
            }

            // Also load Sent Requests Tab
            await this.loadLaborSentRequests();

        } catch (e) {
            console.error('Error loading laborers:', e);
            container.innerHTML = '<div class="empty-state-box" style="color:var(--danger);">Failed to load laborers.</div>';
        }
    },

    async loadLaborSentRequests() {
        const tbody = document.getElementById('laborSentRequestsTable');
        if (!tbody) return;

        try {
            const requests = await AuthService.apiFetch('/labor-requests/sent');
            if (!requests || requests.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box">No sent labor requests. Click "Hire Laborer" on any profile to send a request.</td></tr>';
                return;
            }

            tbody.innerHTML = requests.map(r => `
                <tr>
                    <td>#${r.id}</td>
                    <td><strong>${this.escape(r.laborerName || 'Laborer #' + r.laborerId)}</strong></td>
                    <td>${this.escape(r.projectName || 'Project #' + r.projectId)}</td>
                    <td>${this.formatDate(r.startDate)} &rarr; ${this.formatDate(r.endDate)}</td>
                    <td>$${r.dailyWage || '--'} / day</td>
                    <td><span class="status-badge ${(r.status || '').toLowerCase()}">${r.status}</span></td>
                    <td>
                        ${r.status === 'PENDING' ? `
                            <button class="btn btn-outline" style="padding:0.25rem 0.6rem; font-size:0.75rem; color:var(--danger);" onclick="ContractorApp.cancelLaborRequest(${r.id})">Cancel</button>
                        ` : '--'}
                    </td>
                </tr>
            `).join('');

        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box" style="color:var(--danger);">Error loading sent requests.</td></tr>';
        }
    },

    openHireLaborModal(laborerId, laborerName, dailyWage) {
        const modal = document.getElementById('hireLaborModal');
        modal.querySelector('#hireLaborerId').value = laborerId;
        modal.querySelector('#hireLaborerName').innerText = laborerName;
        modal.querySelector('#hireDailyWage').value = dailyWage || 150;
        this.openModal('hireLaborModal');
    },

    async submitLaborRequest(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('hireLaborModalAlert');

        const laborerId = form.laborerId.value;
        const projectId = form.projectId.value;
        const startDate = form.startDate.value;
        const endDate = form.endDate.value;
        const dailyWage = form.dailyWage.value;
        const jobDescription = form.jobDescription.value.trim();
        const contractorNotes = form.contractorNotes.value.trim();

        if (!projectId) {
            UI.showAlert('hireLaborModalAlert', 'Please select a project for this labor assignment', 'danger');
            return;
        }

        const payload = {
            laborerId: Number(laborerId),
            projectId: Number(projectId),
            startDate,
            endDate,
            dailyWage: Number(dailyWage),
            jobDescription,
            contractorNotes
        };

        UI.setLoading('submitLaborRequestBtn', true, 'Send Work Request');

        try {
            await AuthService.apiFetch(`/laborers/${laborerId}/work-requests`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            UI.showAlert('hireLaborModalAlert', 'Work request sent successfully to laborer!', 'success');
            form.reset();
            setTimeout(() => {
                ContractorApp.closeModal('hireLaborModal');
                ContractorApp.loadLaborSentRequests();
            }, 600);

        } catch (err) {
            UI.setLoading('submitLaborRequestBtn', false, 'Send Work Request');
            UI.showAlert('hireLaborModalAlert', err.message || 'Failed to send work request', 'danger');
        }
    },

    async cancelLaborRequest(requestId) {
        if (!confirm('Are you sure you want to cancel this labor work request?')) return;
        try {
            await AuthService.apiFetch(`/labor-requests/${requestId}/cancel`, {
                method: 'PUT',
                body: JSON.stringify({ reason: 'Contractor cancelled request' })
            });
            this.loadLaborSentRequests();
        } catch (e) {
            alert(e.message || 'Could not cancel request');
        }
    },

    /* =========================================================================
       4. FIND MACHINERY SECTION
       ========================================================================= */
    async loadMachinery() {
        const category = document.getElementById('machineryCategoryFilter').value;
        const search = document.getElementById('machinerySearchInput').value.trim();

        const params = new URLSearchParams();
        if (category) params.append('category', category);
        if (search) params.append('search', search);

        const container = document.getElementById('machineryMarketGrid');
        container.innerHTML = '<div class="empty-state-box"><span class="spinner"></span> Loading verified machinery listings...</div>';

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const list = await AuthService.apiFetch(`/machinery/verified${query}`);

            if (!list || list.length === 0) {
                container.innerHTML = '<div class="empty-state-box"><div class="empty-state-title">No machinery found</div><div class="empty-state-desc">Try clearing category or keyword filters.</div></div>';
            } else {
                container.innerHTML = list.map(m => `
                    <div class="market-card">
                        <div>
                            <div class="market-card-top">
                                <div>
                                    <div class="market-card-title">${this.escape(m.name || m.title || 'Construction Equipment')}</div>
                                    <div class="market-card-subtitle">${this.escape(m.category || 'HEAVY_EQUIPMENT')} &bull; ${this.escape(m.city || m.location || 'Location')}</div>
                                </div>
                                <span class="status-badge ${(m.availability || 'available').toLowerCase()}">${m.availability || 'AVAILABLE'}</span>
                            </div>
                            <div class="market-specs">
                                <div class="market-spec-item">
                                    <span>Model / Specs:</span>
                                    <span>${this.escape(m.model || m.specifications || 'Standard')}</span>
                                </div>
                                <div class="market-spec-item">
                                    <span>Operator Provided:</span>
                                    <span>${m.operatorProvided ? 'Yes' : 'No'}</span>
                                </div>
                                <div class="market-spec-item">
                                    <span>Hourly Rate:</span>
                                    <span>$${m.hourlyRate || '--'} / hr</span>
                                </div>
                            </div>
                        </div>
                        <div class="market-card-bottom">
                            <div class="market-price">
                                $${m.dailyRate || 350} <span>/ day</span>
                            </div>
                            <button class="btn btn-primary" style="padding: 0.45rem 1rem; font-size: 0.85rem;" onclick="ContractorApp.openBookMachineModal(${m.id}, '${this.escape(m.name || 'Machine')}', '${this.escape(m.city || '')}')">
                                Book Machine
                            </button>
                        </div>
                    </div>
                `).join('');
            }

            await this.loadMachineryBookings();

        } catch (e) {
            console.error('Error loading machinery:', e);
            container.innerHTML = '<div class="empty-state-box" style="color:var(--danger);">Failed to load machinery listings.</div>';
        }
    },

    async loadMachineryBookings() {
        const tbody = document.getElementById('machineryBookingsTable');
        if (!tbody) return;

        try {
            const bookings = await AuthService.apiFetch('/machinery/bookings/my-bookings');
            if (!bookings || bookings.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box">No machinery bookings found. Browse equipment and book needed machines.</td></tr>';
                return;
            }

            tbody.innerHTML = bookings.map(b => `
                <tr>
                    <td>#${b.id}</td>
                    <td><strong>${this.escape(b.machineName || 'Machinery #' + b.machineId)}</strong></td>
                    <td>${this.escape(b.projectName || 'Site Project')}</td>
                    <td>${this.formatDate(b.startDate)} &rarr; ${this.formatDate(b.endDate)}</td>
                    <td>$${Number(b.totalAmount || 0).toLocaleString()}</td>
                    <td><span class="status-badge ${(b.status || '').toLowerCase()}">${b.status}</span></td>
                    <td>
                        ${b.status === 'PENDING' || b.status === 'ACCEPTED' ? `
                            <button class="btn btn-outline" style="padding:0.25rem 0.6rem; font-size:0.75rem; color:var(--danger);" onclick="ContractorApp.cancelMachineBooking(${b.id})">Cancel</button>
                        ` : '--'}
                    </td>
                </tr>
            `).join('');

        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box" style="color:var(--danger);">Error loading bookings.</td></tr>';
        }
    },

    openBookMachineModal(machineId, machineName, defaultLocation) {
        const modal = document.getElementById('bookMachineModal');
        modal.querySelector('#bookMachineId').value = machineId;
        modal.querySelector('#bookMachineName').innerText = machineName;
        modal.querySelector('#bookDeliveryLocation').value = defaultLocation || '';
        this.openModal('bookMachineModal');
    },

    async submitMachineBooking(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('bookMachineModalAlert');

        const machineId = form.machineId.value;
        const startDate = form.startDate.value;
        const endDate = form.endDate.value;
        const projectName = form.projectName.value.trim();
        const deliveryLocation = form.deliveryLocation.value.trim();
        const remarks = form.remarks.value.trim();

        const payload = {
            startDate,
            endDate,
            projectName,
            deliveryLocation,
            remarks
        };

        UI.setLoading('submitBookingBtn', true, 'Confirm Booking Request');

        try {
            await AuthService.apiFetch(`/machinery/${machineId}/bookings`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            UI.showAlert('bookMachineModalAlert', 'Machinery booking submitted successfully!', 'success');
            form.reset();
            setTimeout(() => {
                ContractorApp.closeModal('bookMachineModal');
                ContractorApp.loadMachineryBookings();
            }, 600);

        } catch (err) {
            UI.setLoading('submitBookingBtn', false, 'Confirm Booking Request');
            UI.showAlert('bookMachineModalAlert', err.message || 'Booking request failed', 'danger');
        }
    },

    async cancelMachineBooking(bookingId) {
        if (!confirm('Are you sure you want to cancel this machinery booking?')) return;
        try {
            await AuthService.apiFetch(`/machinery/bookings/${bookingId}/cancel`, {
                method: 'PUT',
                body: JSON.stringify({ reason: 'Contractor requested cancellation' })
            });
            this.loadMachineryBookings();
        } catch (e) {
            alert(e.message || 'Could not cancel booking');
        }
    },

    /* =========================================================================
       5. FIND MATERIALS SECTION
       ========================================================================= */
    async loadMaterials() {
        const category = document.getElementById('materialsCategoryFilter').value;
        const search = document.getElementById('materialsSearchInput').value.trim();

        const params = new URLSearchParams();
        if (category) params.append('category', category);
        if (search) params.append('search', search);

        const container = document.getElementById('materialsMarketGrid');
        container.innerHTML = '<div class="empty-state-box"><span class="spinner"></span> Loading raw materials catalog...</div>';

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const list = await AuthService.apiFetch(`/materials${query}`);

            if (!list || list.length === 0) {
                container.innerHTML = '<div class="empty-state-box"><div class="empty-state-title">No materials found</div><div class="empty-state-desc">Try clearing category or search parameters.</div></div>';
            } else {
                container.innerHTML = list.map(m => `
                    <div class="market-card">
                        <div>
                            <div class="market-card-top">
                                <div>
                                    <div class="market-card-title">${this.escape(m.name || 'Building Material')}</div>
                                    <div class="market-card-subtitle">${this.escape(m.category || 'MATERIALS')} &bull; ${this.escape(m.city || m.location || 'Location')}</div>
                                </div>
                                <span class="status-badge ${(m.availability || 'in_stock').toLowerCase()}">${m.availability || 'IN_STOCK'}</span>
                            </div>
                            <div class="market-specs">
                                <div class="market-spec-item">
                                    <span>Unit / UOM:</span>
                                    <span>${this.escape(m.unit || 'Ton / Bag / M3')}</span>
                                </div>
                                <div class="market-spec-item">
                                    <span>Supplier:</span>
                                    <span>${this.escape(m.supplierName || 'Verified Supplier')}</span>
                                </div>
                                <div class="market-spec-item">
                                    <span>Available Quantity:</span>
                                    <span>${Number(m.quantityAvailable || 0).toLocaleString()} ${this.escape(m.unit || 'units')}</span>
                                </div>
                            </div>
                        </div>
                        <div class="market-card-bottom">
                            <div class="market-price">
                                $${m.pricePerUnit || 50} <span>/ ${this.escape(m.unit || 'unit')}</span>
                            </div>
                            <button class="btn btn-primary" style="padding: 0.45rem 1rem; font-size: 0.85rem;" onclick="ContractorApp.openOrderMaterialModal(${m.id}, '${this.escape(m.name || 'Material')}', '${this.escape(m.unit || 'unit')}', ${m.pricePerUnit || 50})">
                                Order Material
                            </button>
                        </div>
                    </div>
                `).join('');
            }

            await this.loadMaterialOrders();

        } catch (e) {
            console.error('Error loading materials:', e);
            container.innerHTML = '<div class="empty-state-box" style="color:var(--danger);">Failed to load material catalog.</div>';
        }
    },

    async loadMaterialOrders() {
        const tbody = document.getElementById('materialOrdersTable');
        if (!tbody) return;

        try {
            const orders = await AuthService.apiFetch('/material-orders/my-orders');
            if (!orders || orders.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box">No material orders placed yet. Browse the catalog and create purchase orders.</td></tr>';
                return;
            }

            tbody.innerHTML = orders.map(o => `
                <tr>
                    <td>#${o.id}</td>
                    <td><strong>${this.escape(o.materialName || 'Material #' + o.materialId)}</strong></td>
                    <td>${o.quantity} ${this.escape(o.unit || 'units')}</td>
                    <td>$${Number(o.totalPrice || 0).toLocaleString()}</td>
                    <td>${this.formatDate(o.requestedDeliveryDate)}</td>
                    <td><span class="status-badge ${(o.status || '').toLowerCase()}">${o.status}</span></td>
                    <td>
                        ${o.status === 'PENDING' ? `
                            <button class="btn btn-outline" style="padding:0.25rem 0.6rem; font-size:0.75rem; color:var(--danger);" onclick="ContractorApp.cancelMaterialOrder(${o.id})">Cancel</button>
                        ` : '--'}
                    </td>
                </tr>
            `).join('');

        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box" style="color:var(--danger);">Error loading orders.</td></tr>';
        }
    },

    openOrderMaterialModal(materialId, materialName, unit, pricePerUnit) {
        const modal = document.getElementById('orderMaterialModal');
        modal.querySelector('#orderMaterialId').value = materialId;
        modal.querySelector('#orderMaterialName').innerText = materialName;
        modal.querySelector('#orderMaterialUnit').innerText = unit;
        modal.querySelector('#orderUnitPrice').innerText = `$${pricePerUnit}`;
        this.openModal('orderMaterialModal');
    },

    async submitMaterialOrder(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('orderMaterialModalAlert');

        const materialId = form.materialId.value;
        const projectId = form.projectId.value;
        const quantity = form.quantity.value;
        const deliveryAddress = form.deliveryAddress.value.trim();
        const requestedDeliveryDate = form.requestedDeliveryDate.value;
        const contractorNotes = form.contractorNotes.value.trim();

        const payload = {
            materialId: Number(materialId),
            projectId: projectId ? Number(projectId) : null,
            quantity: Number(quantity),
            deliveryAddress,
            requestedDeliveryDate: requestedDeliveryDate || null,
            contractorNotes
        };

        UI.setLoading('submitMaterialOrderBtn', true, 'Place Order');

        try {
            await AuthService.apiFetch(`/materials/${materialId}/orders`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            UI.showAlert('orderMaterialModalAlert', 'Material order placed successfully!', 'success');
            form.reset();
            setTimeout(() => {
                ContractorApp.closeModal('orderMaterialModal');
                ContractorApp.loadMaterialOrders();
            }, 600);

        } catch (err) {
            UI.setLoading('submitMaterialOrderBtn', false, 'Place Order');
            UI.showAlert('orderMaterialModalAlert', err.message || 'Failed to place order', 'danger');
        }
    },

    async cancelMaterialOrder(orderId) {
        if (!confirm('Are you sure you want to cancel this material order?')) return;
        try {
            await AuthService.apiFetch(`/material-orders/${orderId}/cancel`, {
                method: 'PUT',
                body: JSON.stringify({ reason: 'Contractor cancelled order' })
            });
            this.loadMaterialOrders();
        } catch (e) {
            alert(e.message || 'Could not cancel order');
        }
    },

    /* =========================================================================
       6. TENDERS SECTION
       ========================================================================= */
    async loadTenders() {
        const search = document.getElementById('tenderSearchInput').value.trim();
        const status = document.getElementById('tenderStatusFilter').value;

        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (status) params.append('status', status);

        const tbody = document.getElementById('tendersTableBody');
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box"><span class="spinner"></span> Fetching active tender notices...</td></tr>';

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const tenders = await AuthService.apiFetch(`/tenders${query}`);

            if (!tenders || tenders.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box">No tenders match your filter.</td></tr>';
            } else {
                tbody.innerHTML = tenders.map(t => `
                    <tr>
                        <td><strong>${this.escape(t.tenderNumber || 'TND-' + t.id)}</strong></td>
                        <td><strong>${this.escape(t.title)}</strong><br><small style="color:var(--text-muted);">${this.escape(t.department || 'Govt/Corporate')}</small></td>
                        <td>${this.escape(t.location || 'Pan-India')}</td>
                        <td>$${Number(t.estimatedValue || 0).toLocaleString()}</td>
                        <td>${this.formatDate(t.closingDate)}</td>
                        <td><span class="status-badge ${(t.status || '').toLowerCase()}">${t.status}</span></td>
                        <td>
                            ${t.status === 'OPEN' ? `
                                <button class="btn btn-primary" style="padding:0.35rem 0.8rem; font-size:0.8rem;" onclick="ContractorApp.openApplyTenderModal(${t.id}, '${this.escape(t.title)}', '${this.escape(t.tenderNumber)}')">Apply</button>
                            ` : `
                                <span style="font-size:0.8rem; color:var(--text-muted);">${t.status}</span>
                            `}
                        </td>
                    </tr>
                `).join('');
            }

            await this.loadMyTenderApplications();

        } catch (e) {
            console.error('Error loading tenders:', e);
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box" style="color:var(--danger);">Error loading tenders.</td></tr>';
        }
    },

    async loadMyTenderApplications() {
        const tbody = document.getElementById('myTenderApplicationsTable');
        if (!tbody) return;

        try {
            const apps = await AuthService.apiFetch('/tenders/applications/my-applications');
            if (!apps || apps.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="empty-state-box">No submitted tender applications found. Apply for open tenders to bid.</td></tr>';
                return;
            }

            tbody.innerHTML = apps.map(a => `
                <tr>
                    <td>#${a.id}</td>
                    <td><strong>${this.escape(a.tenderTitle || 'Tender #' + a.tenderId)}</strong></td>
                    <td>$${Number(a.bidAmount || 0).toLocaleString()}</td>
                    <td>${a.proposedDurationDays || '--'} days</td>
                    <td>${this.formatDate(a.submissionDate || a.createdAt)}</td>
                    <td><span class="status-badge ${(a.status || '').toLowerCase()}">${a.status}</span></td>
                </tr>
            `).join('');

        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="6" class="empty-state-box" style="color:var(--danger);">Error loading tender applications.</td></tr>';
        }
    },

    openApplyTenderModal(tenderId, tenderTitle, tenderNumber) {
        const modal = document.getElementById('applyTenderModal');
        modal.querySelector('#applyTenderId').value = tenderId;
        modal.querySelector('#applyTenderTitle').innerText = tenderTitle;
        modal.querySelector('#applyTenderNumber').innerText = tenderNumber;
        this.openModal('applyTenderModal');
    },

    async submitTenderApplication(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('applyTenderModalAlert');

        const tenderId = form.tenderId.value;
        const bidAmount = form.bidAmount.value;
        const proposedDurationDays = form.proposedDurationDays.value;
        const coverLetter = form.coverLetter.value.trim();

        const payload = {
            bidAmount: Number(bidAmount),
            proposedDurationDays: proposedDurationDays ? Number(proposedDurationDays) : null,
            coverLetter,
            documents: []
        };

        UI.setLoading('submitTenderBidBtn', true, 'Submit Tender Bid');

        try {
            await AuthService.apiFetch(`/tenders/${tenderId}/apply`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            UI.showAlert('applyTenderModalAlert', 'Tender application submitted successfully!', 'success');
            form.reset();
            setTimeout(() => {
                ContractorApp.closeModal('applyTenderModal');
                ContractorApp.loadMyTenderApplications();
            }, 600);

        } catch (err) {
            UI.setLoading('submitTenderBidBtn', false, 'Submit Tender Bid');
            UI.showAlert('applyTenderModalAlert', err.message || 'Application submission failed', 'danger');
        }
    },

    /* =========================================================================
       7. DAILY WORK REPORTS SECTION
       ========================================================================= */
    async loadDailyReports() {
        const projectSelect = document.getElementById('reportProjectFilter');
        const dateInput = document.getElementById('reportDateFilter');

        const projectId = projectSelect ? projectSelect.value : '';
        const date = dateInput ? dateInput.value : '';

        const params = new URLSearchParams();
        if (projectId) params.append('projectId', projectId);
        if (date) params.append('date', date);

        const tbody = document.getElementById('dailyReportsTableBody');
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state-box"><span class="spinner"></span> Loading daily site reports...</td></tr>';

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const reports = await AuthService.apiFetch(`/daily-reports${query}`);

            if (!reports || reports.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" class="empty-state-box">No reports found for selected filter. Click "+ Submit Daily Report" to log site activity.</td></tr>';
                return;
            }

            tbody.innerHTML = reports.map(r => `
                <tr>
                    <td><strong>${this.formatDate(r.date)}</strong></td>
                    <td>${this.escape(r.projectName || 'Project #' + r.projectId)}</td>
                    <td>${this.escape(r.workDescription || '')}</td>
                    <td>${r.numberOfWorkers || 0}</td>
                    <td>${r.workingHours || 8} hrs</td>
                    <td>${r.progressPercentage || 0}%</td>
                    <td>$${Number(r.expenses || 0).toLocaleString()}</td>
                    <td><small style="color:${r.issuesOrDelays ? 'var(--danger)' : 'var(--text-muted)'};">${this.escape(r.issuesOrDelays || 'None')}</small></td>
                </tr>
            `).join('');

        } catch (e) {
            console.error('Error loading daily reports:', e);
            tbody.innerHTML = '<tr><td colspan="8" class="empty-state-box" style="color:var(--danger);">Error loading daily reports.</td></tr>';
        }
    },

    async submitDailyReport(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('newDailyReportModalAlert');
        UI.clearFieldErrors(form);

        const projectId = form.projectId.value;
        const date = form.date.value;
        const workDescription = form.workDescription.value.trim();
        const numberOfWorkers = form.numberOfWorkers.value;
        const machineryUsed = form.machineryUsed.value.trim();
        const materialsUsed = form.materialsUsed.value.trim();
        const quantityCompleted = form.quantityCompleted.value.trim();
        const workingHours = form.workingHours.value || 8;
        const progressPercentage = form.progressPercentage.value || 0;
        const expenses = form.expenses.value || 0;
        const issuesOrDelays = form.issuesOrDelays.value.trim();
        const remarks = form.remarks.value.trim();

        if (!projectId) {
            UI.showAlert('newDailyReportModalAlert', 'Please select a project', 'danger');
            return;
        }

        const payload = {
            projectId: Number(projectId),
            date,
            workDescription,
            numberOfWorkers: Number(numberOfWorkers),
            machineryUsed,
            materialsUsed,
            quantityCompleted,
            workingHours: Number(workingHours),
            progressPercentage: Number(progressPercentage),
            expenses: Number(expenses),
            issuesOrDelays,
            remarks,
            images: []
        };

        UI.setLoading('submitDailyReportBtn', true, 'Submit Daily Report');

        try {
            await AuthService.apiFetch('/daily-reports', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            UI.showAlert('newDailyReportModalAlert', 'Daily work report saved successfully!', 'success');
            form.reset();
            setTimeout(() => {
                ContractorApp.closeModal('newDailyReportModal');
                ContractorApp.loadDailyReports();
            }, 600);

        } catch (err) {
            UI.setLoading('submitDailyReportBtn', false, 'Submit Daily Report');
            if (err.validationErrors) {
                Object.keys(err.validationErrors).forEach(field => {
                    const el = form[field];
                    if (el) UI.showFieldError(el, err.validationErrors[field]);
                });
            } else {
                UI.showAlert('newDailyReportModalAlert', err.message || 'Failed to submit report', 'danger');
            }
        }
    },

    /* =========================================================================
       8. ATTENDANCE SECTION
       ========================================================================= */
    async loadAttendance() {
        const projectSelect = document.getElementById('attendanceProjectFilter');
        const dateInput = document.getElementById('attendanceDateFilter');
        const statusSelect = document.getElementById('attendanceStatusFilter');

        const projectId = projectSelect ? projectSelect.value : '';
        const date = dateInput ? dateInput.value : '';
        const status = statusSelect ? statusSelect.value : '';

        const params = new URLSearchParams();
        if (projectId) params.append('projectId', projectId);
        if (date) params.append('date', date);
        if (status) params.append('status', status);

        const tbody = document.getElementById('attendanceTableBody');
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box"><span class="spinner"></span> Loading attendance logs...</td></tr>';

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const history = await AuthService.apiFetch(`/labor-attendance${query}`);

            // If a specific project is selected, load real summary metrics
            if (projectId) {
                try {
                    const summary = await AuthService.apiFetch(`/labor-attendance/project/${projectId}/summary${date ? '?date=' + date : ''}`);
                    document.getElementById('attTotalRecords').innerText = summary.totalRecords || 0;
                    document.getElementById('attPresent').innerText = summary.presentCount || 0;
                    document.getElementById('attAbsent').innerText = summary.absentCount || 0;
                    document.getElementById('attHalfDay').innerText = summary.halfDayCount || 0;
                    document.getElementById('attOvertime').innerText = summary.overtimeCount || 0;
                    document.getElementById('attTotalHours').innerText = summary.totalHours ? `${summary.totalHours} hrs` : '0 hrs';
                } catch (sumErr) {
                    console.warn('Could not load attendance summary:', sumErr);
                }
            } else {
                // Compute aggregated metrics from current history list
                const total = history ? history.length : 0;
                const present = history ? history.filter(h => h.status === 'PRESENT').length : 0;
                const absent = history ? history.filter(h => h.status === 'ABSENT').length : 0;
                const halfDay = history ? history.filter(h => h.status === 'HALF_DAY').length : 0;
                const overtime = history ? history.filter(h => h.status === 'OVERTIME').length : 0;
                const hours = history ? history.reduce((acc, h) => acc + (h.workingHours || 0), 0) : 0;

                document.getElementById('attTotalRecords').innerText = total;
                document.getElementById('attPresent').innerText = present;
                document.getElementById('attAbsent').innerText = absent;
                document.getElementById('attHalfDay').innerText = halfDay;
                document.getElementById('attOvertime').innerText = overtime;
                document.getElementById('attTotalHours').innerText = `${hours} hrs`;
            }

            if (!history || history.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box">No attendance records found. Click "+ Mark Attendance" to record laborer presence.</td></tr>';
                return;
            }

            tbody.innerHTML = history.map(h => `
                <tr>
                    <td>${this.formatDate(h.date)}</td>
                    <td><strong>${this.escape(h.laborerName || 'Laborer #' + h.laborerId)}</strong></td>
                    <td>${this.escape(h.projectName || 'Project #' + h.projectId)}</td>
                    <td>${h.workingHours || 8} hrs</td>
                    <td><span class="status-badge ${(h.status || '').toLowerCase()}">${h.status}</span></td>
                    <td><small>${this.escape(h.remarks || '--')}</small></td>
                    <td>
                        <button class="btn btn-outline" style="padding:0.25rem 0.55rem; font-size:0.75rem; color:var(--danger);" onclick="ContractorApp.deleteAttendance(${h.id})">Delete</button>
                    </td>
                </tr>
            `).join('');

        } catch (e) {
            console.error('Error loading attendance:', e);
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state-box" style="color:var(--danger);">Error loading attendance history.</td></tr>';
        }
    },

    async submitAttendance(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('markAttendanceModalAlert');

        const laborerId = form.laborerId.value;
        const projectId = form.projectId.value;
        const date = form.date.value;
        const status = form.status.value;
        const workingHours = form.workingHours.value || 8;
        const remarks = form.remarks.value.trim();

        if (!projectId) {
            UI.showAlert('markAttendanceModalAlert', 'Please select a project', 'danger');
            return;
        }

        const payload = {
            laborerId: Number(laborerId),
            projectId: Number(projectId),
            date,
            status,
            workingHours: Number(workingHours),
            remarks
        };

        UI.setLoading('submitAttendanceBtn', true, 'Record Attendance');

        try {
            await AuthService.apiFetch('/labor-attendance', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            UI.showAlert('markAttendanceModalAlert', 'Attendance recorded successfully!', 'success');
            form.reset();
            setTimeout(() => {
                ContractorApp.closeModal('markAttendanceModal');
                ContractorApp.loadAttendance();
            }, 600);

        } catch (err) {
            UI.setLoading('submitAttendanceBtn', false, 'Record Attendance');
            UI.showAlert('markAttendanceModalAlert', err.message || 'Failed to record attendance', 'danger');
        }
    },

    async deleteAttendance(id) {
        if (!confirm('Are you sure you want to delete this attendance entry?')) return;
        try {
            await AuthService.apiFetch(`/labor-attendance/${id}`, { method: 'DELETE' });
            this.loadAttendance();
        } catch (e) {
            alert(e.message || 'Could not delete attendance record');
        }
    },

    /* =========================================================================
       9. NOTIFICATIONS SECTION
       ========================================================================= */
    async loadNotifications() {
        const unreadFilter = document.getElementById('notifFilterUnreadOnly').checked;
        const typeFilter = document.getElementById('notifTypeFilter').value;

        const params = new URLSearchParams();
        if (unreadFilter) params.append('unreadOnly', 'true');
        if (typeFilter) params.append('type', typeFilter);

        const container = document.getElementById('notificationsFeedList');
        container.innerHTML = '<div class="empty-state-box"><span class="spinner"></span> Loading notifications...</div>';

        try {
            const query = params.toString() ? `?${params.toString()}` : '';
            const notifs = await AuthService.apiFetch(`/notifications${query}`);

            if (!notifs || notifs.length === 0) {
                container.innerHTML = '<div class="empty-state-box"><div class="empty-state-title">No notifications</div><div class="empty-state-desc">You are all caught up!</div></div>';
                return;
            }

            container.innerHTML = notifs.map(n => `
                <div class="notif-row ${!n.read ? 'unread' : ''}" id="notif-row-${n.id}">
                    <div class="notif-icon ${this.getNotifColor(n.type)}">
                        <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                    </div>
                    <div class="notif-body">
                        <div class="notif-title">
                            <span>${this.escape(n.title)} <small style="font-size:0.75rem; color:var(--text-muted); font-weight:normal;">(${n.type})</small></span>
                            <span class="notif-time">${this.formatDate(n.createdAt)}</span>
                        </div>
                        <div class="notif-message">${this.escape(n.message)}</div>
                    </div>
                    ${!n.read ? `
                        <button class="btn btn-outline" style="padding:0.25rem 0.55rem; font-size:0.75rem;" onclick="ContractorApp.markNotificationRead(${n.id})">Mark read</button>
                    ` : ''}
                </div>
            `).join('');

        } catch (e) {
            console.error('Error loading notifications:', e);
            container.innerHTML = '<div class="empty-state-box" style="color:var(--danger);">Error loading notifications.</div>';
        }
    },

    async markNotificationRead(id) {
        try {
            await AuthService.apiFetch(`/notifications/${id}/read`, { method: 'PATCH' });
            const row = document.getElementById(`notif-row-${id}`);
            if (row) row.classList.remove('unread');
            this.updateNotificationBadge();
        } catch (e) {
            console.warn('Error marking notification read:', e);
        }
    },

    async markAllNotificationsRead() {
        try {
            await AuthService.apiFetch('/notifications/mark-all-read', { method: 'PATCH' });
            this.loadNotifications();
            this.updateNotificationBadge();
        } catch (e) {
            alert(e.message || 'Error marking all notifications read');
        }
    },

    getNotifColor(type) {
        if (!type) return 'icon-blue';
        if (type.includes('ACCEPTANCE')) return 'icon-emerald';
        if (type.includes('REJECTION')) return 'icon-rose';
        if (type.includes('REQUEST') || type.includes('BOOKING')) return 'icon-amber';
        if (type.includes('ADMIN')) return 'icon-purple';
        return 'icon-blue';
    },

    /* =========================================================================
       10. PROFILE SECTION
       ========================================================================= */
    async loadProfileForm() {
        const form = document.getElementById('contractorProfileForm');
        UI.clearAlert('profileAlert');

        try {
            const profile = await AuthService.apiFetch('/contractors/profile');
            this.contractorProfile = profile;

            // Fill form inputs
            form.companyName.value = profile.companyName || '';
            form.licenseNumber.value = profile.licenseNumber || '';
            form.yearsOfExperience.value = profile.yearsOfExperience || 0;
            form.specialization.value = profile.specialization || '';
            form.phone.value = profile.phone || (this.currentUser ? this.currentUser.phoneNumber : '') || '';
            form.address.value = profile.address || '';
            form.city.value = profile.city || '';
            form.state.value = profile.state || '';
            form.postalCode.value = profile.postalCode || '';
            form.companyDescription.value = profile.companyDescription || '';

            // Update profile badge and metrics
            document.getElementById('profileCompanyNameHeader').innerText = profile.companyName || 'Contractor Profile';
            document.getElementById('profileRatingText').innerText = `★ ${profile.rating || '5.0'} / 5.0`;
            this.updateVerificationBadge(profile.verificationStatus || 'PENDING');

            document.getElementById('profileSaveBtn').innerText = 'Update Profile';
            form.dataset.method = 'PUT';

        } catch (e) {
            console.log('Contractor profile does not exist yet; opening create form.');
            this.contractorProfile = null;
            form.reset();
            if (this.currentUser && this.currentUser.phoneNumber) {
                form.phone.value = this.currentUser.phoneNumber;
            }
            document.getElementById('profileSaveBtn').innerText = 'Create Business Profile';
            form.dataset.method = 'POST';
        }
    },

    async saveProfile(e) {
        e.preventDefault();
        const form = e.target;
        UI.clearAlert('profileAlert');
        UI.clearFieldErrors(form);

        const method = form.dataset.method || 'POST';
        const payload = {
            companyName: form.companyName.value.trim(),
            licenseNumber: form.licenseNumber.value.trim(),
            yearsOfExperience: Number(form.yearsOfExperience.value || 0),
            specialization: form.specialization.value.trim(),
            phone: form.phone.value.trim(),
            address: form.address.value.trim(),
            city: form.city.value.trim(),
            state: form.state.value.trim(),
            postalCode: form.postalCode.value.trim(),
            companyDescription: form.companyDescription.value.trim(),
            profileImage: ''
        };

        UI.setLoading('profileSaveBtn', true, method === 'PUT' ? 'Update Profile' : 'Create Business Profile');

        try {
            const result = await AuthService.apiFetch('/contractors/profile', {
                method,
                body: JSON.stringify(payload)
            });

            this.contractorProfile = result;
            UI.showAlert('profileAlert', `Company profile ${method === 'PUT' ? 'updated' : 'created'} successfully!`, 'success');
            this.updateVerificationBadge(result.verificationStatus || 'PENDING');
            form.dataset.method = 'PUT';
            document.getElementById('profileSaveBtn').innerText = 'Update Profile';
            UI.setLoading('profileSaveBtn', false, 'Update Profile');

        } catch (err) {
            UI.setLoading('profileSaveBtn', false, method === 'PUT' ? 'Update Profile' : 'Create Business Profile');
            if (err.validationErrors) {
                Object.keys(err.validationErrors).forEach(field => {
                    const el = form[field];
                    if (el) UI.showFieldError(el, err.validationErrors[field]);
                });
            } else {
                UI.showAlert('profileAlert', err.message || 'Failed to save profile', 'danger');
            }
        }
    },

    /* =========================================================================
       HELPERS & FORMATTERS
       ========================================================================= */
    escape(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    formatDate(dateStr) {
        if (!dateStr) return '--';
        try {
            const d = new Date(dateStr);
            if (isNaN(d.getTime())) return dateStr;
            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        } catch (e) {
            return dateStr;
        }
    }
};

// Global export for HTML inline triggers
window.ContractorApp = ContractorApp;

document.addEventListener('DOMContentLoaded', () => {
    ContractorApp.init();
});
