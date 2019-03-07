package io.allen.fileserver.domain;

import org.bson.types.Binary;

/**
 * 用于存放文件实际数据的实体类
 */
public class MongoFileData {

    private String id;

    private String fileId;

    private Integer sequence;

    private Integer dataSize;

    private Binary data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
    }

    public Binary getData() {
        return data;
    }

    public void setData(Binary data) {
        this.data = data;
    }
}
