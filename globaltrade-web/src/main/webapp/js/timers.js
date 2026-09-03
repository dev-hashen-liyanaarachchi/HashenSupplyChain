document.addEventListener('DOMContentLoaded', () => {
    const timerGridContainer = document.getElementById('timerGridContainer');
    const alertBox = document.getElementById('alertBox');
    const btnRefreshTimers = document.getElementById('btnRefreshTimers');

    const API_BASE = window.location.pathname.includes('/globaltrade-web') ? '/globaltrade-web/api' : '/api';

    function showAlert(message, type = 'success') {
        alertBox.className = `alert alert-${type}`;
        alertBox.innerHTML = `
            <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                    d="${type === 'error' ? 'M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' : 'M5 13l4 4L19 7'}"/>
            </svg>
            <span>${message}</span>
        `;
        alertBox.style.display = 'flex';
    }

    function hideAlert() {
        alertBox.style.display = 'none';
    }

    // Load EJB Timers Status
    async function loadTimers() {
        try {
            const res = await fetch(`${API_BASE}/timers/status`);
            const timers = await res.json();

            if (res.ok && Array.isArray(timers)) {
                timerGridContainer.innerHTML = timers.map(t => {
                    const isDeclarative = t.type.includes('Declarative');
                    const isPersistent = t.persistence.includes('Persistent');

                    return `
                        <div class="timer-card">
                            <div>
                                <div style="display:flex; gap:6px; margin-bottom:10px; flex-wrap:wrap;">
                                    <span class="timer-badge ${isDeclarative ? 'badge-declarative' : 'badge-programmatic'}">${isDeclarative ? 'Declarative (@Schedule)' : 'Programmatic (TimerService)'}</span>
                                    <span class="timer-badge ${isPersistent ? 'badge-persistent' : 'badge-transient'}">${isPersistent ? 'Persistent (DB Backed)' : 'Transient (In-Memory)'}</span>
                                </div>
                                <h3 style="font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 6px;">${t.name}</h3>
                                <p style="font-size: 12px; color: #64748b; margin-bottom: 12px; line-height:1.5;">${t.description}</p>
                                
                                <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:10px; font-size:12px; margin-bottom:16px;">
                                    <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
                                        <span style="color:#64748b;">Schedule Cadence:</span>
                                        <strong style="color:#1e293b;">${t.schedule}</strong>
                                    </div>
                                    <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
                                        <span style="color:#64748b;">Execution Status:</span>
                                        <strong style="color:#059669;">● ${t.lastRunStatus}</strong>
                                    </div>
                                    <div style="display:flex; justify-content:space-between;">
                                        <span style="color:#64748b;">Executions Logged:</span>
                                        <strong style="color:#2563eb;">${t.executionCount} Executions</strong>
                                    </div>
                                </div>
                            </div>

                            <button type="button" class="btn-primary btn-trigger-timer" data-key="${t.key}" style="width:100%; margin-top:0; padding:8px 14px; font-size:12px; background: linear-gradient(135deg, #059669 0%, #047857 100%);">
                                ▶️ Execute Automated Job Now
                            </button>
                        </div>
                    `;
                }).join('');

                // Attach event handlers
                document.querySelectorAll('.btn-trigger-timer').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const key = btn.getAttribute('data-key');
                        triggerTimer(key);
                    });
                });
            } else {
                timerGridContainer.innerHTML = `<div style="grid-column:1/-1; text-align:center; color:var(--error-color);">Failed to load EJB timer statuses.</div>`;
            }
        } catch (err) {
            console.error('Error fetching timers:', err);
            timerGridContainer.innerHTML = `<div style="grid-column:1/-1; text-align:center; color:var(--error-color);">Network error fetching EJB timer statuses.</div>`;
        }
    }

    // Trigger Timer Manually
    async function triggerTimer(timerKey) {
        hideAlert();
        try {
            const res = await fetch(`${API_BASE}/timers/trigger/${timerKey}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'}
            });

            const data = await res.json();
            if (res.ok) {
                showAlert(data.message || 'EJB Timer Job executed successfully!', 'success');
                if (typeof window.triggerSystemToast === 'function') {
                    window.triggerSystemToast('EJB Automated Job Executed', data.message || 'Background process completed', 'SUCCESS');
                }
                loadTimers();
            } else {
                showAlert(data.error || 'Failed to trigger timer job.', 'error');
            }
        } catch (err) {
            showAlert('Network error triggering timer job.', 'error');
        }
    }

    btnRefreshTimers?.addEventListener('click', loadTimers);

    // Initial Load
    loadTimers();
});
