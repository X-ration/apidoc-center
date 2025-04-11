package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.common.SystemConstants;
import com.adam.apidoc_center.common.ik.IKAnalyzer6x;
import com.adam.apidoc_center.domain.GroupInterface;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectGroup;
import com.adam.apidoc_center.repository.GroupInterfaceRepository;
import com.adam.apidoc_center.repository.ProjectGroupRepository;
import com.adam.apidoc_center.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class LuceneService implements InitializingBean, DisposableBean {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectGroupRepository projectGroupRepository;
    @Autowired
    private GroupInterfaceRepository groupInterfaceRepository;

    private final Path indexPath = Paths.get("indexdir");
    private final Analyzer analyzer = new IKAnalyzer6x();
    private Directory directory;
    private IndexWriter indexWriter = null;
    private boolean serviceAvailable = true;

    @Override
    public void afterPropertiesSet() {
        if(Files.notExists(indexPath) || !Files.isDirectory(indexPath)) {
            serviceAvailable = false;
            try {
                Files.createDirectory(indexPath);
            } catch (IOException e) {
                log.error("创建索引目录失败", e);
                return;
            }
        }
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        try {
            directory = FSDirectory.open(indexPath);
        } catch (IOException e) {
            log.error("打开索引目录失败", e);
            serviceAvailable = false;
            return;
        }
        try {
            indexWriter = new IndexWriter(directory, indexWriterConfig);
        } catch (IOException e) {
            log.error("创建IndexWriter对象失败", e);
            serviceAvailable = false;
            return;
        }
        if(!serviceAvailable) {
            log.info("开始创建索引...");
            long startMills = System.currentTimeMillis();

            final Sort sort = Sort.by(Sort.Direction.ASC, "id");
            Page<Project> projectPage = projectRepository.findAll(PageRequest.of(0, SystemConstants.SEARCH_CREATE_INDEX_PAGESIZE, sort));
            writeIndexByClass(projectPage);
            if(projectPage.getTotalPages() > 1) {
                int pageNum = 1;
                do {
                    projectPage = projectRepository.findAll(PageRequest.of(pageNum++, SystemConstants.SEARCH_CREATE_INDEX_PAGESIZE, sort));
                    writeIndexByClass(projectPage);
                } while (pageNum < projectPage.getTotalPages());
            }

            Page<ProjectGroup> projectGroupPage = projectGroupRepository.findAll(PageRequest.of(0, SystemConstants.SEARCH_CREATE_INDEX_PAGESIZE, sort));
            writeIndexByClass(projectGroupPage);
            if(projectGroupPage.getTotalPages() > 1) {
                int pageNum = 1;
                do {
                    projectGroupPage = projectGroupRepository.findAll(PageRequest.of(pageNum++, SystemConstants.SEARCH_CREATE_INDEX_PAGESIZE, sort));
                    writeIndexByClass(projectGroupPage);
                } while (pageNum < projectGroupPage.getTotalPages());
            }

            Page<GroupInterface> groupInterfacePage = groupInterfaceRepository.findAll(PageRequest.of(0, SystemConstants.SEARCH_CREATE_INDEX_PAGESIZE, sort));
            writeIndexByClass(groupInterfacePage);
            if(groupInterfacePage.getTotalPages() > 1) {
                int pageNum = 1;
                do {
                    groupInterfacePage = groupInterfaceRepository.findAll(PageRequest.of(pageNum++, SystemConstants.SEARCH_CREATE_INDEX_PAGESIZE, sort));
                    writeIndexByClass(groupInterfacePage);
                } while (pageNum < groupInterfacePage.getTotalPages());
            }

            try {
                indexWriter.commit();
            } catch (IOException e) {
                log.error("写入索引失败", e);
                return;
            }

            long cost = System.currentTimeMillis() - startMills;
            log.info("索引创建完成，用时{}ms", cost);
        }

        serviceAvailable = true;
    }

    private <T> void writeIndexByClass(Page<T> page) {
        FieldType fieldTypeId = new FieldType();
        fieldTypeId.setIndexOptions(IndexOptions.DOCS);
        fieldTypeId.setStored(true);
        FieldType fieldTypeClass = new FieldType();
        fieldTypeClass.setIndexOptions(IndexOptions.DOCS);
        fieldTypeClass.setStored(true);
        FieldType fieldTypeName = new FieldType();
        fieldTypeName.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldTypeName.setStored(true);
        fieldTypeName.setTokenized(true);
        fieldTypeName.setStoreTermVectors(true);
        fieldTypeName.setStoreTermVectorPositions(true);
        fieldTypeName.setStoreTermVectorOffsets(true);
        FieldType fieldTypeDescription = new FieldType();
        fieldTypeDescription.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldTypeDescription.setStored(true);
        fieldTypeDescription.setTokenized(true);
        fieldTypeDescription.setStoreTermVectors(true);
        fieldTypeDescription.setStoreTermVectorPositions(true);
        fieldTypeDescription.setStoreTermVectorOffsets(true);

        for(T object: page.getContent()) {
            String id,className,name,description;
            if(object instanceof Project) {
                Project project = (Project) object;
                if(project.getAccessMode() == Project.AccessMode.PUBLIC) {
                    id = String.valueOf(project.getId());
                    className = StringConstants.SEARCH_CLASS_PROJECT;
                    name = project.getName();
                    description = project.getDescription();
                } else {
                    log.info("跳过私有项目{}", project.getId());
                    continue;
                }
            } else if(object instanceof ProjectGroup) {
                ProjectGroup projectGroup = (ProjectGroup) object;
                if(projectGroup.getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
                    id = String.valueOf(projectGroup.getId());
                    className = StringConstants.SEARCH_CLASS_GROUP;
                    name = projectGroup.getName();
                    description = "";
                } else {
                    log.info("跳过私有项目分组{}", projectGroup.getId());
                    continue;
                }
            } else if(object instanceof GroupInterface) {
                GroupInterface groupInterface = (GroupInterface) object;
                if(groupInterface.getProjectGroup().getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
                    id = String.valueOf(groupInterface.getId());
                    className = StringConstants.SEARCH_CLASS_INTERFACE;
                    name = groupInterface.getName();
                    description = groupInterface.getDescription();
                } else {
                    log.info("跳过私有项目接口{}", groupInterface.getId());
                    continue;
                }
            } else {
                log.warn("类型{}不做处理", object.getClass().getName());
                continue;
            }
            Document document = new Document();
            document.add(new Field(StringConstants.SEARCH_FIELD_ID, id, fieldTypeId));
            document.add(new Field(StringConstants.SEARCH_FIELD_CLASS, className, fieldTypeClass));
            document.add(new Field(StringConstants.SEARCH_FIELD_NAME, name, fieldTypeName));
            document.add(new Field(StringConstants.SEARCH_FIELD_DESCRIPTION, description == null ? "" : description, fieldTypeDescription));

            try {
                indexWriter.addDocument(document);
            } catch (IOException e) {
                log.error("添加文档失败 {} {}", id, className, e);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        indexWriter.close();
        directory.close();
        log.info("优雅关闭服务");
    }
}