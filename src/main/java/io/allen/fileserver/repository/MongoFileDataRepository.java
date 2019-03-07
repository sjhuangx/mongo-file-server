package io.allen.fileserver.repository;

import io.allen.fileserver.domain.MongoFileData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoFileDataRepository extends MongoRepository<MongoFileData, String> {

    /**
     * 按照序号获取文件内容
     *
     * @param fileId 文件id
     * @return 文件数据内容列表
     */
    List<MongoFileData> findByFileId(String fileId);

    /**
     * 删除文件分片数据
     *
     * @param fileId 文件id
     */
    void deleteByFileId(String fileId);
}
