package dk.in2isoft.onlineobjects.apps.words.views;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.modules.language.LanguageStatistic;
import dk.in2isoft.onlineobjects.modules.language.LanguageStatisticsDataProvider;
import dk.in2isoft.onlineobjects.ui.Request;

public class WordsStatisticsView extends AbstractView {

	private LanguageStatisticsDataProvider statisticsDataProvider;
	private List<LanguageStatistic> languages;

	@Override
	protected void before(Request request) throws Exception {
		Map<Locale, List<LanguageStatistic>> data = statisticsDataProvider.getData().getCategoriesByLanguage();
		this.languages = data.get(request.getLocale());
	}

	public List<LanguageStatistic> getLanguages() {
		return languages;
	}

	// Wiring...

	public void setStatisticsDataProvider(LanguageStatisticsDataProvider statisticsDataProvider) {
		this.statisticsDataProvider = statisticsDataProvider;
	}
}
