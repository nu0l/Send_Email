# Send_Email

## 功能特点

- **多邮箱发送**：支持配置多个邮箱账号。
- **随机字符替换**：在邮件正文中插入随机不可见字符，规避内容检测。
- **干扰字符插入**：随机添加不可见干扰字符，有效绕过邮件网关。
- **正文编码**：采用 `quoted-printable` 编码技术，提升内容传递的隐蔽性。
- **代理支持**：支持代理设置，降低 IP 地址被检测风险。
- **延迟发送**：对邮件发送设置随机延迟，减少被判为垃圾邮件的可能性。
- **格式转换**：支持 `.eml` 文件导入并转换为 HTML 格式。
- **HTML 格式支持**：直接发送 HTML 格式邮件，打造精美邮件内容。
- **定制化邮件模板**：根据收件人信息定制内容，实现个性化邮件发送。

------

## 快速开始

### 运行方式

通过以下命令启动程序：

```bash
java -jar send_email.jar [web|gui]
```

- `web`：启动 SpringMVC（目前只实现了后端功能）。
- `gui`：启动 JavaFX 图形化界面。

以下以 GUI 模块为例：

------

### 配置说明

首次运行时，工具会自动生成配置文件： 路径：`~/.config/sendEmail/config.properties`

示例：

```bash
ls -la ~/.config/sendEmail/
-rw-r--r--@  1 test  staff   833 Dec 10 09:55 config.properties
```

> 若配置文件未生成，请确保 `send_email.jar` 具有写入权限：

```bash
chmod 777 send_email.jar
```

#### 配置邮箱账户

运行工具后，按以下步骤添加邮箱账户（以 163 邮箱为例）：

1. 打开“邮件配置”界面，输入以下信息：

   | 配置项     | 示例值                                         |
   | ---------- | ---------------------------------------------- |
   | 序号       | 从 `0` 开始递增                                |
   | 服务器地址 | smtp.163.com                                   |
   | 端口       | `25`（非加密）/ `465`（加密 SSL）              |
   | SSL        | `false`（端口为 `25`）/ `true`（端口为 `465`） |
   | 匿名邮箱   | `true`：匿名发送/ `false`：非匿名发送          |
   | 邮箱用户名 | example@163.com                                |
   | 邮箱密码   | 邮箱密码或授权码                               |
   | 发件昵称   | 管理员                                         |
   | 发件邮箱   | 与邮箱用户名相同（如 example@163.com）         |

2. 点击 **添加** 按钮，完成配置写入。

3. 通过 **查看配置** 选项确认配置是否正确。

------

### HTML 转换

支持从邮箱导出的 `.eml` 文件直接转换为 `.html` 格式邮件，并保持原始样式进行发送。**原汁原味**。

![image-20241212155254029](README.assets/image-20241212155254029.png)

------

### 发送邮件

建议配置多个邮箱账户，当第一个邮箱发送失败时会自动切换到其他邮箱。

发送流程：

1. 输入或导入邮件内容。

2. 程序会执行两步优化：

   - 随机插入不可见字符。
   - 随机添加干扰字符。

   通过这些操作提升邮件通过网关检测的概率。

#### Tips：

1. **附件发送**：对于 `.exe` 等可执行文件，建议多次编码或使用 7z 加密文件名，以绕过网关检测。
2. **敏感信息**：可以将敏感信息转换为图片插入邮件，网关无法检测图片内容。

------

### 定制化邮件

在某些情况下，可以对邮件内容进行定制化设计（例如针对某单位的精准推广或信息分发🎣）。

示例：

1. 将 `.eml` 文件转换为 `.html` 文件。
2. 使用 `thymeleaf` 模板语法进行插值，例如：`th:text="${userName}"`。

HTML 模板（Test.html）：

```html
<!DOCTYPE html>
<html>
<div style="line-height:1.7;color:#000000;font-size:14px;font-family:Arial">
    <p>亲爱的 <span th:text="${userName}"></span> (工号: <span th:text="${workNo}"></span>)，</p>
    <p>以下是关于 <span th:text="${companyName}"></span> VPN 的安装手册，帮助您顺利完成 VPN 的设置和使用。</p>
    <hr>
    <ul>
        <li>操作系统：Windows 10、macOS、Linux</li>
        <li>下载链接：<a href="https://baidu.com/">XXVPN 下载地址</a></li>
    </ul>
    <p>如遇问题，请联系 IT 支持团队。</p>
</div>
</html>
```

CSV 数据文件（Test.csv）：

| toEmail        | emailSubject | userName | workNo | companyName |
| -------------- | ------------ | -------- | ------ | ----------- |
| demo1@mail.com | VPN 安装指南 | 张三     | 001    | 公司 A      |
| demo2@mail.com | VPN 安装指南 | 李四     | 002    | 公司 B      |

#### 效果展示

邮件示例：

![image-20241212162200614](README.assets/image-20241212162200614.png)

----

# WEB 模块

运行 WEB 模块后，系统会生成默认的登录用户名和密码：

- **用户名**：`user`
- **密码**：随机生成（运行时在控制台输出，例如：`Using generated security password: b068f0ca-3e4b-44a2-b5dc-967e051a7c1b`）。

目前该模块仅封装了后端接口，无前端界面支持。

------

## 后端接口文档

### 配置接口 `/api/config`

- `GET /api/config/getConfig`：获取单个配置
- `GET /api/config/getAllConfig`：获取所有配置
- `POST /api/config/addConfig`：新增配置
- `POST /api/config/updateConfig`：更新配置
- `POST /api/config/delConfig`：删除配置
- `GET /api/config/getConfigPath`：获取配置文件路径

------

### 文件解析接口 `/api/file`

- `POST /api/file/conversion`：上传并解析 `.eml` 文件，自动转换为 HTML 格式并下载

------

### 代理接口 `/api/proxy`

- `POST /api/proxy/setProxy`：设置代理
- `POST /api/proxy/unSetProxy`：取消代理

------

### 邮件发送接口 `/api/email`

- `POST /api/email/uploadAttachments`：上传附件
- `GET /api/email/getUploadAttachments`：获取附件列表
- `POST /api/email/delUploadAttachments`：删除所有附件
- `POST /api/email/send`：发送邮件

------

### 自定义邮件接口 `/api/email`

- `POST /api/email/uploadTemplateAndCsv`：上传模板和 CSV 文件

- `GET /api/email/getUploadTemplateAndCsv`：获取模板和 CSV 文件列表

- `POST /api/email/delUploadTemplateAndCsv`：删除上传的模板和 CSV 文件

- `POST /api/email/validationCustomEmail`：验证自定义邮件

- `POST /api/email/sendCustomEmail`：发送自定义邮件

  

## TODO

- [ ] 封装前端界面
- [ ] 编写 API 文档（Swagger）
