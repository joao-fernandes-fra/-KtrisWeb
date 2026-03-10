(function () {

    const DEFAULTS = {
        boardRows: 20, boardCols: 10, bufferZone: 4,
        gravityBase: 1000.0, gravityIncrement: 0.8, levelCap: 99,
        shouldCollapseOnFreeze: true,
        goalType: 'NONE', goalValue: 0.0,
        dasDelay: 133.0, arrDelay: 0.0, entryDelay: 200.0,
        lockDelay: 500.0, softDropDelay: 0.5, maxLockResets: 15,
        isHoldEnabled: true, isGhostEnabled: true,
        isSpinEnabled: true, is180Enabled: false, previewSize: 5,
    };

    window.ktrisConfig = Object.assign({}, DEFAULTS);

    const num = id => parseFloat(document.getElementById(id).value);
    const int = id => parseInt(document.getElementById(id).value, 10);
    const bool = id => document.getElementById(id).checked;
    const sel = id => document.getElementById(id).value;

    function bindRange(inputId, labelId) {
        const input = document.getElementById(inputId);
        const label = document.getElementById(labelId);
        const sync = () => label.textContent = input.value;
        input.addEventListener('input', sync);
        sync();
    }

    function collectConfig() {
        window.ktrisConfig = {
            boardRows: int('cfg-boardRows'),
            boardCols: int('cfg-boardCols'),
            bufferZone: int('cfg-bufferZone'),
            gravityBase: num('cfg-gravityBase'),
            gravityIncrement: num('cfg-gravityIncrement'),
            levelCap: int('cfg-levelCap'),
            shouldCollapseOnFreeze: bool('cfg-collapseOnFreeze'),
            goalType: sel('cfg-goalType'),
            goalValue: num('cfg-goalValue'),
            dasDelay: num('cfg-dasDelay'),
            arrDelay: num('cfg-arrDelay'),
            entryDelay: num('cfg-entryDelay'),
            lockDelay: num('cfg-lockDelay'),
            softDropDelay: num('cfg-softDropDelay'),
            maxLockResets: int('cfg-maxLockResets'),
            isHoldEnabled: bool('cfg-holdEnabled'),
            isGhostEnabled: bool('cfg-ghostEnabled'),
            isSpinEnabled: bool('cfg-spinEnabled'),
            is180Enabled: bool('cfg-180Enabled'),
            previewSize: int('cfg-previewSize'),
        };
    }

    function updateGoalRow() {
        const type = sel('cfg-goalType');
        const row = document.getElementById('goal-value-row');
        const lbl = document.getElementById('goal-value-label');
        const none = type === 'NONE';
        row.style.opacity = none ? '0.3' : '1';
        row.style.pointerEvents = none ? 'none' : 'auto';
        if (!none) lbl.textContent = type === 'LINES' ? 'Line Goal' : 'Time Goal (s)';
    }

    const overlay = () => document.getElementById('config-overlay');
    const btn = () => document.getElementById('nav-settings');

    function openSettings() {
        const el = overlay();
        el.classList.remove('hidden', 'closing');
        el.setAttribute('aria-hidden', 'false');
        btn().classList.add('active');
    }

    function closeSettings() {
        const el = overlay();
        el.classList.add('closing');
        el.setAttribute('aria-hidden', 'true');
        btn().classList.remove('active');
        el.addEventListener('animationend', () => {
            el.classList.add('hidden');
            el.classList.remove('closing');
        }, {once: true});
    }

    function toggleSettings() {
        overlay().classList.contains('hidden') ? openSettings() : closeSettings();
    }

    document.addEventListener('DOMContentLoaded', () => {
        bindRange('cfg-gravityIncrement', 'lbl-gravityIncrement');
        bindRange('cfg-previewSize', 'lbl-previewSize');
        bindRange('cfg-maxLockResets', 'lbl-maxLockResets');
        document.getElementById('cfg-goalType').addEventListener('change', updateGoalRow);
        updateGoalRow();
        document.getElementById('nav-settings').addEventListener('click', toggleSettings);
        document.getElementById('cfg-close').addEventListener('click', closeSettings);
        overlay().addEventListener('click', e => {
            if (e.target === overlay()) closeSettings();
        });
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape' && !overlay().classList.contains('hidden')) closeSettings();
        });
        document.getElementById('cfg-reset').addEventListener('click', () => location.reload());
        document.getElementById('cfg-apply').addEventListener('click', () => {
            collectConfig();
            closeSettings();
            if (typeof window.ktrisRestart === 'function') {
                window.ktrisRestart();
            }
        });
    });
})();