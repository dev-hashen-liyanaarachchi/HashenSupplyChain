document.addEventListener('DOMContentLoaded', () => {
    const API_BASE = window.location.pathname.includes('/globaltrade-web') ? '/globaltrade-web/api' : '/api';

    let notifications = [];
    let unreadCount = 0;

    // Inject CSS for Bell Icon, Drawer, and Toast Popups dynamically
    const styleElem = document.createElement('style');
    styleElem.innerHTML = `
        /* Floating Notification Bell Button */
        .notif-bell-container {
            position: relative;
            display: inline-flex;
            align-items: center;
        }

        .btn-notif-bell {
            background: #ffffff;
            border: 1.5px solid #e2e8f0;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            cursor: pointer;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            transition: all 0.2s ease;
        }

        .btn-notif-bell:hover {
            transform: scale(1.05);
            border-color: #2563eb;
            box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
        }

        .notif-badge-count {
            position: absolute;
            top: -4px;
            right: -4px;
            background: #ef4444;
            color: #ffffff;
            font-size: 10px;
            font-weight: 700;
            border-radius: 10px;
            padding: 2px 6px;
            border: 2px solid #ffffff;
            line-height: 1;
            box-shadow: 0 2px 4px rgba(239, 68, 68, 0.4);
            animation: pulse-badge 2s infinite;
        }

        @keyframes pulse-badge {
            0% { transform: scale(1); }
            50% { transform: scale(1.15); }
            100% { transform: scale(1); }
        }

        /* Slide-Out Notification Drawer */
        .notif-drawer-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background: rgba(15, 23, 42, 0.4);
            backdrop-filter: blur(4px);
            z-index: 99998;
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.3s ease;
        }

        .notif-drawer-overlay.active {
            opacity: 1;
            pointer-events: auto;
        }

        .notif-drawer {
            position: fixed;
            top: 0;
            right: -420px;
            width: 400px;
            max-width: 90vw;
            height: 100vh;
            background: #ffffff;
            box-shadow: -10px 0 30px rgba(0,0,0,0.15);
            z-index: 99999;
            transition: right 0.35s cubic-bezier(0.16, 1, 0.3, 1);
            display: flex;
            flex-direction: column;
        }

        .notif-drawer.active {
            right: 0;
        }

        .notif-drawer-header {
            padding: 20px;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #f8fafc;
        }

        .notif-drawer-title {
            font-size: 16px;
            font-weight: 700;
            color: #0f172a;
            display: flex;
            align-items: center;
            gap: 8px;
            margin: 0;
        }

        .notif-drawer-body {
            flex: 1;
            overflow-y: auto;
            padding: 16px;
        }

        .notif-item {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 14px;
            margin-bottom: 12px;
            transition: all 0.2s ease;
            position: relative;
        }

        .notif-item.unread {
            background: #f0f9ff;
            border-color: #bae6fd;
        }

        .notif-item:hover {
            border-color: #3b82f6;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }

        .notif-item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 6px;
        }

        .notif-cat-badge {
            font-size: 10px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 12px;
            text-transform: uppercase;
        }

        .cat-customs { background: #f3e8ff; color: #6b21a8; }
        .cat-inventory { background: #fee2e2; color: #991b1b; }
        .cat-logistics { background: #dbeafe; color: #1e40af; }
        .cat-vendor { background: #fef3c7; color: #92400e; }

        .notif-item-title {
            font-size: 13px;
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 4px;
        }

        .notif-item-msg {
            font-size: 12px;
            color: #475569;
            line-height: 1.4;
        }

        /* Toast Container */
        .toast-container {
            position: fixed;
            top: 24px;
            right: 24px;
            z-index: 100000;
            display: flex;
            flex-direction: column;
            gap: 10px;
            pointer-events: none;
        }

        .toast-card {
            pointer-events: auto;
            background: #ffffff;
            border-left: 4px solid #2563eb;
            box-shadow: 0 10px 25px rgba(0,0,0,0.15);
            border-radius: 10px;
            padding: 14px 18px;
            width: 340px;
            display: flex;
            align-items: flex-start;
            gap: 12px;
            animation: slide-in 0.35s ease forwards;
        }

        .toast-card.critical { border-left-color: #ef4444; }
        .toast-card.warning { border-left-color: #f59e0b; }
        .toast-card.success { border-left-color: #10b981; }

        @keyframes slide-in {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
    `;
    document.head.appendChild(styleElem);

    // Create Toast Container
    const toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container';
    document.body.appendChild(toastContainer);

    // Create Drawer Markup
    const drawerOverlay = document.createElement('div');
    drawerOverlay.className = 'notif-drawer-overlay';

    const drawer = document.createElement('div');
    drawer.className = 'notif-drawer';
    drawer.innerHTML = `
        <div class="notif-drawer-header">
            <h3 class="notif-drawer-title">🔔 System Notifications & Live Alerts</h3>
            <button type="button" id="btnCloseNotifDrawer" style="background:none; border:none; font-size:18px; cursor:pointer; color:#64748b;">✕</button>
        </div>
        <div style="padding:10px 16px; background:#ffffff; border-bottom:1px solid #e2e8f0; display:flex; justify-content:space-between; align-items:center; font-size:12px;">
            <span id="notifUnreadLabel" style="color:#64748b; font-weight:600;">0 Unread Alerts</span>
            <button type="button" id="btnClearNotifs" style="background:none; border:none; color:#ef4444; font-weight:600; cursor:pointer; font-size:11px;">🗑️ Clear All</button>
        </div>
        <div class="notif-drawer-body" id="notifListContainer">
            <div style="text-align:center; color:#94a3b8; padding:30px 0;">Loading notifications...</div>
        </div>
    `;
    document.body.appendChild(drawerOverlay);
    document.body.appendChild(drawer);

    // Attach Bell Icon into Header if brand-header or role-header exists
    function injectBellIcon() {
        const headerRight = document.querySelector('.brand-header > div') || document.querySelector('.role-header');
        if (headerRight && !document.getElementById('btnNotifBell')) {
            const bellWrap = document.createElement('div');
            bellWrap.className = 'notif-bell-container';
            bellWrap.innerHTML = `
                <button type="button" id="btnNotifBell" class="btn-notif-bell" title="System Notifications & Alerts">
                    🔔
                    <span id="notifBadgeCount" class="notif-badge-count" style="display:none;">0</span>
                </button>
            `;
            headerRight.appendChild(bellWrap);

            document.getElementById('btnNotifBell').addEventListener('click', () => {
                drawerOverlay.classList.add('active');
                drawer.classList.add('active');
            });
        }
    }

    drawerOverlay.addEventListener('click', () => {
        drawerOverlay.classList.remove('active');
        drawer.classList.remove('active');
    });

    document.getElementById('btnCloseNotifDrawer')?.addEventListener('click', () => {
        drawerOverlay.classList.remove('active');
        drawer.classList.remove('active');
    });

    document.getElementById('btnClearNotifs')?.addEventListener('click', async () => {
        try {
            await fetch(`${API_BASE}/notifications/clear`, {method: 'POST'});
            loadNotifications();
        } catch (err) {
        }
    });

    // Show Floating Toast Popup
    function showToast(title, message, severity = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast-card ${severity.toLowerCase()}`;

        let icon = '🔔';
        if (severity === 'CRITICAL') icon = '🚨';
        else if (severity === 'WARNING') icon = '⚠️';
        else if (severity === 'SUCCESS') icon = '✅';

        toast.innerHTML = `
            <div style="font-size:20px;">${icon}</div>
            <div style="flex:1;">
                <div style="font-size:13px; font-weight:700; color:#0f172a; margin-bottom:2px;">${title}</div>
                <div style="font-size:12px; color:#475569;">${message}</div>
            </div>
        `;

        toastContainer.appendChild(toast);
        setTimeout(() => {
            toast.style.transition = 'all 0.4s ease';
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 400);
        }, 4500);
    }

    // Fetch Notifications from Server
    async function loadNotifications() {
        try {
            const res = await fetch(`${API_BASE}/notifications`);
            if (res.ok) {
                const data = await res.json();
                notifications = data;
                unreadCount = notifications.filter(n => !n.readStatus).length;

                const notifBadgeCount = document.getElementById('notifBadgeCount');
                const notifUnreadLabel = document.getElementById('notifUnreadLabel');
                const notifListContainer = document.getElementById('notifListContainer');

                if (notifBadgeCount) {
                    if (unreadCount > 0) {
                        notifBadgeCount.textContent = unreadCount;
                        notifBadgeCount.style.display = 'block';
                    } else {
                        notifBadgeCount.style.display = 'none';
                    }
                }

                if (notifUnreadLabel) {
                    notifUnreadLabel.textContent = `${unreadCount} Unread Alerts`;
                }

                if (notifListContainer) {
                    if (notifications.length === 0) {
                        notifListContainer.innerHTML = `<div style="text-align:center; color:#94a3b8; padding:40px 0;">No system notifications logged yet.</div>`;
                        return;
                    }

                    notifListContainer.innerHTML = notifications.map(n => {
                        let catClass = 'cat-logistics';
                        if (n.category === 'CUSTOMS') catClass = 'cat-customs';
                        else if (n.category === 'INVENTORY') catClass = 'cat-inventory';
                        else if (n.category === 'VENDOR') catClass = 'cat-vendor';

                        return `
                            <div class="notif-item ${!n.readStatus ? 'unread' : ''}">
                                <div class="notif-item-header">
                                    <span class="notif-cat-badge ${catClass}">${n.category}</span>
                                    <small style="font-size:10px; color:#94a3b8;">${n.createdAt ? n.createdAt.substring(11, 16) : 'Now'}</small>
                                </div>
                                <div class="notif-item-title">${n.title}</div>
                                <div class="notif-item-msg">${n.message}</div>
                            </div>
                        `;
                    }).join('');
                }
            }
        } catch (err) {
            console.error('Error fetching notifications:', err);
        }
    }

    // Expose Global Toast Function
    window.triggerSystemToast = (title, msg, type = 'info') => {
        showToast(title, msg, type);
        loadNotifications();
    };

    // Initial Injection & Setup
    injectBellIcon();
    loadNotifications();

    // Poll for notifications every 6 seconds
    setInterval(loadNotifications, 6000);
});
