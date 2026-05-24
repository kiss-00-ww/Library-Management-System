package com.library.book.controller;

import com.alibaba.excel.EasyExcel;
import com.library.book.dto.BookImportDTO;
import com.library.book.dto.BookImportListener;
import com.library.book.dto.Response;
import com.library.book.service.BookService;
import com.library.book.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "Book Import")
@RestController
@RequestMapping("/api/admin/book")
public class BookImportController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/template")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("下载图书导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        String fileName = URLEncoder.encode("图书导入模板", "UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), BookImportDTO.class)
                .sheet("图书信息")
                .doWrite(List.of());
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("批量导入图书")
    public Response<Map<String, Object>> importBooks(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Response.fail("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return Response.fail("仅支持 Excel 文件（.xlsx 或 .xls）");
        }

        try {
            BookImportListener listener = new BookImportListener(bookService, categoryService);
            EasyExcel.read(file.getInputStream(), BookImportDTO.class, listener).sheet().doRead();

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", listener.getSuccessCount());
            result.put("failCount", listener.getFailCount());
            result.put("failReasons", listener.getFailReasons());
            return Response.ok(result);
        } catch (IOException e) {
            log.error("导入文件读取失败", e);
            return Response.fail("文件读取失败: " + e.getMessage());
        }
    }
}
