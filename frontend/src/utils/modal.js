/**
 * ============================================================================
 * 文件：frontend/src/utils/modal.js
 * 作用：自定义模态框工具模块，替代原生的 window.confirm() 和 window.alert()
 * ============================================================================
 *
 * 【教学知识点：为什么需要自定义模态框】
 *
 * 原生的 window.confirm() 和 window.alert() 存在以下问题：
 *
 * 1. 样式不可控：
 *    - 不同浏览器显示样式不同
 *    - 无法自定义样式，与应用风格不统一
 *    - 无法添加自定义按钮或内容
 *
 * 2. 功能有限：
 *    - confirm() 只有确认和取消两个按钮
 *    - alert() 只有一个确定按钮
 *    - 无法自定义标题、按钮文本
 *
 * 3. 用户体验差：
 *    - 会阻塞 JavaScript 执行（同步行为）
 *    - 无法添加动画效果
 *    - 移动端体验不佳
 *
 * 4. 安全风险：
 *    - 某些浏览器允许用户勾选"不再显示此页面的对话框"
 *    - 可能被恶意网站滥用进行"对话框轰炸"
 *
 * 【教学知识点：纯 DOM 操作实现】
 *
 * 本模块使用纯 DOM 操作创建模态框，不依赖任何 UI 框架（如 Element Plus）。
 * 这样做的好处：
 * 1. 零依赖：不需要额外的包
 * 2. 轻量级：代码量小，加载快
 * 3. 灵活性：可以完全控制样式和行为
 *
 * 缺点：
 * 1. 代码复杂度高：需要手动创建和管理 DOM 元素
 * 2. 可维护性差：样式和逻辑混在一起
 * 3. 缺少动画：需要手动实现过渡效果
 *
 * 【教学知识点：Promise 封装】
 *
 * confirmDialog 和 alertMessage 都返回 Promise，
 * 这样可以使用 async/await 语法调用：
 *
 * // 传统回调方式
 * window.confirm('确认删除？') // 返回 true/false
 *
 * // Promise 方式
 * const confirmed = await confirmDialog('确认删除？')
 *
 * Promise 方式的优势：
 * 1. 代码更清晰，逻辑更易读
 * 2. 可以在 await 前后添加其他异步操作
 * 3. 更容易进行错误处理
 */

/**
 * 【教学知识点：HTML 转义函数】
 *
 * 功能：将字符串中的特殊 HTML 字符转换为实体，防止 XSS 攻击。
 *
 * 需要转义的特殊字符：
 * - & → &amp;  （必须最先转义）
 * - < → &lt;
 * - > → &gt;
 * - " → &quot;
 * - ' → &#39;
 *
 * 【教学知识点：XSS 攻击】
 *
 * XSS（Cross-Site Scripting，跨站脚本攻击）是一种常见的 Web 安全漏洞。
 * 攻击者通过在页面中注入恶意脚本，可以：
 * - 窃取用户的 Cookie（包括 Token）
 * - 伪造用户请求
 * - 篡改页面内容
 * - 重定向到恶意网站
 *
 * 本函数使用 DOM API 进行转义，比手动替换更安全：
 * 1. 创建一个 div 元素
 * 2. 设置 textContent（会自动转义特殊字符）
 * 3. 读取 innerHTML（获取转义后的内容）
 *
 * 这是浏览器原生的转义机制，比手动替换更可靠。
 *
 * @param {string} str - 需要转义的字符串
 * @returns {string} 转义后的安全字符串
 */
