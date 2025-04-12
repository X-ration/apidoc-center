package com.adam.apidoc_center.service;

import com.adam.apidoc_center.business.search.SearchType;
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
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    private final Analyzer analyzer = new IKAnalyzer6x(true);
    private Directory directory;
    private IndexWriter indexWriter = null;
    private boolean serviceAvailable = true;

    public List<String> searchSuggestion(String param, SearchType searchType, int maxSize) {
        Assert.isTrue(maxSize > 0, "searchSuggestion maxSize:" + maxSize + "<=0!");
        Assert.notNull(searchType, "searchSugggestion searchType null");
        if(!serviceAvailable) {
            return new ArrayList<>();
        }

        if(param == null) {
            param = "";
        }
        IndexReader indexReader = null;

        try {
            indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            String[] fields = {StringConstants.SEARCH_FIELD_NAME, StringConstants.SEARCH_FIELD_DESCRIPTION};
//            MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(fields, analyzer);
//            Query keywordQuery = multiFieldQueryParser.parse(param);
            Map<String, Integer> termFreqMap = new HashMap<>();
            prefixQuery(param, searchType, fields, indexReader, indexSearcher, termFreqMap);
            log.debug("termFreqMap {}", termFreqMap);
            List<String> termList = new ArrayList<>(termFreqMap.keySet());
            termList.sort((s1,s2) -> -1 * Integer.compare(termFreqMap.get(s1), termFreqMap.get(s2)));
            log.debug("termList {}", termList);
//            return termList.subList(0, maxSize);
            return termList.size()  > maxSize ? termList.subList(0, maxSize) : termList;
        } catch (IOException | ParseException e) {
            log.error("生成搜索建议失败", e);
            return new ArrayList<>();
        } finally {
            if(indexReader != null) {
                try {
                    indexReader.close();
                } catch (IOException e) {
                    log.error("关闭IndexReader失败", e);
                }
            }
        }
    }

    private void prefixQuery(String param, SearchType searchType, String[] fields, IndexReader indexReader, IndexSearcher indexSearcher, Map<String, Integer> termFreqMap) throws IOException, ParseException {
        Query prefixQuery = new PrefixQuery(new Term(fields[0], param));
        TopDocs topDocs = searchTopDoc(prefixQuery, searchType, indexSearcher);

        Set<Integer> docIdSet = new HashSet<>();
        for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
            docIdSet.add(scoreDoc.doc);
            Terms terms = indexReader.getTermVector(scoreDoc.doc, StringConstants.SEARCH_FIELD_NAME);
            countTermFreq(terms, termFreqMap, param);
            terms = indexReader.getTermVector(scoreDoc.doc, StringConstants.SEARCH_FIELD_DESCRIPTION);
            countTermFreq(terms, termFreqMap, param);
        }

        prefixQuery = new PrefixQuery(new Term(fields[1], param));
        topDocs = searchTopDoc(prefixQuery, searchType, indexSearcher);

        for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
            if(docIdSet.contains(scoreDoc.doc))
                continue;
            Terms terms = indexReader.getTermVector(scoreDoc.doc, StringConstants.SEARCH_FIELD_NAME);
            countTermFreq(terms, termFreqMap, param);
            terms = indexReader.getTermVector(scoreDoc.doc, StringConstants.SEARCH_FIELD_DESCRIPTION);
            countTermFreq(terms, termFreqMap, param);
        }
    }

    private TopDocs searchTopDoc(Query query, SearchType searchType, IndexSearcher indexSearcher) throws IOException, ParseException {
        TopDocs topDocs;
        if(searchType == SearchType.ALL) {
            topDocs = indexSearcher.search(query, SystemConstants.SEARCH_SUGGESTION_SEARCH_SIZE);
        } else {
            String className = searchTypeToClassName(searchType);
            QueryParser queryParser = new QueryParser(StringConstants.SEARCH_FIELD_CLASS, analyzer);
            Query classQuery = queryParser.parse(className);
            BooleanClause booleanClause1 = new BooleanClause(query, BooleanClause.Occur.MUST),
                    booleanClause2 = new BooleanClause(classQuery, BooleanClause.Occur.MUST);
            BooleanQuery booleanQuery = new BooleanQuery.Builder()
                    .add(booleanClause1).add(booleanClause2).build();
            topDocs = indexSearcher.search(booleanQuery, SystemConstants.SEARCH_SUGGESTION_SEARCH_SIZE);
        }
        return topDocs;
    }

    private void countTermFreq(Terms terms, Map<String, Integer> termFreqMap, String param) throws IOException {
        if(terms == null)  {
            return;
        }
        TermsEnum termsEnum = terms.iterator();
        BytesRef bytesRef = null;
        while((bytesRef = termsEnum.next()) != null) {
            String termText = bytesRef.utf8ToString();
            if(termText.contains(param)) {
                int termFreq = (int) termsEnum.totalTermFreq();
                if (termFreqMap.containsKey(termText)) {
                    termFreqMap.put(termText, termFreqMap.get(termText) + termFreq);
                } else {
                    termFreqMap.put(termText, termFreq);
                }
            }
        }
    }

    private String searchTypeToClassName(SearchType searchType) {
        switch (searchType) {
            case PROJECT:
                return StringConstants.SEARCH_CLASS_PROJECT;
            case GROUP:
                return StringConstants.SEARCH_CLASS_GROUP;
            case INTERFACE:
                return StringConstants.SEARCH_CLASS_INTERFACE;
            default:
                throw new IllegalArgumentException("无效的searchType");
        }
    }

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
                    className = StringConstants.SEARCH_CLASS_PROJECT;
                    id = className + project.getId();
                    name = project.getName();
                    description = project.getDescription();
                } else {
                    log.info("跳过私有项目{}", project.getId());
                    continue;
                }
            } else if(object instanceof ProjectGroup) {
                ProjectGroup projectGroup = (ProjectGroup) object;
                if(projectGroup.getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
                    className = StringConstants.SEARCH_CLASS_GROUP;
                    id = className + projectGroup.getId();
                    name = projectGroup.getName();
                    description = "";
                } else {
                    log.info("跳过私有项目分组{}", projectGroup.getId());
                    continue;
                }
            } else if(object instanceof GroupInterface) {
                GroupInterface groupInterface = (GroupInterface) object;
                if(groupInterface.getProjectGroup().getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
                    className = StringConstants.SEARCH_CLASS_INTERFACE;
                    id = className + groupInterface.getId();
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