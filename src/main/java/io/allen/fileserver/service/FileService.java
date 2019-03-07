package io.allen.fileserver.service;


import io.allen.fileserver.domain.MongoFile;
import io.allen.fileserver.domain.MongoFileData;
import io.allen.fileserver.repository.MongoFileDataRepository;
import io.allen.fileserver.repository.MongoFileRepository;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private static Logger logger = LoggerFactory.getLogger(FileService.class);

    /**
     * 默认的 chunk size 大小 8MB
     */
    private static final int DEFAULT_CHUNK_SIZE = 1024 * 1024 * 8;

    @Autowired
    public MongoFileRepository fileRepository;

    @Autowired
    private MongoFileDataRepository fileDataRepository;

    /**
     * 获取文件信息
     *
     * @param id 文件id
     * @return 文件信息
     */
    public Optional<MongoFile> getFileById(String id) {
        return fileRepository.findById(id);
    }

    /**
     * 获取所有文件信息.
     *
     * @param limit  the limit
     * @param offset the offset
     * @return 文件信息列表
     */
    public List<MongoFile> listFiles(Integer limit, Integer offset) {
        if (limit == null) {
            limit = 200;
        }
        if (offset == null) {
            offset = 0;
        }
        int pageSize = limit;
        int current = offset / pageSize;
        Sort sort = new Sort(Sort.Direction.DESC, "uploadDate");
        Pageable pageable = PageRequest.of(current, pageSize, sort);

        Page<MongoFile> page = fileRepository.findAll(pageable);
        return page.getContent();
    }

    /**
     * 获取分页文件信息
     *
     * @param current  当前页码
     * @param pageSize 每页数量
     * @return 文件信息列表
     */
    public Page<MongoFile> pageFiles(Integer current, Integer pageSize) {
        if (current == null) {
            current = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Sort sort = new Sort(Sort.Direction.DESC, "uploadDate");
        Pageable pageable = PageRequest.of(current, pageSize, sort);

        return fileRepository.findAll(pageable);
    }

    /**
     * 获取文件数据
     *
     * @param fileId   文件id
     * @param response http请求对象
     */
    public void getFileData(String fileId, HttpServletResponse response) {
        if (fileId == null || fileId.isEmpty()) {
            return;
        }

        // 去除文件扩展名
        int index = fileId.indexOf('.');
        if (index != -1) {
            fileId = fileId.substring(0, index);
        }

        long start = System.currentTimeMillis();
        Optional<MongoFile> orFile = getFileById(fileId);
        if (orFile.isPresent()) {
            try {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName*=UTF-8''" + URLEncoder.encode(orFile.get().getName(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("setHeader file size:", e);
            }

            MongoFile file = orFile.get();
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            response.setHeader(HttpHeaders.CONTENT_LENGTH, file.getSize() + "");
            response.setHeader("Connection", "close");

            try (OutputStream os = response.getOutputStream()) {
                getFileData(fileId, os);
            } catch (IOException e) {
                response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
            long end2 = System.currentTimeMillis();
            logger.info("send file: {} data finish(ms): {}", fileId, (end2 - start));

            file.setDownloadCount(file.getDownloadCount() + 1);
            file.setLatestDownload(new Date());
            fileRepository.save(file);
        }
    }

    /**
     * 将文件内容写入到OutputStream中
     *
     * @param fileId 文件id
     * @param os     OutputStream输出流
     */
    public void getFileData(String fileId, OutputStream os) {
        long start = System.currentTimeMillis();
        Optional<MongoFile> orFile = getFileById(fileId);
        if (orFile.isPresent()) {
            List<MongoFileData> fileDataList = fileDataRepository.findByFileId(fileId);
            fileDataList.sort(Comparator.comparing(MongoFileData::getSequence));
            long end1 = System.currentTimeMillis();
            logger.info("get file: {} data from mongo(ms): {}", fileId, (end1 - start));

            try {
                for (MongoFileData fileData : fileDataList) {
                    os.write(fileData.getData().getData());
                }
                os.flush();
                long end2 = System.currentTimeMillis();
                logger.info("send file: {} data finish(ms): {}", fileId, (end2 - start));

                MongoFile file = orFile.get();
                file.setDownloadCount(file.getDownloadCount() + 1);
                file.setLatestDownload(new Date());
                fileRepository.save(file);
            } catch (IOException e) {
                logger.error("get file data error", e);
            }
        }
    }

    /**
     * 上传文件
     * 文件会按照 DEFAULT_CHUNK_SIZE 大小分片存储在MongoDB中
     *
     * @param file 上传文件对象
     * @return 文件保存结果
     */
    public MongoFile uploadFile(MultipartFile file) {
        long start = System.currentTimeMillis();
        logger.info("upload file start: {} at {}", file.getOriginalFilename(), new Date());
        try {
            MongoFile f = new MongoFile();
            f.setName(file.getOriginalFilename());
            f.setContentType(file.getContentType());
            f.setSize(file.getSize());
            f.setUploadDate(new Date());
            f.setChunkSize(DEFAULT_CHUNK_SIZE);
            MongoFile returnFile = fileRepository.save(f);

            InputStream is = file.getInputStream();
            byte[] buf = new byte[DEFAULT_CHUNK_SIZE];
            int chunkCount = 0;
            int byteRead;
            while ((byteRead = is.read(buf)) != -1) {
                saveFileData(returnFile.getId(), chunkCount, byteRead, buf);
                chunkCount += 1;
            }
            returnFile.setPath("/files/" + returnFile.getId());
            returnFile.setDataPath("/files/data/" + returnFile.getId());
            returnFile.setChunkCount(chunkCount);
            returnFile = fileRepository.save(returnFile);
            long end1 = System.currentTimeMillis();
            logger.info("upload file end: {} at {} fileId: {} and spend(ms): {}",
                    file.getOriginalFilename(), new Date(), returnFile.getId(), (end1 - start));

            return returnFile;
        } catch (IOException ex) {
            logger.error("upload file error", ex);
            return null;
        }
    }

    /**
     * 复制文件
     *
     * @param fileId 文件id
     * @return 复制结果
     */
    public MongoFile copyFile(String fileId) {
        Optional<MongoFile> orFile = getFileById(fileId);
        if (orFile.isPresent()) {
            List<MongoFileData> fileDataList = fileDataRepository.findByFileId(fileId);
            fileDataList.sort(Comparator.comparing(MongoFileData::getSequence));

            MongoFile f = new MongoFile();
            f.setName(orFile.get().getName());
            f.setContentType(orFile.get().getContentType());
            f.setSize(orFile.get().getSize());
            f.setUploadDate(new Date());
            f.setChunkSize(DEFAULT_CHUNK_SIZE);
            final MongoFile returnFile = fileRepository.save(f);

            fileDataList.forEach(data -> {
                MongoFileData fileData = new MongoFileData();
                fileData.setFileId(returnFile.getId());
                fileData.setSequence(data.getSequence());
                fileData.setDataSize(data.getDataSize());
                fileData.setData(data.getData());
                fileDataRepository.insert(fileData);
            });

            return returnFile;
        }
        return null;
    }

    /**
     * 删除文件.
     *
     * @param id 文件id
     * @return the string
     */
    public String deleteFile(String id) {
        logger.info("delete file: {}", id);
        fileRepository.deleteById(id);
        fileDataRepository.deleteByFileId(id);
        return "{\"code\":0,\"message\":\"success\",\"data\":\"" + id + "\"}";
    }

    /**
     * 保存文件数据块
     *
     * @param fileId     文件id
     * @param chunkCount 当前数据块序号
     * @param byteRead   读取的数据长度
     * @param buf        数据块内容
     */
    private void saveFileData(String fileId, int chunkCount, int byteRead, byte[] buf) {
        MongoFileData fileData = new MongoFileData();
        fileData.setFileId(fileId);
        fileData.setSequence(chunkCount);

        // 保存的数据长度小于buf长度则拷贝到新数组
        if (byteRead < buf.length) {
            byte[] readBuf = new byte[byteRead];
            System.arraycopy(buf, 0, readBuf, 0, byteRead);
            fileData.setDataSize(readBuf.length);
            fileData.setData(new Binary(readBuf));
        } else {
            fileData.setDataSize(buf.length);
            fileData.setData(new Binary(buf));
        }

        fileDataRepository.insert(fileData);
    }
}