function escapeHtml(str) {
  if (!str) return ''
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

/**
 * 【教学知识点：创建模态框容器】
 *
 * 功能：创建模态框的 DOM 结构，包括遮罩层和模态框容器。
 *
 * 返回值：{ overlay, modal }
 * - overlay: 遮罩层元素（半透明黑色背景）
 * - modal: 模态框容器（白色圆角卡片）
 *
 * 【教学知识点：CSS 内联样式】
 *
 * 本函数使用 style.cssText 设置样式，这是一种内联样式的方式。
 * 与 CSS 类相比，内联样式的优先级更高。
 *
 * 常见的 CSS 属性：
 * - position: fixed - 固定定位，相对于视口
 * - z-index: 9999 - 层叠顺序，数字越大越靠前
 * - display: flex - 弹性布局
 * - align-items: center - 垂直居中
 * - justify-content: center - 水平居中
 * - border-radius: 16px - 圆角
 * - box-shadow: 阴影效果
 * - rgba(0,0,0,0.45) - 半透明黑色
 *
 * 【教学知识点：遮罩层的作用】
 *
 * 遮罩层（overlay）的作用：
 * 1. 视觉隔离：将用户的注意力集中在模态框上
 * 2. 防止误操作：用户点击遮罩层可以关闭模态框
 * 3. 锁定交互：阻止用户与背景页面交互
 */
function createModalContainer() {
  // 创建半透明遮罩层
  const overlay = document.createElement('div')
  overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.45);z-index:9999;display:flex;align-items:center;justify-content:center;'

  // 创建模态框容器
  const modal = document.createElement('div')
  modal.style.cssText = 'background:#fff;border-radius:16px;padding:28px 32px;max-width:420px;width:90%;box-shadow:0 20px 60px rgba(0,0,0,0.2);font-family:system-ui,-apple-system,sans-serif;'

  // 将模态框添加到遮罩层
  overlay.appendChild(modal)

  // 将遮罩层添加到页面 body
  document.body.appendChild(overlay)

  return { overlay, modal }
}

/**
 * 【教学知识点：confirmDialog 确认对话框】
 *
 * 功能：显示一个确认对话框，返回 Promise<boolean>。
 *
 * @param {string} message - 确认提示文本
 * @param {Object} [options] - 可选配置
 * @param {string} [options.confirmText='确认'] - 确认按钮文本
 * @param {string} [options.cancelText='取消'] - 取消按钮文本
 * @param {string} [options.title='操作确认'] - 对话框标题
 * @returns {Promise<boolean>} 用户点击确认返回 true，取消返回 false
 *
 * 使用示例：
 * const confirmed = await confirmDialog('确认删除该商品？')
 * if (confirmed) {
 *   // 执行删除操作
 * }
 *
 * // 自定义按钮文本
 * const confirmed = await confirmDialog('确认提交订单？', {
 *   title: '订单确认',
 *   confirmText: '提交',
 *   cancelText: '再想想'
 * })
 *
 * 【教学知识点：innerHTML 安全使用】
 *
 * 本函数使用 innerHTML 设置模态框内容。虽然存在 XSS 风险，
 * 但通过 escapeHtml() 对用户输入进行了转义，是安全的。
 *
 * 模板字符串（Template Literal）使用反引号 ` 包裹，
 * 可以使用 ${} 插入变量：
 * `<h3>${escapeHtml(title)}</h3>`
 *
 * 【教学知识点：事件监听与清理】
 *
 * 使用 addEventListener 监听事件，需要注意：
 * 1. 事件监听器会在 DOM 元素移除时自动清理
 * 2. 但全局事件（如 document 上的 keydown）需要手动清理
 * 3. 不清理会导致内存泄漏
 *
 * 本函数在关闭模态框时会：
 * 1. 移除 overlay 元素（自动清理其上的事件）
 * 2. 移除 document 上的 keydown 监听器（手动清理）
 */
