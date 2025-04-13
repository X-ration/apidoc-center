package com.adam.apidoc_center.service;

import com.adam.apidoc_center.business.search.SearchResultPO;
import com.adam.apidoc_center.business.search.SearchType;
import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.common.SystemConstants;
import com.adam.apidoc_center.common.ik.IKAnalyzer6x;
import com.adam.apidoc_center.domain.GroupInterface;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectGroup;
import com.adam.apidoc_center.repository.GroupInterfaceRepository;
import com.adam.apidoc_center.repository.ProjectGroupRepository;
import com.adam.apidoc_center.repository.ProjectRepository;
import com.adam.apidoc_center.util.AssertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
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
import org.springframework.util.CollectionUtils;

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

    public PagedData<SearchResultPO> search(String searchParam, SearchType searchType, int pageNum, int pageSize) {
        AssertUtil.requireNonNull(searchParam, searchType);
        if(!serviceAvailable || StringUtils.isBlank(searchParam)) {
            return PagedData.emptyPagedData(pageNum, pageSize);
        }

        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            String[] fields = {StringConstants.SEARCH_FIELD_NAME, StringConstants.SEARCH_FIELD_DESCRIPTION};
            MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(fields, analyzer);
            Query query = multiFieldQueryParser.parse(searchParam);
            TopDocs topDocs = indexSearcher.search(query, pageSize);
            int totalHit = topDocs.totalHits;
            int totalPage = (int) Math.ceil((double) totalHit / pageSize);
            List<SearchResultPO> searchResultList = new LinkedList<>();
            if(pageNum == 0) {
                for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
                    Document document = indexSearcher.doc(scoreDoc.doc);
                    SearchResultPO searchResultPO = new SearchResultPO(document);
                    highlightTitleDescription(searchResultPO, query, indexSearcher, scoreDoc.doc);
                    searchResultList.add(searchResultPO);
                }
                return new PagedData<>(searchResultList, pageNum, pageSize, totalHit);
            } else if(pageNum >= totalPage) {
                return PagedData.emptyPagedData(pageNum, pageSize);
            } else {
                ScoreDoc lastScoreDoc = topDocs.scoreDocs[topDocs.scoreDocs.length - 1];
                int pageCounter = pageNum;
                while(pageCounter-- > 0) {
                    topDocs = indexSearcher.searchAfter(lastScoreDoc, query, pageSize);
                    lastScoreDoc = topDocs.scoreDocs[topDocs.scoreDocs.length - 1];
                }
                for(ScoreDoc scoreDoc: topDocs.scoreDocs) {
                    Document document = indexSearcher.doc(scoreDoc.doc);
                    SearchResultPO searchResultPO = new SearchResultPO(document);
                    highlightTitleDescription(searchResultPO, query, indexSearcher, scoreDoc.doc);
                    searchResultList.add(searchResultPO);
                }
                return new PagedData<>(searchResultList, pageNum, pageSize, totalHit);
            }
        } catch (IOException | ParseException e) {
            log.error("搜索失败", e);
            return PagedData.emptyPagedData(pageNum, pageSize);
        }
    }

    private void highlightTitleDescription(SearchResultPO searchResultPO, Query query, IndexSearcher indexSearcher, int docId) {
        String nameHighlight = getFieldHighlight(query, indexSearcher, docId, StringConstants.SEARCH_FIELD_NAME);
        if(nameHighlight != null) {
            searchResultPO.setName(nameHighlight);
        }
        String descriptionHighlight = getFieldHighlight(query, indexSearcher, docId, StringConstants.SEARCH_FIELD_DESCRIPTION);
        if(descriptionHighlight != null) {
            String oldDescription = searchResultPO.getDescription();
            String escapedDescriptionHighlight = descriptionHighlight.replaceAll("<span.*?>","").replaceAll("</span>","");
            char dfc = oldDescription.charAt(0), dlc = oldDescription.charAt(oldDescription.length() - 1);
            if(!escapedDescriptionHighlight.startsWith(String.valueOf(dfc))) {
                descriptionHighlight = "..." + descriptionHighlight;
            }
            if(!escapedDescriptionHighlight.endsWith(String.valueOf(dlc))) {
                descriptionHighlight = descriptionHighlight + "...";
            }
            searchResultPO.setDescription(descriptionHighlight);
        }
    }

    private String getFieldHighlight(Query query, IndexSearcher indexSearcher, int docId, String field) {
        QueryScorer queryScorer = new QueryScorer(query, field);
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style=\"color:red;\">", "</span>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, queryScorer);

        try {
            Document document = indexSearcher.doc(docId);
            String fieldValue = document.get(field);
            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearcher.getIndexReader(), docId, field, analyzer);
            Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
            highlighter.setTextFragmenter(fragmenter);
            return highlighter.getBestFragment(tokenStream, fieldValue);
        } catch (IOException | InvalidTokenOffsetsException e) {
            log.error("获取高亮内容失败 docId={} field={}", docId, field, e);
            return null;
        }
    }

    public List<String> searchSuggestion(String param, SearchType searchType, int maxSize) {
        Assert.isTrue(maxSize > 0, "searchSuggestion maxSize:" + maxSize + "<=0!");
        Assert.notNull(searchType, "searchSugggestion searchType null");
        if(!serviceAvailable || StringUtils.isBlank(param)) {
            return new ArrayList<>();
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
        for(T object: page.getContent()) {
            String id,className,classId,name,description;
            if(object instanceof Project) {
                Project project = (Project) object;
                if(project.getAccessMode() == Project.AccessMode.PUBLIC) {
                    className = StringConstants.SEARCH_CLASS_PROJECT;
                    id = String.valueOf(project.getId());
                    classId = className + id;
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
                    id = String.valueOf(projectGroup.getId());
                    classId = className + id;
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
                    id = String.valueOf(groupInterface.getId());
                    classId = className + id;
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

            Document document = newDocument(id, className, classId, name, description);
            try {
                indexWriter.addDocument(document);
            } catch (IOException e) {
                log.error("添加文档失败 {} {}", id, className, e);
            }
        }
    }

    public void createProject(Project project) {
        if(project.getAccessMode() == Project.AccessMode.PUBLIC) {
            String id = String.valueOf(project.getId());
            String className = StringConstants.SEARCH_CLASS_PROJECT;
            String classId = className + id;
            Document document = newDocument(id, className, classId, project.getName(), project.getDescription());
            try {
                indexWriter.addDocument(document);
                indexWriter.commit();
                log.debug("创建项目成功 {}", project.getId());
            } catch (IOException e) {
                log.error("创建项目失败 {}", project.getId(), e);
            }
        }
    }

    public void updateProject(Project project, Project.AccessMode oldAccessMode) {
        Project.AccessMode newAccessMode = project.getAccessMode();
        if(newAccessMode == Project.AccessMode.PUBLIC) {
            String id = String.valueOf(project.getId());
            String className = StringConstants.SEARCH_CLASS_PROJECT;
            String classId = className + id;
            Document document = newDocument(id, className, classId, project.getName(), project.getDescription());
            Term term = new Term(StringConstants.SEARCH_FIELD_CLASS_ID, classId);
            try {
                indexWriter.updateDocument(term, document);
                log.debug("更新项目-updateDocument更新项目{}", project.getId());
            } catch (IOException e) {
                log.error("更新项目-更新项目失败 {}", project.getId(), e);
            }
        } else if(newAccessMode == Project.AccessMode.PRIVATE){
            String id = String.valueOf(project.getId());
            String className = StringConstants.SEARCH_CLASS_PROJECT;
            String classId = className + id;
            Term term = new Term(StringConstants.SEARCH_FIELD_CLASS_ID, classId);
            try {
                indexWriter.deleteDocuments(term);
                log.debug("更新项目-deleteDocuments删除项目{}", project.getId());
            } catch (IOException e) {
                log.error("更新项目-删除项目失败{}", project.getId(), e);
            }
        }
        if(oldAccessMode == Project.AccessMode.PRIVATE && newAccessMode == Project.AccessMode.PUBLIC) {
            List<ProjectGroup> projectGroupList = project.getProjectGroupList();
            if(!CollectionUtils.isEmpty(projectGroupList)) {
                for(int i=0;i<projectGroupList.size();i++) {
                    ProjectGroup projectGroup = projectGroupList.get(i);
                    createGroupInternal(projectGroup, "更新项目-创建分组", false);
                    List<GroupInterface> groupInterfaceList = projectGroup.getGroupInterfaceList();
                    if(!CollectionUtils.isEmpty(groupInterfaceList)) {
                        for(int j=0;j<groupInterfaceList.size();j++) {
                            GroupInterface groupInterface = groupInterfaceList.get(j);
                            createInterfaceInternal(groupInterface, "更新项目-创建接口", false);
                        }
                    }
                }
            }
        } else if(oldAccessMode == Project.AccessMode.PUBLIC && newAccessMode == Project.AccessMode.PRIVATE) {
            List<ProjectGroup> projectGroupList = project.getProjectGroupList();
            if(!CollectionUtils.isEmpty(projectGroupList)) {
                for(int i=0;i<projectGroupList.size();i++) {
                    ProjectGroup projectGroup = projectGroupList.get(i);
                    deleteGroupInternal(projectGroup, "更新项目-删除分组", false);
                    List<GroupInterface> groupInterfaceList = projectGroup.getGroupInterfaceList();
                    if(!CollectionUtils.isEmpty(groupInterfaceList)) {
                        for(int j=0;j<groupInterfaceList.size();j++) {
                            GroupInterface groupInterface = groupInterfaceList.get(j);
                            deleteInterfaceInternal(groupInterface, "更新项目-删除接口", false);
                        }
                    }
                }
            }
        }
        try {
            indexWriter.commit();
            log.debug("更新项目-提交{}", project.getId());
        } catch (IOException e) {
            log.error("更新项目-提交失败 {}", project.getId(), e);
        }
    }

    private void createGroupInternal(ProjectGroup projectGroup, String msg, boolean commit) {
        String id = String.valueOf(projectGroup.getId());
        String className = StringConstants.SEARCH_CLASS_GROUP;
        String classId = className + id;
        Document document = newDocument(id, className, classId, projectGroup.getName(), "");
        try {
            indexWriter.addDocument(document);
            if(commit) {
                indexWriter.commit();
            }
            log.debug(msg + "addDocument分组{} commit={}", projectGroup.getId(), commit);
        } catch (IOException e) {
            log.error(msg + "失败{}", projectGroup.getId(), e);
        }
    }

    private void createInterfaceInternal(GroupInterface groupInterface, String msg, boolean commit) {
        String id = String.valueOf(groupInterface.getId());
        String className = StringConstants.SEARCH_CLASS_INTERFACE;
        String classId = className + id;
        Document document = newDocument(id, className, classId, groupInterface.getName(), groupInterface.getDescription());
        try {
            indexWriter.addDocument(document);
            if(commit) {
                indexWriter.commit();
            }
            log.debug(msg + "addDocument接口{} commit={}", groupInterface.getId(), commit);
        } catch (IOException e) {
            log.error(msg + "失败{}", groupInterface.getId(), e);
        }
    }

    private void deleteGroupInternal(ProjectGroup projectGroup, String msg, boolean commit) {
        String id = String.valueOf(projectGroup.getId());
        String className = StringConstants.SEARCH_CLASS_GROUP;
        String classId = className + id;
        Term term = new Term(StringConstants.SEARCH_FIELD_CLASS_ID, classId);
        try {
            indexWriter.deleteDocuments(term);
            if(commit) {
                indexWriter.commit();
            }
            log.debug(msg + "deleteDocument分组{} commit={}", projectGroup.getId(), commit);
        } catch (IOException e) {
            log.error(msg + "失败{}", projectGroup.getId(), e);
        }
    }

    private void deleteInterfaceInternal(GroupInterface groupInterface, String msg, boolean commit) {
        String id = String.valueOf(groupInterface.getId());
        String className = StringConstants.SEARCH_CLASS_INTERFACE;
        String classId = className + id;
        Term term = new Term(StringConstants.SEARCH_FIELD_CLASS_ID, classId);
        try {
            indexWriter.deleteDocuments(term);
            if(commit) {
                indexWriter.commit();
            }
            log.debug(msg + "deleteDocument接口{} commit={}", groupInterface.getId(), commit);
        } catch (IOException e) {
            log.error(msg + "失败{}", groupInterface.getId(), e);
        }
    }

    public void deleteProject(Project project) {
        if(project.getAccessMode() == Project.AccessMode.PUBLIC) {
            String id = String.valueOf(project.getId());
            String className = StringConstants.SEARCH_CLASS_PROJECT;
            String classId = className + id;
            Term term = new Term(StringConstants.SEARCH_FIELD_CLASS_ID, classId);
            try {
                indexWriter.deleteDocuments(term);
                log.debug("删除项目-deleteDocument项目{}", project.getId());
            } catch (IOException e) {
                log.error("删除项目-删除项目失败{}", project.getId(), e);
            }
            List<ProjectGroup> projectGroupList = project.getProjectGroupList();
            if(!CollectionUtils.isEmpty(projectGroupList)) {
                for(int i=0;i<projectGroupList.size();i++) {
                    ProjectGroup projectGroup = projectGroupList.get(i);
                    deleteGroupInternal(projectGroup, "删除项目-删除分组", false);
                    List<GroupInterface> groupInterfaceList = projectGroup.getGroupInterfaceList();
                    if(!CollectionUtils.isEmpty(groupInterfaceList)) {
                        for(int j=0;j<groupInterfaceList.size();j++) {
                            GroupInterface groupInterface = groupInterfaceList.get(j);
                            deleteInterfaceInternal(groupInterface, "删除项目-删除接口", false);
                        }
                    }
                }
            }

            try {
                indexWriter.commit();
                log.debug("删除项目-提交{}", project.getId());
            } catch (IOException e) {
                log.error("删除项目-提交失败{}", project.getId(), e);
            }
        }
    }

    public void createGroup(ProjectGroup projectGroup) {
        if(projectGroup.getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
            createGroupInternal(projectGroup, "创建分组", true);
        }
    }

    public void updateGroup(ProjectGroup projectGroup) {
        if(projectGroup.getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
            String id = String.valueOf(projectGroup.getId());
            String className = StringConstants.SEARCH_CLASS_GROUP;
            String classId = className + id;
            Document document = newDocument(id, className, classId, projectGroup.getName(), "");
            Term term = new Term(StringConstants.SEARCH_FIELD_CLASS_ID, classId);
            try {
                indexWriter.updateDocument(term, document);
                indexWriter.commit();
                log.debug("更新分组成功{}",projectGroup.getId());
            } catch (IOException e) {
                log.error("更新分组失败{}", projectGroup.getId(), e);
            }
        }
    }

    public void deleteGroup(ProjectGroup projectGroup) {
        if(projectGroup.getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
            deleteGroupInternal(projectGroup, "删除分组-删除分组", false);
            List<GroupInterface> groupInterfaceList = projectGroup.getGroupInterfaceList();
            if(!CollectionUtils.isEmpty(groupInterfaceList)) {
                for(GroupInterface groupInterface: groupInterfaceList) {
                    deleteInterfaceInternal(groupInterface, "删除分组-删除接口", false);
                }
            }
            try {
                indexWriter.commit();
                log.debug("删除分组-提交{}", projectGroup.getId());
            } catch (IOException e) {
                log.error("删除分组-提交失败{}", projectGroup.getId(), e);
            }
        }
    }

    public void createInterface(GroupInterface groupInterface) {
        if(groupInterface.getProjectGroup().getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
            createInterfaceInternal(groupInterface, "创建接口", true);
        }
    }

    public void updateInterface(GroupInterface groupInterface) {
        if(groupInterface.getProjectGroup().getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
            String id = String.valueOf(groupInterface.getId());
            String className = StringConstants.SEARCH_CLASS_INTERFACE;
            String classId = className + id;
            Document document = newDocument(id, className, classId, groupInterface.getName(), groupInterface.getDescription());
            Term term = new Term(StringConstants.SEARCH_FIELD_CLASS_ID, classId);
            try {
                indexWriter.updateDocument(term, document);
                indexWriter.commit();
                log.debug("更新接口成功{}", groupInterface.getId());
            } catch (IOException e) {
                log.error("更新接口失败{}", groupInterface.getId(), e);
            }
        }
    }

    public void deleteInterface(GroupInterface groupInterface) {
        if(groupInterface.getProjectGroup().getProject().getAccessMode() == Project.AccessMode.PUBLIC) {
            deleteInterfaceInternal(groupInterface, "删除接口", true);
        }
    }

    private Document newDocument(String id, String className, String classId, String name, String description) {
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

        Document document = new Document();
        document.add(new Field(StringConstants.SEARCH_FIELD_ID, id, fieldTypeId));
        document.add(new Field(StringConstants.SEARCH_FIELD_CLASS, className, fieldTypeClass));
        document.add(new StringField(StringConstants.SEARCH_FIELD_CLASS_ID, classId, Field.Store.YES));
        document.add(new Field(StringConstants.SEARCH_FIELD_NAME, name, fieldTypeName));
        document.add(new Field(StringConstants.SEARCH_FIELD_DESCRIPTION, description == null ? "" : description, fieldTypeDescription));
        return document;
    }

    @Override
    public void destroy() throws Exception {
        indexWriter.close();
        directory.close();
        log.info("优雅关闭服务");
    }
}