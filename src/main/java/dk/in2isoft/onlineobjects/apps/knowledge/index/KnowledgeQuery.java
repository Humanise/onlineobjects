package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.modules.index.IndexQuery;

public class KnowledgeQuery extends IndexQuery {

	private Collection<String> type;
	private String subset;
	private List<Long> wordIds;
	private List<Long> tagIds;
	private List<Long> authorIds;
	private Boolean inbox;
	private Boolean favorite;
	
	public void setText(String text) {
		this.text = text;
	}

	public Collection<String> getType() {
		return type;
	}

	public void setType(Collection<String> type) {
		this.type = type;
	}

	public String getSubset() {
		return subset;
	}

	public void setSubset(String subset) {
		this.subset = subset;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<Long> getWordIds() {
		return wordIds;
	}

	public void setWordIds(List<Long> wordIds) {
		this.wordIds = wordIds;
	}

	public List<Long> getTagIds() {
		return tagIds;
	}

	public void setTagIds(List<Long> tagIds) {
		this.tagIds = tagIds;
	}

	public List<Long> getAuthorIds() {
		return authorIds;
	}

	public void setAuthorIds(List<Long> authorIds) {
		this.authorIds = authorIds;
	}
	
	public void setInbox(Boolean inbox) {
		this.inbox = inbox;
	}
	
	public void setFavorite(Boolean favorite) {
		this.favorite = favorite;
	}
	
	public static String build(KnowledgeQuery query) {
		String[] textParts = Strings.getWords(query.getText());

		StringBuilder indexQuery = new StringBuilder();

		Collection<String> types = query.getType();
		if (types != null && !types.isEmpty()) {
			if (types.contains("any")) {
				types = Lists.newArrayList(InternetAddress.class.getSimpleName(),Statement.class.getSimpleName(),Question.class.getSimpleName(),Hypothesis.class.getSimpleName());
			}
			indexQuery.append("(");
			for (Iterator<String> i = types.iterator(); i.hasNext();) {
				String type = (String) i.next();
				indexQuery.append("type:").append(QueryParser.escape(type)).append("");
				if (i.hasNext()) {
					indexQuery.append(" OR ");
				}
			}
			indexQuery.append(")");
		}

		for (String string : textParts) {
			if (indexQuery.length() > 0) {
				indexQuery.append(" AND ");
			}
			indexQuery.append("(");
			indexQuery.append("title:").append(QueryParserUtil.escape(string)).append("*^4");
			indexQuery.append(" OR words:").append(QueryParserUtil.escape(string)).append("*^2");
			indexQuery.append(" OR text:").append(QueryParserUtil.escape(string)).append("*");
			indexQuery.append(")");
		}
		if (query.getWordIds() != null) {
			for (Long id : query.getWordIds()) {
				if (indexQuery.length() > 0) {
					indexQuery.append(" AND ");
				}
				indexQuery.append("word:").append(id);
			}
		}
		if (query.getTagIds() != null) {
			for (Long id : query.getTagIds()) {
				if (indexQuery.length() > 0) {
					indexQuery.append(" AND ");
				}
				indexQuery.append("tag:").append(id);
			}
		}
		if (query.getAuthorIds() != null) {
			for (Long id : query.getAuthorIds()) {
				if (indexQuery.length() > 0) {
					indexQuery.append(" AND ");
				}
				indexQuery.append("author:").append(id);
			}
		}
		if ("inbox".equals(query.getSubset()) || (query.inbox!=null && query.inbox==true)) {
			if (indexQuery.length() > 0) {
				indexQuery.append(" AND ");
			}
			indexQuery.append("inbox:yes");
		}
		if ("favorite".equals(query.getSubset()) || (query.favorite!=null && query.favorite==true)) {
			if (indexQuery.length() > 0) {
				indexQuery.append(" AND ");
			}
			indexQuery.append("favorite:yes");
		}
		if ("archive".equals(query.getSubset()) || (query.inbox!=null && query.inbox==false)) {
			if (indexQuery.length() > 0) {
				indexQuery.append(" AND ");
			}
			indexQuery.append("inbox:no");
		}
		return indexQuery.toString();
	}

}
