<%@ page contentType="text/html;charset=UTF-8" %>
<%--
    footer.jsp
    Closes main content, adds footer and scripts
    Location: /WEB-INF/views/common/footer.jsp
--%>

</div><!-- end .container -->
</main><!-- end .main-content -->

<!-- ═══════════════════════════════════════════════════════════════
     FOOTER
     ═══════════════════════════════════════════════════════════════ -->
<footer class="app-footer no-print">
    <div class="footer-inner">
        <div class="footer-brand">
            <span>🦷</span>
            <strong>Sunrise Dental Clinic</strong>
            <span class="footer-sep">—</span>
            <span>Colombo, Sri Lanka</span>
        </div>
        <div class="footer-copy">
            &copy; <%= java.time.Year.now().getValue() %>
            Sunrise Dental Clinic. All rights reserved.
        </div>
    </div>
</footer>

<!-- ═══════════════════════════════════════════════════════════════
     TOAST NOTIFICATIONS
     ═══════════════════════════════════════════════════════════════ -->
<% if (request.getAttribute("successMsg") != null) { %>
<div class="toast toast-success" id="globalToast">
    <span class="toast-icon">✅</span>
    <span class="toast-msg">${successMsg}</span>
    <button class="toast-close"
            onclick="document.getElementById('globalToast').remove()">✕</button>
</div>
<% } %>

<% if (request.getAttribute("errorMsg") != null) { %>
<div class="toast toast-error" id="globalToast">
    <span class="toast-icon">❌</span>
    <span class="toast-msg">${errorMsg}</span>
    <button class="toast-close"
            onclick="document.getElementById('globalToast').remove()">✕</button>
</div>
<% } %>

<!-- JavaScript -->
<script src="${pageContext.request.contextPath}/assets/js/app.js"></script>

<script>
    // ── Sidebar Toggle Functions ─────────────────────────────
    function toggleSidebar() {
        const sidebar    = document.getElementById('sidebar');
        const mainContent = document.getElementById('mainContent');
        const hamburger  = document.getElementById('sidebarToggle');
        const footer     = document.querySelector('.app-footer');
        const overlay    = document.getElementById('sidebarOverlay');

        if (window.innerWidth <= 768) {
            // Mobile: slide in/out
            sidebar.classList.toggle('mobile-open');
            overlay.classList.toggle('show');
        } else {
            // Desktop: collapse/expand
            sidebar.classList.toggle('collapsed');
            mainContent.classList.toggle('full-width');
            hamburger.classList.toggle('active');
            if (footer) footer.classList.toggle('full-width');

            // Save state
            const isCollapsed = sidebar.classList.contains('collapsed');
            localStorage.setItem('sidebarCollapsed', isCollapsed);
        }
    }

    function closeSidebar() {
        const sidebar = document.getElementById('sidebar');
        const overlay = document.getElementById('sidebarOverlay');

        sidebar.classList.remove('mobile-open');
        overlay.classList.remove('show');
    }

    // Restore sidebar state on page load
    document.addEventListener('DOMContentLoaded', function() {
        const wasCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';

        if (wasCollapsed && window.innerWidth > 768) {
            document.getElementById('sidebar').classList.add('collapsed');
            document.getElementById('mainContent').classList.add('full-width');
            document.getElementById('sidebarToggle').classList.add('active');

            const footer = document.querySelector('.app-footer');
            if (footer) footer.classList.add('full-width');
        }
    });

    // Close mobile sidebar when clicking a link
    document.querySelectorAll('.sidebar-link').forEach(function(link) {
        link.addEventListener('click', function() {
            if (window.innerWidth <= 768) {
                closeSidebar();
            }
        });
    });

    // Close mobile sidebar on window resize
    window.addEventListener('resize', function() {
        if (window.innerWidth > 768) {
            document.getElementById('sidebar').classList.remove('mobile-open');
            document.getElementById('sidebarOverlay').classList.remove('show');
        }
    });
</script>

</body>
</html>