package com.example.ecommerce.utils;

import com.example.ecommerce.entity.Product;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * ============================================================
 * 【教学重点】Excel 工具类 —— 数据导入导出的核心能力
 * ============================================================
 *
 * <h2>1. 为什么需要 Excel 导入导出？</h2>
 * <p>在电商管理系统中，Excel 是最常用的数据交换格式：</p>
 * <ul>
 *   <li><b>批量导入</b>：管理员通过 Excel 模板批量添加商品，比逐条录入效率高得多</li>
 *   <li><b>数据导出</b>：将商品列表、订单记录等导出为 Excel，便于线下分析、报表制作</li>
 *   <li><b>模板下载</b>：提供标准的导入模板，降低用户填写错误的概率</li>
 * </ul>
 *
 * <h2>2. Apache POI 简介</h2>
 * <p>Apache POI 是 Java 操作 Microsoft Office 文件的标准库：</p>
 * <ul>
 *   <li><b>HSSFWorkbook</b>：操作 .xls 格式（Excel 97-2003），最多 65536 行</li>
 *   <li><b>XSSFWorkbook</b>：操作 .xlsx 格式（Excel 2007+），支持 1048576 行</li>
 *   <li><b>WorkbookFactory</b>：工厂类，自动识别文件格式创建对应的 Workbook</li>
 * </ul>
 *
 * <h2>3. 本类提供的功能</h2>
 * <ul>
 *   <li>{@link #exportExcel} —— 通用的 Excel 导出方法</li>
 *   <li>{@link #exportProductImportTemplate} —— 导出商品导入模板</li>
 *   <li>{@link #parseProducts} —— 解析商品导入的 Excel 文件</li>
 * </ul>
 *
 * <h2>4. 导入导出流程</h2>
 * <pre>
 * 导出流程：
 * Controller 调用 exportExcel(headers, data)
 *     → 创建 Workbook 和 Sheet
 *     → 写入表头行（加粗、居中）
 *     → 写入数据行
 *     → 自动调整列宽
 *     → 输出为 byte[] → 返回文件下载
 *
 * 导入流程：
 * 用户上传 Excel 文件 → InputStream
 *     → WorkbookFactory.create() 解析文件
 *     → 读取表头，建立列名到字段的映射
 *     → 逐行读取数据，转换为 Product 对象
 *     → 返回 List&lt;Product&gt; → Service 层批量保存
 * </pre>
 *
 * @author 教学示例
 */
public class ExcelUtil {

    /*
     * 商品表头映射表。
     * <p>Key：Excel 中的表头名称（支持中文和英文）</p>
     * <p>Value：Product 实体的字段名</p>
     *
     * <p><b>教学点</b>：使用映射表的好处：</p>
     * <ul>
     *   <li>支持多种表头写法（如"商品名称"和"name"都映射到 name 字段）</li>
     *   <li>表头到字段的映射关系集中管理，易于维护</li>
     *   <li>新增字段只需在此处添加一行映射</li>
     * </ul>
     */
    private static final Map<String, String> PRODUCT_HEADER_MAPPING = createProductHeaderMapping();

    // ==================== 导出方法 ====================

    /*
     * 通用的 Excel 导出方法。
     *
     * <p><b>功能</b>：将数据列表导出为 Excel 格式的字节数组。</p>
     *
     * <p><b>生成的 Excel 结构</b>：</p>
     * <pre>
     * | 表头1（加粗居中） | 表头2（加粗居中） | 表头3（加粗居中） |
     * | 数据1-1          | 数据1-2          | 数据1-3          |
     * | 数据2-1          | 数据2-2          | 数据2-3          |
     * </pre>
     *
     * <p><b>使用示例</b>：</p>
     * <pre>{@code
     * // 在 Controller 中
     * @GetMapping("/products/export")
     * public void exportProducts(HttpServletResponse response) throws IOException {
     *     String[] headers = {"商品名称", "价格", "库存"};
     *     List<Map<String, Object>> data = productService.findAllAsMap();
     *     byte[] excelBytes = ExcelUtil.exportExcel(headers, data);
     *
     *     response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
     *     response.setHeader("Content-Disposition", "attachment; filename=products.xlsx");
     *     response.getOutputStream().write(excelBytes);
     * }
     * }</pre>
     *
     * @param headers  表头数组，每个元素对应 Excel 的一列标题
     * @param dataList 数据列表，每个 Map 代表一行，Key 为表头名称，Value 为单元格的值
     * @return Excel 文件的字节数组，可直接写入 OutputStream
     * @throws IOException 文件写入异常
     */
    public static byte[] exportExcel(String[] headers, List<Map<String, Object>> dataList) throws IOException {
        // 教学点：XSSFWorkbook 创建 .xlsx 格式（Excel 2007+），支持更多行和更好的压缩
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("数据导出");

        // ---------- 写入表头行 ----------
        Row headerRow = sheet.createRow(0);  // 第 0 行为表头
        for (int index = 0; index < headers.length; index++) {
            Cell cell = headerRow.createCell(index);
            cell.setCellValue(headers[index]);

            // 教学点：创建单元格样式，让表头更醒目
            org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);  // 加粗
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);      // 水平居中
            style.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            cell.setCellStyle(style);
        }

        // ---------- 写入数据行 ----------
        for (int rowIndex = 0; rowIndex < dataList.size(); rowIndex++) {
            Row dataRow = sheet.createRow(rowIndex + 1);  // 从第 1 行开始（第 0 行是表头）
            Map<String, Object> dataMap = dataList.get(rowIndex);
            for (int colIndex = 0; colIndex < headers.length; colIndex++) {
                Cell cell = dataRow.createCell(colIndex);
                // 教学点：通过表头名称从 Map 中获取对应的值
                Object value = dataMap.get(headers[colIndex]);
                if (value != null) {
                    cell.setCellValue(value.toString());  // 统一转为字符串写入
                }
            }
        }

        // ---------- 自动调整列宽 ----------
        for (int index = 0; index < headers.length; index++) {
            sheet.autoSizeColumn(index);
        }

        // ---------- 输出为字节数组 ----------
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();  // 教学点：必须关闭 workbook 释放资源
        return outputStream.toByteArray();
    }

    /*
     * 导出商品批量导入模板。
     *
     * <p><b>功能</b>：生成一个包含示例数据的 Excel 模板，供管理员下载后填写。</p>
     *
     * <p><b>模板结构</b>：</p>
     * <pre>
     * | 商品名称 | 商品描述 | 价格   | 库存 | 图片地址                          | 分类ID | 上架状态 |
     * | 示例蓝牙音箱 | 支持立体声播放... | 299.00 | 50   | https://example.com/...jpg | 6      | 1        |
     * </pre>
     *
     * <p><b>教学点</b>：提供模板的好处：</p>
     * <ul>
     *   <li>降低用户填写错误的概率</li>
     *   <li>表头格式固定，导入时可以准确匹配列名</li>
     *   <li>示例数据帮助用户理解每列的填写要求</li>
     * </ul>
     *
     * @return 模板 Excel 文件的字节数组
     * @throws IOException 文件写入异常
     */
    public static byte[] exportProductImportTemplate() throws IOException {
        String[] headers = {"商品名称", "商品描述", "价格", "库存", "图片地址", "分类ID", "上架状态"};
        Map<String, Object> sample = new LinkedHashMap<String, Object>();
        sample.put("商品名称", "示例蓝牙音箱");
        sample.put("商品描述", "支持立体声播放，适合客厅和桌面场景。");
        sample.put("价格", "299.00");
        sample.put("库存", "50");
        sample.put("图片地址", "https://example.com/product-speaker.jpg");
        sample.put("分类ID", "6");
        sample.put("上架状态", "1");
        return exportExcel(headers, Collections.singletonList(sample));
    }

    // ==================== 导入方法 ====================

    /*
     * 解析商品导入的 Excel 文件。
     *
     * <p><b>功能</b>：将用户上传的 Excel 文件解析为 {@code List<Product>}，
     * 供 Service 层批量保存到数据库。</p>
     *
     * <p><b>解析流程</b>：</p>
     * <ol>
     *   <li>使用 {@code WorkbookFactory} 自动识别 .xls/.xlsx 格式</li>
     *   <li>读取第一行作为表头，建立列名到字段的映射</li>
     *   <li>校验是否包含所有必要的表头列</li>
     *   <li>逐行读取数据，跳过空行</li>
     *   <li>将每行数据转换为 {@code Product} 对象</li>
     *   <li>对数值类型进行解析和校验</li>
     * </ol>
     *
     * <p><b>使用示例</b>：</p>
     * <pre>{@code
     * @PostMapping("/products/import")
     * public Result<Void> importProducts(@RequestParam MultipartFile file) throws IOException {
     *     List<Product> products = ExcelUtil.parseProducts(file.getInputStream());
     *     productService.batchSave(products);
     *     return Result.success();
     * }
     * }</pre>
     *
     * <p><b>错误处理</b>：</p>
     * <ul>
     *   <li>缺少必要列 → 抛出 {@code IllegalArgumentException("Excel 缺少必要列：xxx")}</li>
     *   <li>必填字段为空 → 抛出 {@code IllegalArgumentException("第 N 行的"xxx"不能为空")}</li>
     *   <li>数值格式错误 → 抛出 {@code IllegalArgumentException("第 N 行的"xxx"格式不正确")}</li>
     * </ul>
     *
     * @param inputStream Excel 文件的输入流
     * @return 解析后的商品列表
     * @throws IOException              文件读取异常
     * @throws IllegalArgumentException 数据格式错误时抛出，包含具体的错误行和字段信息
     */
    public static List<Product> parseProducts(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // 教学点：WorkbookFactory.create() 能自动识别 .xls 和 .xlsx 格式
            if (workbook.getNumberOfSheets() == 0) {
                return Collections.emptyList();
            }

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                return Collections.emptyList();  // 只有表头或空表
            }

            // 教学点：FormulaEvaluator 用于计算公式单元格的值
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            // 教学点：DataFormatter 用于将不同类型的单元格统一格式化为字符串
            DataFormatter formatter = new DataFormatter(Locale.CHINA);

            // 读取表头，建立列名到列索引的映射
            Map<String, Integer> headerIndexMap = readHeaderIndexMap(sheet.getRow(sheet.getFirstRowNum()), formatter,
                    evaluator);
            validateProductHeaders(headerIndexMap);

            // 逐行解析数据
            List<Product> products = new ArrayList<Product>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isEmptyRow(row, formatter, evaluator)) {
                    continue;  // 跳过空行
                }

                Product product = new Product();
                product.setName(readRequiredCell(row, headerIndexMap, "name", formatter,
                        evaluator, rowIndex, "商品名称"));
                product.setDescription(readOptionalCell(row, headerIndexMap, "description",
                        formatter, evaluator));
                product.setPrice(parseDecimal(readRequiredCell(row, headerIndexMap, "price",
                        formatter, evaluator, rowIndex, "价格"), rowIndex, "价格"));
                product.setStock(parseInteger(readRequiredCell(row, headerIndexMap, "stock",
                        formatter, evaluator, rowIndex, "库存"), rowIndex, "库存"));
                product.setImage(readOptionalCell(row, headerIndexMap, "image", formatter, evaluator));
                product.setCategoryId(parseInteger(readRequiredCell(row, headerIndexMap, "categoryId",
                        formatter, evaluator, rowIndex, "分类ID"), rowIndex, "分类ID"));
                product.setStatus(parseStatus(readOptionalCell(row, headerIndexMap, "status",
                        formatter, evaluator), rowIndex));
                products.add(product);
            }

            return products;
        } catch (IllegalArgumentException ex) {
            throw ex;  // 业务校验异常直接抛出
        } catch (Exception ex) {
            throw new IOException("解析 Excel 文件失败", ex);
        }
    }

    // ==================== 私有辅助方法 ====================

    /*
     * 读取表头行，建立列名到列索引的映射。
     *
     * <p><b>教学点</b>：</p>
     * <ol>
     *   <li>遍历表头行的每个单元格</li>
     *   <li>通过 {@link PRODUCT_HEADER_MAPPING} 将中文表头名转换为字段名</li>
     *   <li>忽略无法识别的表头列（容错性）</li>
     * </ol>
     *
     * <p><b>示例</b>：如果表头为 ["商品名称", "价格", "库存"]，
     * 则返回 {"name" -> 0, "price" -> 1, "stock" -> 2}</p>
     *
     * @param headerRow 表头行对象
     * @param formatter 数据格式化器
     * @param evaluator 公式计算器
     * @return 字段名到列索引的映射
     */
    private static Map<String, Integer> readHeaderIndexMap(Row headerRow,
                                                           DataFormatter formatter,
                                                           FormulaEvaluator evaluator) {
        Map<String, Integer> headerIndexMap = new HashMap<String, Integer>();
        if (headerRow == null) {
            return headerIndexMap;
        }

        for (int cellIndex = headerRow.getFirstCellNum(); cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            if (cellIndex < 0) {
                continue;
            }
            String headerName = readCell(headerRow.getCell(cellIndex), formatter, evaluator);
            if (headerName == null || headerName.trim().isEmpty()) {
                continue;
            }
            // 教学点：通过映射表将表头名标准化为字段名
            String normalizedHeader = PRODUCT_HEADER_MAPPING.get(normalizeHeader(headerName));
            if (normalizedHeader != null) {
                headerIndexMap.put(normalizedHeader, cellIndex);
            }
        }
        return headerIndexMap;
    }

    /*
     * 校验表头是否包含所有必要的列。
     *
     * <p><b>教学点</b>：在解析数据之前先校验表头，可以尽早发现问题，
     * 避免解析到一半才发现缺少必要列，浪费时间和资源。</p>
     *
     * @param headerIndexMap 字段名到列索引的映射
     * @throws IllegalArgumentException 缺少必要列时抛出，列出所有缺失的列名
     */
    private static void validateProductHeaders(Map<String, Integer> headerIndexMap) {
        List<String> requiredHeaders = new ArrayList<String>();
        if (!headerIndexMap.containsKey("name")) {
            requiredHeaders.add("商品名称");
        }
        if (!headerIndexMap.containsKey("price")) {
            requiredHeaders.add("价格");
        }
        if (!headerIndexMap.containsKey("stock")) {
            requiredHeaders.add("库存");
        }
        if (!headerIndexMap.containsKey("categoryId")) {
            requiredHeaders.add("分类ID");
        }
        if (!requiredHeaders.isEmpty()) {
            throw new IllegalArgumentException("Excel 缺少必要列：" + String.join("、", requiredHeaders));
        }
    }

    /*
     * 读取必填单元格的值。
     *
     * <p><b>与 {@link #readOptionalCell} 的区别</b>：必填单元格如果为空会抛出异常，
     * 可选单元格为空则返回 null。</p>
     *
     * @param row            当前行
     * @param headerIndexMap 表头映射
     * @param fieldKey       字段名
     * @param formatter      格式化器
     * @param evaluator      公式计算器
     * @param rowIndex       当前行号（用于错误提示）
     * @param displayName    列的中文名（用于错误提示）
     * @return 单元格的值（已 trim）
     * @throws IllegalArgumentException 必填字段为空时抛出
     */
    private static String readRequiredCell(Row row,
                                           Map<String, Integer> headerIndexMap,
                                           String fieldKey,
                                           DataFormatter formatter,
                                           FormulaEvaluator evaluator,
                                           int rowIndex,
                                           String displayName) {
        String value = readOptionalCell(row, headerIndexMap, fieldKey, formatter, evaluator);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("第 " + (rowIndex + 1) + " 行的“" + displayName + "”不能为空");
        }
        return value.trim();
    }

    /*
     * 读取可选单元格的值。
     *
     * <p><b>教学点</b>：可选字段为空时返回 null，不会抛异常。
     * 调用方根据业务需要决定是否对 null 值做特殊处理。</p>
     *
     * @param row            当前行
     * @param headerIndexMap 表头映射
     * @param fieldKey       字段名
     * @param formatter      格式化器
     * @param evaluator      公式计算器
     * @return 单元格的值（已 trim），为空时返回 null
     */
    private static String readOptionalCell(Row row,
                                           Map<String, Integer> headerIndexMap,
                                           String fieldKey,
                                           DataFormatter formatter,
                                           FormulaEvaluator evaluator) {
        Integer cellIndex = headerIndexMap.get(fieldKey);
        if (cellIndex == null) {
            return null;  // 表头中没有这一列
        }
        String value = readCell(row.getCell(cellIndex), formatter, evaluator);
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /*
     * 读取单个单元格的值。
     *
     * <p><b>教学点</b>：Excel 中的单元格有多种类型（字符串、数字、公式、布尔等），
     * {@code DataFormatter} 可以将它们统一格式化为字符串。</p>
     * <p>对于公式单元格，需要使用 {@code FormulaEvaluator} 先计算公式结果，
     * 再格式化为字符串。</p>
     *
     * @param cell      单元格对象
     * @param formatter 数据格式化器
     * @param evaluator 公式计算器
     * @return 单元格的字符串值，单元格为空时返回 null
     */
    private static String readCell(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.FORMULA) {
            // 教学点：公式单元格需要先计算再格式化
            return formatter.formatCellValue(cell, evaluator);
        }
        return formatter.formatCellValue(cell);
    }

    /*
     * 判断是否为空行。
     *
     * <p><b>教学点</b>：Excel 文件中经常存在空行（用户可能不小心多打了回车），
     * 解析时需要跳过这些空行，避免创建出字段全为 null 的 Product 对象。</p>
     *
     * @param row       行对象
     * @param formatter 格式化器
     * @param evaluator 公式计算器
     * @return 如果所有单元格都为空则返回 true
     */
    private static boolean isEmptyRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (row == null) {
            return true;
        }
        for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
            if (cellIndex < 0) {
                continue;
            }
            String value = readCell(row.getCell(cellIndex), formatter, evaluator);
            if (value != null && !value.trim().isEmpty()) {
                return false;  // 只要有一个非空单元格，就不是空行
            }
        }
        return true;
    }

    /*
     * 将字符串解析为 BigDecimal（用于价格字段）。
     *
     * <p><b>教学点</b>：为什么价格用 BigDecimal 而不是 double？</p>
     * <ul>
     *   <li>double 有精度问题：{@code 0.1 + 0.2 != 0.3}（浮点数的二进制表示限制）</li>
     *   <li>BigDecimal 是精确的十进制运算，适合金融计算</li>
     *   <li>电商系统中价格精确到分，不能有误差</li>
     * </ul>
     *
     * @param value       字符串值
     * @param rowIndex    行号（用于错误提示）
     * @param displayName 列名（用于错误提示）
     * @return 解析后的 BigDecimal 值
     * @throws IllegalArgumentException 格式不正确时抛出
     */
    private static BigDecimal parseDecimal(String value, int rowIndex, String displayName) {
        try {
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("第 " + (rowIndex + 1) + " 行的“" + displayName + "”格式不正确");
        }
    }

    /*
     * 将字符串解析为 Integer（用于库存、分类ID等整数字段）。
     *
     * <p><b>教学点</b>：Excel 中数字可能以浮点数形式存储（如 50.0），
     * 这里做了兼容处理：如果包含小数点，先转为 BigDecimal 再取整数部分。</p>
     *
     * @param value       字符串值
     * @param rowIndex    行号
     * @param displayName 列名
     * @return 解析后的 Integer 值
     * @throws IllegalArgumentException 格式不正确时抛出
     */
    private static Integer parseInteger(String value, int rowIndex, String displayName) {
        try {
            if (value.contains(".")) {
                BigDecimal decimal = new BigDecimal(value.trim());
                return decimal.intValueExact();  // 如果有小数部分会抛异常
            }
            return Integer.valueOf(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("第 " + (rowIndex + 1) + " 行的“" + displayName + "”格式不正确");
        }
    }

    /*
     * 解析上架状态字段。
     *
     * <p><b>支持的格式</b>：</p>
     * <ul>
     *   <li>"1" 或 "true" 或 "上架" → 1（上架）</li>
     *   <li>"0" 或 "false" 或 "下架" → 0（下架）</li>
     *   <li>为空 → 默认 1（上架）</li>
     * </ul>
     *
     * <p><b>教学点</b>：对用户友好的输入做兼容处理，降低填写门槛。</p>
     *
     * @param value    字符串值
     * @param rowIndex 行号
     * @return 状态值（0 或 1）
     * @throws IllegalArgumentException 值不合法时抛出
     */
    private static Integer parseStatus(String value, int rowIndex) {
        if (value == null || value.trim().isEmpty()) {
            return 1;  // 默认上架
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("1".equals(normalized) || "true".equals(normalized) || "上架".equals(normalized)) {
            return 1;
        }
        if ("0".equals(normalized) || "false".equals(normalized) || "下架".equals(normalized)) {
            return 0;
        }
        throw new IllegalArgumentException("第 " + (rowIndex + 1) + " 行的“上架状态”只能填写 1/0 或 上架/下架");
    }

    /*
     * 标准化表头名称。
     *
     * <p><b>处理</b>：去空格、转小写，使表头匹配更灵活。</p>
     *
     * <p><b>示例</b>：{@code "商品 名称"} → {@code "商品名称"}，{@code "Price"} → {@code "price"}</p>
     *
     * @param header 原始表头名
     * @return 标准化后的表头名
     */
    private static String normalizeHeader(String header) {
        return header == null ? "" : header.trim().replace(" ", "").toLowerCase(Locale.ROOT);
    }

    /*
     * 创建商品表头映射表。
     *
     * <p><b>映射关系</b>：Excel 表头名 → Product 字段名</p>
     *
     * <p><b>教学点</b>：支持中文和英文两种表头，提高用户体验。
     * 用户可以用中文表头（更直观）或英文表头（更通用）。</p>
     *
     * @return 表头映射表
     */
    private static Map<String, String> createProductHeaderMapping() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("商品名称", "name");
        mapping.put("name", "name");
        mapping.put("商品描述", "description");
        mapping.put("description", "description");
        mapping.put("价格", "price");
        mapping.put("price", "price");
        mapping.put("库存", "stock");
        mapping.put("stock", "stock");
        mapping.put("图片地址", "image");
        mapping.put("图片", "image");
        mapping.put("image", "image");
        mapping.put("分类id", "categoryId");
        mapping.put("categoryid", "categoryId");
        mapping.put("上架状态", "status");
        mapping.put("状态", "status");
        mapping.put("status", "status");
        return mapping;
    }
}
