package io.allen.fileserver.controller;


import io.allen.fileserver.domain.MongoFile;
import io.allen.fileserver.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件上传下载接口的抽象基类, 可以直接继承使用
 *
 * 如下注解为示例：
 * 注解 CrossOrigin(origins = "*", maxAge = 3600) // 允许所有域名访问
 * 注解 Controller
 * 注解 RequestMapping("/files")
 * @author allen
 */
public abstract class AbstractFileServerController {

    @Autowired
    private FileService fileService;

    /**
     * 分页查询文件列表.
     *
     * @param current the current
     * @param pageSize the page size
     * @return the list
     */
    @GetMapping("")
    @ResponseBody
    public List<MongoFile> listFilesByPage(@RequestParam(required = false) Integer current,
                                           @RequestParam(required = false) Integer pageSize) {
        return fileService.listFiles(current, pageSize);
    }

    /**
     * 分页查询文件列表.
     *
     * @param limit the limit
     * @param offset the offset
     * @return the list
     */
    @GetMapping("all")
    @ResponseBody
    public List<MongoFile> listFiles(@RequestParam(required = false, defaultValue = "200") Integer limit,
                                @RequestParam(required = false, defaultValue = "0") Integer offset) {
        return fileService.listFiles(limit, offset);
    }

    /**
     * 获取文件信息.
     *
     * @param id the id
     * @return the file
     */
    @GetMapping("{id}")
    @ResponseBody
    public MongoFile fileInfo(@PathVariable String id) {
        return fileService.getFileById(id).orElse(null);
    }

    /**
     * 获取文件数据.
     *
     * @param id the id
     * @param response the response
     */
    @GetMapping("data/{id}")
    public void getFileData(@PathVariable String id, HttpServletResponse response) {
        fileService.getFileData(id, response);
    }

    /**
     * 上传接口.
     *
     * @param file the file
     * @return the file
     */
    @PostMapping("/upload")
    @ResponseBody
    public MongoFile uploadFile(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    /**
     * 删除文件.
     *
     * @param id the id
     * @return the string
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteFile(@PathVariable String id) {
        return fileService.deleteFile(id);
    }
}