export function confirmDialog(message, options = {}) {
  /**
   * 【教学知识点：Promise 构造函数】
   *
   * new Promise((resolve, reject) => { ... })
   *
   * - resolve(value): 将 Promise 状态改为"已完成"，并传递结果值
   * - reject(error): 将 Promise 状态改为"已拒绝"，并传递错误原因
   *
   * 本函数只使用 resolve，因为：
   * - 用户点击确认：resolve(true)
   * - 用户点击取消：resolve(false)
   * - 不会有"错误"情况
   *
   * 调用方使用 await 获取结果：
   * const result = await confirmDialog('确认？')
   * // result 是 true 或 false
   */
  return new Promise((resolve) => {
    // 创建遮罩层和模态框
    const { overlay, modal } = createModalContainer()

    // 获取配置项，使用默认值
    const title = options.title || '操作确认'

    /**
     * 【教学知识点：模板字符串构建 HTML】
     *
     * 使用模板字符串（反引号）构建 HTML 结构。
     *
     * 样式说明：
     * - h3: 标题样式，加粗，深色
     * - p: 提示文本样式，中灰色
     * - div: 按钮容器，使用 flex 布局右对齐
     * - button: 按钮样式，圆角，阴影
     *
     * 注意：所有用户输入都通过 escapeHtml() 转义，防止 XSS
     */
    modal.innerHTML = `
      <h3 style="margin:0 0 14px 0;font-size:18px;font-weight:700;color:#1e293b;">${escapeHtml(title)}</h3>
      <p style="margin:0 0 22px 0;font-size:15px;color:#475569;line-height:1.6;">${escapeHtml(message)}</p>
      <div style="display:flex;gap:12px;justify-content:flex-end;">
        <button class="modal-cancel-btn" style="padding:10px 22px;border-radius:10px;border:1px solid #cbd5e1;
        background:#fff;color:#475569;font-size:14px;font-weight:600;cursor:pointer;">
        ${escapeHtml(options.cancelText || '取消')}</button>
        <button class="modal-confirm-btn" style="padding:10px 22px;border-radius:10px;border:none;background:#4f7cff;
        color:#fff;font-size:14px;font-weight:600;cursor:pointer;">${escapeHtml(options.confirmText || '确认')}</button>
      </div>
    `

    /**
     * 【教学知识点：querySelector 选择器】
     *
     * querySelector(selector) 返回匹配选择器的第一个元素。
     * 常用选择器：
     * - '.class': 类选择器
     * - '#id': ID 选择器
     * - 'tag': 标签选择器
     * - '[attr]': 属性选择器
     *
     * 本函数使用类选择器获取按钮元素
     */
    const confirmBtn = modal.querySelector('.modal-confirm-btn')
    const cancelBtn = modal.querySelector('.modal-cancel-btn')

    /**
     * 【教学知识点：关闭模态框函数】
     *
     * close 函数负责：
     * 1. 从 DOM 中移除遮罩层（会自动清理其上的事件监听器）
     * 2. 调用 resolve 返回结果
     *
     * document.body.removeChild(overlay) 会：
     * - 移除 overlay 及其所有子元素
     * - 自动清理这些元素上的事件监听器
     * - 释放内存
     */
    const close = (result) => {
      document.body.removeChild(overlay)
      resolve(result)
    }

    /**
     * 【教学知识点：addEventListener 事件监听】
     *
     * element.addEventListener(event, handler) 用于监听事件。
     *
     * 常用事件：
     * - 'click': 鼠标点击
     * - 'keydown': 键盘按下
     * - 'mouseover': 鼠标悬停
     * - 'submit': 表单提交
     * - 'input': 输入框内容变化
     *
     * 本函数监听按钮的 click 事件：
     * - 确认按钮点击：close(true)
     * - 取消按钮点击：close(false)
     */
    confirmBtn.addEventListener('click', () => close(true))
    cancelBtn.addEventListener('click', () => close(false))

    /**
     * 【教学知识点：点击遮罩层关闭】
     *
     * 监听遮罩层的 click 事件，如果点击的是遮罩层本身（而不是模态框），
     * 则关闭模态框（等同于取消）。
     *
     * 为什么需要检查 e.target === overlay？
     * 因为事件冒泡：点击模态框内的元素时，事件会冒泡到遮罩层。
     * 如果不检查，点击模态框也会关闭。
     *
     * e.target 是实际被点击的元素，
     * overlay 是绑定了事件监听器的元素。
     * 只有当它们相同时，才说明用户点击的是遮罩层背景。
     */
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        close(false)
      }
    })

    /**
     * 【教学知识点：键盘事件处理（ESC 键关闭）】
     *
     * 监听键盘的 keydown 事件，如果按下 ESC 键则关闭模态框。
     *
     * 这是模态框的常见交互模式，提升用户体验。
     *
     * 【教学知识点：事件清理】
     *
     * 在 document 上添加的事件监听器需要手动清理，
     * 否则会导致内存泄漏。
     *
     * 本函数在按下 ESC 时：
     * 1. 关闭模态框
     * 2. 移除 document 上的 keydown 监听器
     *
     * 使用具名函数（handleEsc）而非匿名函数，
     * 这样才能通过 removeEventListener 移除。
     */
    const handleEsc = (e) => {
      if (e.key === 'Escape') {
        close(false)
        document.removeEventListener('keydown', handleEsc)
      }
    }
    document.addEventListener('keydown', handleEsc)

    /**
     * 【教学知识点：focus() 焦点管理】
     *
     * confirmBtn.focus() 将焦点设置到确认按钮上。
     *
     * 这样做的好处：
     * 1. 用户可以直接按 Enter 键确认
     * 2. 提升键盘可访问性
     * 3. 符合无障碍设计规范
     */
    confirmBtn.focus()
  })
}

