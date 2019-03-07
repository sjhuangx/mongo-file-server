package io.allen.fileserver.domain;

import java.util.Date;

/**
 * 用于记录文件信息实体类
 */
public class MongoFile {
    private String id;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件大小
     */
    private long size;

    /**
     * 上传时间
     */
    private Date uploadDate;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件数据路径
     */
    private String dataPath;

    /**
     * 文件的chunkSize 分块大小
     */
    private Integer chunkSize;

    /**
     * 文件的chunkCount 分块数量
     */
    private Integer chunkCount;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 最近的下载时间
     */
    private Date latestDownload;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public Integer getDownloadCount() {
        if (downloadCount == null) {
            downloadCount = 0;
        }
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Date getLatestDownload() {
        return latestDownload;
    }

    public void setLatestDownload(Date latestDownload) {
        this.latestDownload = latestDownload;
    }
}
