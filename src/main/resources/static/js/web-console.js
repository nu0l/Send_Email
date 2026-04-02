    function sameAppPath(absPath) {
        if (!absPath || typeof absPath !== 'string') return absPath;
        if (absPath.charAt(0) !== '/') return absPath;
        return absPath.replace(/^\/+/, '');
    }

    function isLoginPageUrl(url) {
        if (!url) return false;
        try {
            var p = new URL(url, window.location.origin).pathname;
            if (p === '/login.html' || p === '/login') return true;
            if (/\/login(\.html)?$/.test(p)) return true;
            if (p.indexOf('/login/') === 0) return true;
            return false;
        } catch (e) {
            return false;
        }
    }

    var paneDefs = [
        { id: 'config', title: '邮箱配置', desc: '多 SMTP 账户增删改查' },
        { id: 'proxy', title: '代理设置', desc: 'SOCKS5 等代理' },
        { id: 'convert', title: 'EML 转 HTML', desc: '上传 EML 下载 HTML' },
        { id: 'attachment', title: '附件管理', desc: '发送前上传附件' },
        { id: 'send', title: '普通发送', desc: 'HTML/文本正文' },
        { id: 'custom', title: '模板邮件', desc: 'HTML + CSV 变量' },
        { id: 'activity', title: '统计与记录', desc: '发送次数与审计日志' },
        { id: 'drill', title: '演练模板', desc: '内置模板与示例 CSV' }
    ];

    function setStatus(msg, ok) {
        if (typeof ok === 'undefined') ok = true;
        var el = document.getElementById('status');
        if (!el) return;
        var time = new Date().toLocaleTimeString();
        var prefix = '[' + time + '] ';
        el.innerText = prefix + msg + '\n\n' + el.innerText.replace(/^等待操作…\n\n?/, '');
        el.className = ok ? 'success' : 'error';
    }

    function clearLog() {
        var el = document.getElementById('status');
        el.innerText = '日志已清空。';
        el.className = 'success';
    }

    async function api(path, options) {
        options = options || {};
        var opts = Object.assign({ credentials: 'same-origin' }, options);
        var noRedirect = opts.noRedirect === true;
        delete opts.noRedirect;
        var headers = Object.assign({}, opts.headers || {});
        if (opts.body instanceof FormData) {
            delete headers['Content-Type'];
        }
        opts.headers = headers;
        var reqPath = sameAppPath(path);
        var res = await fetch(reqPath, opts);
        if (noRedirect) {
            if (res.status === 401 || res.status === 403) {
                return null;
            }
            if (res.redirected && isLoginPageUrl(res.url)) {
                return null;
            }
        } else {
            if (res.status === 401 || res.status === 403) {
                window.location.href = sameAppPath('/login');
                throw new Error('未登录或会话已过期，请重新登录');
            }
            if (res.redirected && isLoginPageUrl(res.url)) {
                window.location.href = sameAppPath('/login');
                throw new Error('未登录或会话已过期，请重新登录');
            }
        }
        var text = await res.text();
        var ct = (res.headers.get('Content-Type') || '');
        if (reqPath.indexOf('api/') === 0 && ct.indexOf('text/html') >= 0 && text.length > 0 && text.trim().charAt(0) === '<') {
            if (noRedirect) {
                return null;
            }
            window.location.href = sameAppPath('/login');
            throw new Error('未登录或会话已过期，请重新登录');
        }
        var parsed = null;
        try { parsed = JSON.parse(text); } catch (_) { /* ignore */ }

        if (!res.ok) {
            // 统一错误信息：优先从 ApiResponse 中取 message。
            if (parsed && typeof parsed === 'object' && parsed.message) {
                throw new Error(parsed.message);
            }
            throw new Error(text || ('请求失败: HTTP ' + res.status));
        }

        // 统一成功/失败业务码：{ code, message, data }
        if (parsed && typeof parsed === 'object' && parsed.code != null && parsed.message != null && ('data' in parsed)) {
            if (parsed.code === 0) {
                return parsed.data;
            }
            throw new Error(parsed.message || ('请求失败 code=' + parsed.code));
        }

        // 兼容非 ApiResponse 的旧接口：直接返回 JSON 或纯文本
        if (parsed != null) return parsed;
        return text;
    }

    async function refreshSessionUser() {
        var el = document.getElementById('sessionUser');
        var timeoutMs = 15000;
        try {
            var fetchMe = api('/api/me', { noRedirect: true });
            var timeout = new Promise(function (_, rej) {
                setTimeout(function () { rej(new Error('__timeout__')); }, timeoutMs);
            });
            var me = await Promise.race([fetchMe, timeout]);
            if (!el) return;
            if (me === null) {
                el.innerHTML = '未登录 · <a href="' + sameAppPath('/login') + '" style="color:#fff;text-decoration:underline;">去登录</a>';
                return;
            }
            if (typeof me === 'object' && me !== null && me.username) {
                el.textContent = '已登录：' + me.username;
            } else {
                el.textContent = '已登录';
            }
        } catch (e) {
            if (!el) return;
            if (e && e.message === '__timeout__') {
                el.textContent = '会话检查超时，请检查网络或刷新';
            } else {
                el.textContent = '无法获取会话';
            }
        }
    }

    function renderTabs() {
        var tabsEl = document.getElementById('tabs');
        var panesEl = document.getElementById('panes');
        if (!tabsEl || !panesEl) {
            throw new Error('找不到 #tabs 或 #panes');
        }
        tabsEl.innerHTML = '';
        panesEl.innerHTML = '';
        paneDefs.forEach(function (d, i) {
            var tab = document.createElement('button');
            tab.type = 'button';
            tab.className = 'tab' + (i === 0 ? ' active' : '');
            tab.setAttribute('role', 'tab');
            tab.setAttribute('aria-selected', i === 0 ? 'true' : 'false');
            tab.innerText = d.title;
            tab.title = d.desc;
            (function (pid) {
                tab.onclick = function () { activatePane(pid); };
            })(d.id);
            tabsEl.appendChild(tab);

            var pane = document.createElement('section');
            pane.className = 'card pane' + (i === 0 ? ' active' : '');
            pane.id = 'pane-' + d.id;
            pane.setAttribute('role', 'tabpanel');
            panesEl.appendChild(pane);
        });
    }

    function activatePane(id) {
        var tabsEl = document.getElementById('tabs');
        var panesEl = document.getElementById('panes');
        if (!tabsEl || !panesEl) return;
        var tabButtons = tabsEl.querySelectorAll('button[role="tab"]');
        tabButtons.forEach(function (el, i) {
            var def = paneDefs[i];
            var on = def && def.id === id;
            if (on) el.classList.add('active'); else el.classList.remove('active');
            el.setAttribute('aria-selected', on ? 'true' : 'false');
        });
        panesEl.querySelectorAll('section.pane').forEach(function (el) {
            var on = el.id === 'pane-' + id;
            if (on) el.classList.add('active'); else el.classList.remove('active');
        });
        if (id === 'activity') {
            loadActivity();
        }
        if (id === 'drill') {
            loadDrillTemplates();
        }
    }

    function paneHtml() {
        document.getElementById('pane-config').innerHTML = `
            <h2>邮箱配置</h2>
            <div class="inner-card">
                <div class="grid">
                    <div><label>配置序号（从 0 开始）</label><input id="cfgIndex" type="number" placeholder="0"></div>
                    <div><label>SMTP 主机</label><input id="cfgHost" placeholder="smtp.example.com"></div>
                    <div><label>端口</label><input id="cfgPort" type="number" placeholder="465"></div>
                    <div><label>用户名</label><input id="cfgUser" placeholder="邮箱账号"></div>
                    <div><label>密码 / 授权码</label><input id="cfgPass" type="password" placeholder=""></div>
                    <div><label>From 地址</label><input id="cfgFrom" placeholder="发件人邮箱"></div>
                    <div><label>发件人昵称</label><input id="cfgNick" placeholder="显示名称"></div>
                    <div><label>SSL</label>
                        <select id="cfgSsl"><option value="true">开启 SSL</option><option value="false">关闭 SSL</option></select></div>
                    <div><label>SMTP 认证</label>
                        <select id="cfgAuth"><option value="true">需要认证（常规邮箱）</option><option value="false">不需要认证（内网/测试）</option></select></div>
                </div>
                <div class="row" style="margin-top:14px;">
                    <button type="button" onclick="getAllConfig()">查询全部</button>
                    <button type="button" onclick="getConfigById()">按序号查询</button>
                    <button type="button" onclick="addConfig()">新增</button>
                    <button type="button" onclick="updateConfig()">更新</button>
                    <button type="button" class="danger" onclick="delConfig()">删除</button>
                </div>
            </div>`;

        document.getElementById('pane-proxy').innerHTML = `
            <h2>代理设置</h2>
            <div class="inner-card">
                <div class="grid">
                    <div><label>状态</label>
                        <select id="proxyEnable"><option value="true">启用</option><option value="false">禁用</option></select></div>
                    <div><label>类型</label>
                        <select id="proxyType"><option>HTTP</option><option>SOCKS4</option><option>SOCKS5</option></select></div>
                    <div><label>主机</label><input id="proxyHost" placeholder="127.0.0.1"></div>
                    <div><label>端口</label><input id="proxyPort" type="number" placeholder="7890"></div>
                    <div><label>代理用户名（可选）</label><input id="proxyUser" placeholder=""></div>
                    <div><label>代理密码（可选）</label><input id="proxyPassword" type="password" placeholder=""></div>
                </div>
                <p class="hint">邮件走 SOCKS5 时，后端 JavaMail 使用 <code class="inline">mail.smtp.socks.*</code>；类型请选 SOCKS5 并与服务器一致。</p>
                <div class="row" style="margin-top:14px;">
                    <button type="button" onclick="setProxy()">保存代理</button>
                    <button type="button" class="danger" onclick="unsetProxy()">清空代理</button>
                </div>
            </div>`;

        document.getElementById('pane-convert').innerHTML = `
            <h2>EML 转 HTML</h2>
            <div class="inner-card">
                <label for="emlFile">选择 .eml 文件</label>
                <input id="emlFile" type="file" accept=".eml">
                <div class="row" style="margin-top:12px;"><button type="button" onclick="convertEml()">上传并下载 HTML</button></div>
            </div>`;

        document.getElementById('pane-attachment').innerHTML = `
            <h2>附件管理</h2>
            <div class="inner-card">
                <label for="attachmentFiles">选择文件（可多选）</label>
                <input id="attachmentFiles" type="file" multiple>
                <div class="row" style="margin-top:12px;">
                    <button type="button" onclick="uploadAttachments()">上传附件</button>
                    <button type="button" class="secondary" onclick="listAttachments()">刷新列表</button>
                    <button type="button" class="danger" onclick="clearAttachments()">清空附件</button>
                </div>
                <p class="hint">当前附件（发送/模板邮件可引用）：</p>
                <pre class="inner-card" style="margin-top:8px;font-size:12px;overflow:auto;"><code id="attachmentList">[]</code></pre>
            </div>`;

        document.getElementById('pane-send').innerHTML = `
            <h2>普通邮件发送</h2>
            <div class="inner-card">
                <div class="grid">
                    <div><label>收件人（每行一个，支持逗号/分号分隔）</label><textarea id="sendTo" placeholder="a@b.com"></textarea></div>
                    <div><label>主题</label><textarea id="sendSubject" placeholder="邮件主题" style="min-height:52px;"></textarea></div>
                </div>
                <label for="sendContent" style="margin-top:12px;">正文（HTML 或纯文本）</label>
                <textarea id="sendContent" placeholder="邮件正文"></textarea>
                <p class="hint">附件来自上方「附件管理」中已上传的文件列表。</p>
                <label style="margin-top:12px;display:flex;gap:8px;align-items:center;">
                    <input id="forgeEnable" type="checkbox">
                    启用 Forgery 发件人（本地研究测试）
                </label>
                <div class="grid" style="margin-top:10px;">
                    <div><label style="margin-top:0;">From 显示昵称（不动邮箱，可选）</label><input id="forgeFromNickname" placeholder="昵称"></div>
                    <div><label style="margin-top:0;">Reply-To 邮箱（可选）</label><input id="forgeFromEmail" placeholder="replyto@example.com"></div>
                    <div><label style="margin-top:0;">Reply-To 昵称（可选）</label><input id="replyToNickname" placeholder="Reply Nick"></div>
                </div>
                <div class="grid" style="margin-top:10px;">
                    <div><label style="margin-top:0;">投递间隔（秒，0=不控制）</label><input id="deliveryIntervalMs" type="text" placeholder="0 / 1s / 5s"></div>
                </div>
                <div class="row" style="margin-top:12px;"><button type="button" onclick="sendEmail()">立即发送</button></div>
            </div>`;

        document.getElementById('pane-custom').innerHTML = `
            <h2>模板邮件（HTML + CSV）</h2>
            <div class="inner-card">
                <label for="customFiles">上传模板与数据文件</label>
                <input id="customFiles" type="file" multiple accept=".html,.csv">
                <div class="row" style="margin-top:12px;">
                    <button type="button" onclick="uploadTemplateCsv()">上传模板与 CSV</button>
                    <button type="button" class="secondary" onclick="listTemplateCsv()">刷新列表</button>
                    <button type="button" class="danger" onclick="clearTemplateCsv()">清空模板文件</button>
                </div>
                <p class="hint">可用文件：</p>
                <pre class="inner-card" style="margin-top:8px;font-size:12px;overflow:auto;"><code id="templateCsvList">[]</code></pre>
                <div class="grid" style="margin-top:12px;">
                    <div><label>模板文件绝对路径</label><input id="templatePath" placeholder="上传后自动填充或手填"></div>
                    <div><label>CSV 绝对路径</label><input id="csvPath" placeholder="需含 toEmail、emailSubject 等列"></div>
                </div>
                <label style="margin-top:12px;display:flex;gap:8px;align-items:center;">
                    <input id="forgeEnableCustom" type="checkbox">
                    启用 Forgery 发件人（本地研究测试）
                </label>
                <div class="grid" style="margin-top:10px;">
                    <div><label style="margin-top:0;">From 显示昵称（不动邮箱，可选）</label><input id="forgeFromNicknameCustom" placeholder="昵称"></div>
                    <div><label style="margin-top:0;">Reply-To 邮箱（可选）</label><input id="forgeFromEmailCustom" placeholder="replyto@example.com"></div>
                    <div><label style="margin-top:0;">Reply-To 昵称（可选）</label><input id="replyToNicknameCustom" placeholder="Reply Nick"></div>
                </div>
                <div class="grid" style="margin-top:10px;">
                    <div><label style="margin-top:0;">投递间隔（秒，0=不控制）</label><input id="deliveryIntervalMsCustom" type="text" placeholder="0 / 1s / 5s"></div>
                </div>
                <div class="row" style="margin-top:12px;">
                    <button type="button" class="secondary" onclick="validateCustom()">渲染预览（首条）</button>
                    <button type="button" onclick="sendCustom()">发送模板邮件</button>
                </div>
            </div>`;

        document.getElementById('pane-activity').innerHTML = `
            <h2>统计与记录</h2>
            <div class="inner-card">
                <div class="row" style="margin-bottom:10px;">
                    <button type="button" onclick="loadActivity()">刷新</button>
                </div>
                <label>汇总</label>
                <pre class="inner-card" style="margin-top:8px;font-size:12px;overflow:auto;max-height:200px;"><code id="activityStats">尚未加载</code></pre>
                <label style="margin-top:12px;display:block;">最近记录</label>
                <pre class="inner-card" style="margin-top:8px;font-size:12px;overflow:auto;max-height:280px;"><code id="activityRecords">尚未加载</code></pre>
            </div>`;

        document.getElementById('pane-drill').innerHTML = `
            <h2>演练模板</h2>
            <div class="inner-card">
                <p class="hint">内置模板含「授权安全意识演练」横幅，适用于经授权的安全意识演练；请部署到本机后，将路径填到「模板邮件」的模板文件字段，并配合示例 CSV。禁止用于未授权测试。</p>
                <div id="drillListHost" style="margin-top:12px;"></div>
            </div>`;
    }

    function escapeHtml(s) {
        if (s == null) return '';
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function parseDeliveryIntervalMs(input) {
        if (input == null) return 0;
        var s = String(input).trim().toLowerCase();
        if (!s) return 0;
        if (s === '0') return 0;
        var ms = 0;
        // 支持：1s / 5s / 1000ms / 直接数字（按秒）
        if (s.endsWith('ms')) {
            var v1 = parseFloat(s.slice(0, -2));
            if (isNaN(v1)) return 0;
            ms = v1;
        } else {
            if (s.endsWith('s')) s = s.slice(0, -1);
            var v2 = parseFloat(s);
            if (isNaN(v2)) return 0;
            ms = v2 * 1000;
        }
        if (!(ms > 0)) return 0;
        // clamp：上限 10min，避免输入过大导致异常等待
        if (ms > 600000) ms = 600000;
        return Math.round(ms);
    }

    async function loadActivity() {
        try {
            var st = await api('/api/activity/stats');
            var rec = await api('/api/activity/records?limit=80');
            var a = document.getElementById('activityStats');
            var b = document.getElementById('activityRecords');
            if (a) a.textContent = JSON.stringify(st, null, 2);
            if (b) b.textContent = JSON.stringify(rec, null, 2);
            setStatus('统计与记录已刷新');
        } catch (e) {
            setStatus(e.message, false);
        }
    }

    async function loadDrillTemplates() {
        try {
            var list = await api('/api/drill/templates');
            var host = document.getElementById('drillListHost');
            if (!host) return;
            host.innerHTML = list.map(function (t) {
                return '<div class="inner-card" style="margin-bottom:12px;">' +
                    '<strong>' + escapeHtml(t.title) + '</strong> <span style="color:#64748b;font-size:12px;">' + escapeHtml(t.id) + '</span>' +
                    '<p style="margin:8px 0 0;font-size:13px;color:#475569;line-height:1.5;">' + escapeHtml(t.description) + '</p>' +
                    '<div class="row" style="margin-top:10px;">' +
                    '<button type="button" onclick="deployDrill(\'' + t.id + '\')">部署到本机</button>' +
                    '<button type="button" class="ghost" onclick="downloadDrillCsv(\'' + t.id + '\')">下载示例 CSV</button>' +
                    '</div><p class="hint" id="drill-path-' + t.id + '"></p></div>';
            }).join('');
            setStatus('已加载 ' + list.length + ' 个演练模板');
        } catch (e) {
            setStatus(e.message, false);
        }
    }

    async function deployDrill(id) {
        try {
            var r = await api('/api/drill/deploy?id=' + encodeURIComponent(id), { method: 'POST' });
            var el = document.getElementById('drill-path-' + id);
            if (el) el.textContent = '模板路径: ' + r.path + '（可填到「模板邮件」的模板文件字段）';
            setStatus('已部署: ' + r.path);
        } catch (e) {
            setStatus(e.message, false);
        }
    }

    async function downloadDrillCsv(id) {
        try {
            var res = await fetch(sameAppPath('/api/drill/sample-csv?id=' + encodeURIComponent(id)), { credentials: 'same-origin' });
            if (res.status === 401) { window.location.href = sameAppPath('/login'); return; }
            if (!res.ok) throw new Error(await res.text());
            var blob = await res.blob();
            var url = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = 'drill-sample-' + id + '.csv';
            a.click();
            URL.revokeObjectURL(url);
            setStatus('示例 CSV 已下载');
        } catch (e) {
            setStatus(e.message, false);
        }
    }

    function buildAccount() {
        return {
            id: Number(document.getElementById('cfgIndex').value),
            host: document.getElementById('cfgHost').value,
            port: Number(document.getElementById('cfgPort').value),
            ssl: document.getElementById('cfgSsl').value === 'true',
            authrequired: document.getElementById('cfgAuth').value === 'true',
            username: document.getElementById('cfgUser').value,
            password: document.getElementById('cfgPass').value,
            from: document.getElementById('cfgFrom').value,
            nickname: document.getElementById('cfgNick').value
        };
    }

    async function getAllConfig() {
        try { setStatus(JSON.stringify(await api('/api/config/getAllConfig'), null, 2)); }
        catch (e) { setStatus(e.message, false); }
    }
    async function getConfigById() {
        try { setStatus(JSON.stringify(await api('/api/config/getConfig?index=' + document.getElementById('cfgIndex').value), null, 2)); }
        catch (e) { setStatus(e.message, false); }
    }
    async function addConfig() {
        try {
            setStatus(await api('/api/config/addConfig?index=' + document.getElementById('cfgIndex').value, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(buildAccount())
            }));
        } catch (e) { setStatus(e.message, false); }
    }
    async function updateConfig() {
        try {
            setStatus(await api('/api/config/updateConfig?index=' + document.getElementById('cfgIndex').value, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(buildAccount())
            }));
        } catch (e) { setStatus(e.message, false); }
    }
    async function delConfig() {
        try { setStatus(await api('/api/config/delConfig?index=' + document.getElementById('cfgIndex').value)); }
        catch (e) { setStatus(e.message, false); }
    }

    function proxyPayload() {
        return {
            enable: document.getElementById('proxyEnable').value === 'true',
            type: document.getElementById('proxyType').value,
            host: document.getElementById('proxyHost').value,
            port: Number(document.getElementById('proxyPort').value),
            username: document.getElementById('proxyUser').value,
            password: document.getElementById('proxyPassword').value
        };
    }
    async function setProxy() {
        try {
            setStatus(await api('/api/proxy/setProxy', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(proxyPayload())
            }));
        } catch (e) { setStatus(e.message, false); }
    }
    async function unsetProxy() {
        try {
            setStatus(await api('/api/proxy/unSetProxy', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(proxyPayload())
            }));
        } catch (e) { setStatus(e.message, false); }
    }

    async function convertEml() {
        try {
            var file = document.getElementById('emlFile').files[0];
            if (!file) throw new Error('请选择 .eml 文件');
            var fd = new FormData();
            fd.append('file', file);
            var res = await fetch(sameAppPath('/api/file/conversion'), { method: 'POST', credentials: 'same-origin', body: fd });
            if (res.status === 401) { window.location.href = sameAppPath('/login'); return; }
            if (!res.ok) throw new Error(await res.text());
            var blob = await res.blob();
            var url = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = file.name.replace(/\.eml$/i, '.html');
            a.click();
            URL.revokeObjectURL(url);
            setStatus('EML 转换成功，HTML 已下载。');
        } catch (e) { setStatus(e.message, false); }
    }

    async function uploadAttachments() {
        try {
            var files = document.getElementById('attachmentFiles').files;
            if (!files.length) throw new Error('请先选择附件');
            var fd = new FormData();
            Array.prototype.forEach.call(files, function (f) { fd.append('file', f); });
            setStatus(await api('/api/email/uploadAttachments', { method: 'POST', body: fd }));
            await listAttachments();
        } catch (e) { setStatus(e.message, false); }
    }

    async function listAttachments() {
        try {
            var data = await api('/api/email/getUploadAttachments', { noRedirect: true });
            if (data === null || typeof data !== 'object') {
                return {};
            }
            var el = document.getElementById('attachmentList');
            if (el) el.textContent = JSON.stringify(data, null, 2);
            setStatus('附件列表已刷新');
            return data;
        } catch (e) { setStatus(e.message, false); return {}; }
    }

    async function clearAttachments() {
        try {
            setStatus(await api('/api/email/delUploadAttachments'));
            await listAttachments();
        } catch (e) { setStatus(e.message, false); }
    }

    async function sendEmail() {
        try {
            var raw = document.getElementById('sendTo').value;
            var to = raw.split(/\n|,|;/).map(function (v) { return v.trim(); }).filter(Boolean);
            var subject = document.getElementById('sendSubject').value.trim();
            var content = document.getElementById('sendContent').value;
            var attachmentMap = await listAttachments();
            var attachments = Object.keys(attachmentMap).map(function (fileName) {
                return { fileName: fileName, filePath: attachmentMap[fileName] };
            });
            var payload = { to: to, subject: subject, content: content, attachments: attachments };
            var forgeEnableEl = document.getElementById('forgeEnable');
            var forgeEnabled = forgeEnableEl && forgeEnableEl.checked === true;
            if (forgeEnabled) {
                var forgeEmailEl = document.getElementById('forgeFromEmail');
                var forgeEmail = forgeEmailEl ? forgeEmailEl.value.trim() : '';
                var forgeNickEl = document.getElementById('forgeFromNickname');
                var forgeNick = forgeNickEl ? forgeNickEl.value.trim() : '';
                var replyToNickEl = document.getElementById('replyToNickname');
                var replyToNick = replyToNickEl ? replyToNickEl.value.trim() : '';

                // forgeFromEmail 这里用于测试 Reply-To 邮箱（不动 From 邮箱）
                if (forgeNick) payload.forgeFromNickname = forgeNick;
                if (forgeEmail) payload.replyToEmail = forgeEmail;
                if (replyToNick) payload.replyToNickname = replyToNick;
            }
            var intervalEl = document.getElementById('deliveryIntervalMs');
            var intervalMs = intervalEl ? parseDeliveryIntervalMs(intervalEl.value) : 0;
            if (intervalMs > 0) payload.deliveryIntervalMs = intervalMs;
            setStatus(await api('/api/email/send', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            }));
        } catch (e) { setStatus(e.message, false); }
    }

    async function uploadTemplateCsv() {
        try {
            var files = document.getElementById('customFiles').files;
            if (!files.length) throw new Error('请选择 html / csv 文件');
            var fd = new FormData();
            Array.prototype.forEach.call(files, function (f) { fd.append('file', f); });
            setStatus(await api('/api/email/uploadTemplateAndCsv', { method: 'POST', body: fd }));
            await listTemplateCsv();
        } catch (e) { setStatus(e.message, false); }
    }

    async function listTemplateCsv() {
        try {
            var data = await api('/api/email/getUploadTemplateAndCsv', { noRedirect: true });
            if (data === null || typeof data !== 'object') {
                return {};
            }
            var tcl = document.getElementById('templateCsvList');
            if (tcl) tcl.textContent = JSON.stringify(data, null, 2);
            var keys = Object.keys(data);
            var htmlPath = null;
            var csvPath = null;
            for (var i = 0; i < keys.length; i++) {
                var k = keys[i];
                var lower = k.toLowerCase();
                if (lower.endsWith('.html')) htmlPath = data[k];
                if (lower.endsWith('.csv')) csvPath = data[k];
            }
            if (htmlPath) document.getElementById('templatePath').value = htmlPath;
            if (csvPath) document.getElementById('csvPath').value = csvPath;
            setStatus('模板文件列表已刷新');
            return data;
        } catch (e) { setStatus(e.message, false); return {}; }
    }

    async function clearTemplateCsv() {
        try {
            setStatus(await api('/api/email/delUploadTemplateAndCsv'));
            await listTemplateCsv();
        } catch (e) { setStatus(e.message, false); }
    }

    async function validateCustom() {
        try {
            var payload = {
                templatePath: document.getElementById('templatePath').value,
                csvPath: document.getElementById('csvPath').value
            };
            setStatus(await api('/api/email/validationCustomEmail', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            }));
        } catch (e) { setStatus(e.message, false); }
    }

    async function sendCustom() {
        try {
            var attachmentMap = await listAttachments();
            var attachments = Object.keys(attachmentMap).map(function (fileName) {
                return { fileName: fileName, filePath: attachmentMap[fileName] };
            });
            var payload = {
                templatePath: document.getElementById('templatePath').value,
                csvPath: document.getElementById('csvPath').value,
                attachments: attachments
            };
            var forgeEnableEl = document.getElementById('forgeEnableCustom');
            var forgeEnabled = forgeEnableEl && forgeEnableEl.checked === true;
            if (forgeEnabled) {
                var forgeEmailEl = document.getElementById('forgeFromEmailCustom');
                var forgeEmail = forgeEmailEl ? forgeEmailEl.value.trim() : '';
                var forgeNickEl = document.getElementById('forgeFromNicknameCustom');
                var forgeNick = forgeNickEl ? forgeNickEl.value.trim() : '';
                var replyToNickEl = document.getElementById('replyToNicknameCustom');
                var replyToNick = replyToNickEl ? replyToNickEl.value.trim() : '';

                // forgeFromEmailCustom 这里用于测试 Reply-To 邮箱（不动 From 邮箱）
                if (forgeNick) payload.forgeFromNickname = forgeNick;
                if (forgeEmail) payload.replyToEmail = forgeEmail;
                if (replyToNick) payload.replyToNickname = replyToNick;
            }
            var intervalEl = document.getElementById('deliveryIntervalMsCustom');
            var intervalMs = intervalEl ? parseDeliveryIntervalMs(intervalEl.value) : 0;
            if (intervalMs > 0) payload.deliveryIntervalMs = intervalMs;
            setStatus(await api('/api/email/sendCustomEmail', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            }));
        } catch (e) { setStatus(e.message, false); }
    }

    function initApp() {
        var tabsHolder = document.getElementById('tabs');
        var panesHolder = document.getElementById('panes');
        if (!tabsHolder || !panesHolder) {
            var shell = document.querySelector('.shell');
            if (shell) {
                var banner = document.createElement('div');
                banner.className = 'card';
                banner.style.cssText = 'border:2px solid #dc2626;background:#fef2f2;margin-bottom:14px;';
                banner.innerHTML = '<h2 style="margin:0 0 8px;color:#991b1b;">页面结构异常</h2><p class="hint" style="color:#7f1d1d;">未找到 #tabs / #panes。请尝试强制刷新 (Ctrl+F5)，或确认访问的是本应用返回的完整 HTML。</p>';
                var anchor = shell.querySelector('section.card.compact');
                if (anchor) {
                    shell.insertBefore(banner, anchor);
                } else {
                    shell.appendChild(banner);
                }
            }
            return;
        }
        try {
            renderTabs();
            paneHtml();
            activatePane(paneDefs[0].id);
        } catch (e) {
            panesHolder.innerHTML = '<section class="card"><h2>初始化失败</h2><p class="hint">' + String(e.message || e) + '</p></section>';
            return;
        }
        refreshSessionUser();
        listAttachments();
        listTemplateCsv();
    }

    function bootConsole() {
        if (window.__webConsoleInited) return;
        window.__webConsoleInited = true;
        initApp();
    }

    // 脚本执行后立即初始化一次，避免 DOMContentLoaded/load 竞态导致“无 Tab 无请求”。
    setTimeout(bootConsole, 0);