/**
 * 【教学知识点：alertMessage 消息提示框】
 *
 * 功能：显示一个消息提示框，返回 Promise<void>。
 *
 * @param {string} message - 提示文本
 * @param {Object} [options] - 可选配置
 * @param {string} [options.buttonText='确定'] - 按钮文本
 * @param {string} [options.title='提示'] - 对话框标题
 * @returns {Promise<void>} 用户点击确定后 resolve
 *
 * 使用示例：
 * await alertMessage('操作成功！')
 *
 * // 自定义标题和按钮
 * await alertMessage('商品已添加到购物车', {
 *   title: '添加成功',
 *   buttonText: '知道了'
 * })
 *
 * 【教学知识点：confirmDialog vs alertMessage】
 *
 * confirmDialog: 有两个按钮（确认/取消），返回 boolean
 * alertMessage: 有一个按钮（确定），返回 void
 *
 * 使用场景：
 * - confirmDialog: 需要用户确认的操作（删除、提交等）
 * - alertMessage: 信息提示（成功、失败、警告等）
 */
export function alertMessage(message, options = {}) {
  return new Promise((resolve) => {
    const { overlay, modal } = createModalContainer()

    const title = options.title || '提示'

    /**
     * 【教学知识点：单按钮模态框】
     *
     * 与 confirmDialog 不同，alertMessage 只有一个"确定"按钮。
     * 布局使用 justify-content: flex-end 使按钮右对齐。
     */
    modal.innerHTML = `
      <h3 style="margin:0 0 14px 0;font-size:18px;font-weight:700;color:#1e293b;">${escapeHtml(title)}</h3>
      <p style="margin:0 0 22px 0;font-size:15px;color:#475569;line-height:1.6;">${escapeHtml(message)}</p>
      <div style="display:flex;justify-content:flex-end;">
        <button class="modal-ok-btn" style="padding:10px 22px;border-radius:10px;border:none;background:#4f7cff;
        color:#fff;font-size:14px;font-weight:600;cursor:pointer;">${escapeHtml(options.buttonText || '确定')}</button>
      </div>
    `

    const okBtn = modal.querySelector('.modal-ok-btn')

    // 关闭并清理模态框
    const close = () => {
      document.body.removeChild(overlay)
      resolve()
    }

    // 绑定按钮事件
    okBtn.addEventListener('click', close)

    // 点击遮罩层关闭
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        close()
      }
    })

    // 支持 ESC 键关闭
    const handleEsc = (e) => {
      if (e.key === 'Escape') {
        close()
        document.removeEventListener('keydown', handleEsc)
      }
    }
    document.addEventListener('keydown', handleEsc)

    // 聚焦确定按钮
    okBtn.focus()
  })
}
