package dk.in2isoft.onlineobjects.apps.knowledge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.api.KnowledgeListRow;
import dk.in2isoft.onlineobjects.apps.knowledge.index.KnowledgeQuery;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.modules.index.IndexManager;
import dk.in2isoft.onlineobjects.modules.index.IndexSearchQuery;
import dk.in2isoft.onlineobjects.modules.index.IndexSearchResult;
import dk.in2isoft.onlineobjects.modules.index.IndexService;
import dk.in2isoft.onlineobjects.modules.knowledge.KnowledgeListQuery;
import dk.in2isoft.onlineobjects.ui.Request;

public class KnowledgeSearcher {

	private ModelService modelService;
	private IndexService indexService;
	private static Logger log = LogManager.getLogger(KnowledgeSearcher.class);

	public SearchResult<Entity> search(Request request, int page, int pageSize) throws ExplodingClusterFuckException, SecurityException {
		KnowledgeQuery readerQuery = new KnowledgeQuery();
		readerQuery.setText(request.getString("text"));
		readerQuery.setSubset(request.getString("subset"));
		readerQuery.setType(request.getStrings("type"));
		readerQuery.setPage(page);
		readerQuery.setPageSize(pageSize);
		readerQuery.setWordIds(request.getLongs("tags"));
		readerQuery.setAuthorIds(request.getLongs("authors"));

		return search(readerQuery, request);
	}

	public SearchResult<KnowledgeListRow> searchOptimized(KnowledgeQuery readerQuery, Operator operator) throws ExplodingClusterFuckException, SecurityException, ModelException {
		final Pair<Integer, ListMultimap<String, Long>> idsByType = find(operator, readerQuery);
		List<Long> ids = Lists.newArrayList(idsByType.getValue().values());
		if (ids.isEmpty()) {
			return new SearchResult<KnowledgeListRow>(new ArrayList<>(), 0);
		}
		KnowledgeListQuery query = new KnowledgeListQuery(operator);
		query.withIds(ids);
		SearchResult<KnowledgeListRow> result = modelService.search(query, operator);
		List<KnowledgeListRow> list = result.getList();
		if (list.size() != ids.size()) {
			log.error("Inconsistent database results: actual={} vs expected={}", list.size(), ids.size());
		}
		for (Iterator<KnowledgeListRow> i = list.iterator(); i.hasNext();) {
			KnowledgeListRow row = i.next();
			if (Strings.isBlank(row.getType())) {
				log.warn("Missing type: id={}, text={}", row.getId(), row.getText());
				i.remove();
			}
		}
		Collections.sort(list, new Comparator<KnowledgeListRow>() {

			public int compare(KnowledgeListRow o1, KnowledgeListRow o2) {
				int index1 = ids.indexOf(o1.getId());
				int index2 = ids.indexOf(o2.getId());
				if (index1 > index2) {
					return 1;
				} else if (index2 > index1) {
					return -1;
				}
				return 0;
			}
		});

		result.setTotalCount(idsByType.getKey());
		return result;
	}

	public SearchResult<Entity> search(KnowledgeQuery readerQuery, Operator privileged) throws ExplodingClusterFuckException, SecurityException {
		if (privileged==null) {
			throw new SecurityException("No privileged provided");
		}
		Pair<Integer, ListMultimap<String, Long>> found = find(privileged, readerQuery);
		final ListMultimap<String, Long> idsByType = found.getValue();
		final List<Long> ids = Lists.newArrayList(idsByType.values());

		List<Entity> list = Lists.newArrayList();
		{
			List<Long> addressIds = idsByType.get(InternetAddress.class.getSimpleName().toLowerCase());
			if (!addressIds.isEmpty()) {
				Query<InternetAddress> query = Query.after(InternetAddress.class).withIds(addressIds).as(privileged);

				list.addAll(modelService.list(query, privileged));
			}
		}
		{
			List<Long> partIds = idsByType.get(Statement.class.getSimpleName().toLowerCase());
			if (!partIds.isEmpty()) {
				Query<Statement> query = Query.after(Statement.class).withIds(partIds).as(privileged);

				list.addAll(modelService.list(query, privileged));
			}
		}
		{
			List<Long> partIds = idsByType.get(Question.class.getSimpleName().toLowerCase());
			if (!partIds.isEmpty()) {
				Query<Question> query = Query.after(Question.class).withIds(partIds).as(privileged);

				list.addAll(modelService.list(query, privileged));
			}
		}
		{
			List<Long> partIds = idsByType.get(Hypothesis.class.getSimpleName().toLowerCase());
			if (!partIds.isEmpty()) {
				Query<Hypothesis> query = Query.after(Hypothesis.class).withIds(partIds).as(privileged);

				list.addAll(modelService.list(query, privileged));
			}
		}

		sortByIds(list, ids);

		int totalCount = found.getKey();


		return new SearchResult<>(list, totalCount);
	}

	private void sortByIds(List<Entity> list, final List<Long> ids) {
		Collections.sort(list, new Comparator<Entity>() {

			public int compare(Entity o1, Entity o2) {
				int index1 = ids.indexOf(o1.getId());
				int index2 = ids.indexOf(o2.getId());
				if (index1 > index2) {
					return 1;
				} else if (index2 > index1) {
					return -1;
				}
				return 0;
			}
		});
	}

	private Pair<Integer,ListMultimap<String, Long>> find(Privileged privileged, KnowledgeQuery query) throws ExplodingClusterFuckException {
		IndexManager index = getIndex(privileged);
		if (Strings.isBlank(query.getText()) && Strings.isBlank(query.getSubset())) {
			ListMultimap<String, Long> idsByType = index.getIdsByType();
			return new Pair<Integer, ListMultimap<String,Long>>(idsByType.size(), idsByType);
		}
		final ListMultimap<String, Long> ids = LinkedListMultimap.create();

		IndexSearchQuery indexQuery = new IndexSearchQuery(KnowledgeQuery.build(query));
		indexQuery.setPage(query.getPage());
		indexQuery.setPageSize(query.getPageSize());
		if (Strings.isBlank(query.getText())) {
			indexQuery.addLongOrdering("created",true);
		}
		SearchResult<IndexSearchResult> search = index.search(indexQuery);
		for (IndexSearchResult row : search.getList()) {
			Long id = row.getLong("id");
			String type = row.getString("type");
			ids.put(type, id);
		}
		return new Pair<Integer, ListMultimap<String,Long>>(search.getTotalCount(), ids);
	}

	private IndexManager getIndex(Privileged privileged) {
		return indexService.getIndex("app-reader-user-" + privileged.getIdentity());
	}

	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}

}
