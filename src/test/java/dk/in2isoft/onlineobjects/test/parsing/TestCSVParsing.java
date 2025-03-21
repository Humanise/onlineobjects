package dk.in2isoft.onlineobjects.test.parsing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.words.WordsModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.language.WordImportRow;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.test.AbstractSpringTestCase;
import dk.in2isoft.onlineobjects.test.EssentialTests;

@Category(EssentialTests.class)
public class TestCSVParsing extends AbstractSpringTestCase {
	
	private static Logger log = LogManager.getLogger(TestCSVParsing.class);
			
	@Autowired
	private WordsModelService wordsModelService;

	@Test
	public void testSimple() throws Exception {
		try (
			FileReader fileReader = new FileReader(getTestFile("language/names.csv"));
			CSVReader reader = new CSVReader(fileReader,';')
		) {
		    String [] nextLine;
		    while ((nextLine = reader.readNext()) != null) {
		        log.info(Arrays.toString(nextLine));
		    }
		}
	}
	
	@Test
	public void testToModel() throws FileNotFoundException, IOException, ModelException, BadRequestException, SecurityException, NotFoundException {
		ColumnPositionMappingStrategy<WordImportRow> strat = new ColumnPositionMappingStrategy<WordImportRow>();
		strat.setType(WordImportRow.class);
		String[] columns = new String[] {"text", "category", "language"}; // the fields to bind do in your JavaBean
		strat.setColumnMapping(columns);
		
		CsvToBean<WordImportRow> csv = new CsvToBean<WordImportRow>();
		
		Operator operator = modelService.newAdminOperator();
		
		try (
			FileReader fileReader = new FileReader(getTestFile("language/names.csv"));
			CSVReader reader = new CSVReader(fileReader,';');
		) {
			List<WordImportRow> list = csv.parse(strat, reader);
			list.remove(0);
			importWords(list, operator);
		}
		operator.commit();
	}

	private void importWords(List<WordImportRow> list, Operator admin) throws ModelException, BadRequestException, SecurityException, NotFoundException {
		for (WordImportRow row : list) {
			WordListPerspective found = findWordToEnrich(row,admin);
			if (found==null) {
				log.info("Nothing to enrich: "+row);
			}
			else if (row.getLanguage().equals(found.getLanguage()) && row.getCategory().equals(found.getLexicalCategory())) {
				log.info("Already exists: "+row);
			} else {
				Word word = modelService.get(Word.class, found.getId(), admin);
				if (Strings.isBlank(found.getLanguage())) {
					wordsModelService.changeLanguage(word, row.getLanguage(), admin);
				}
				if (Strings.isBlank(found.getLexicalCategory())) {
					wordsModelService.changeCategory(word, row.getCategory(), admin);
				}
			}
			
		}
	}
	
	private WordListPerspective findWordToEnrich(WordImportRow row, Operator operator) throws ModelException {
		
		WordListPerspectiveQuery query = new WordListPerspectiveQuery();
		query.withWord(row.getText().toLowerCase());
		List<WordListPerspective> found = modelService.list(query, operator);
		
		int topHits = -1;
		WordListPerspective topHit = null;
		for (WordListPerspective word : found) {
			int hits = 0;
			if (Strings.isNotBlank(word.getLanguage()) && !row.getLanguage().equals(word.getLanguage())) {
				continue;
			}
			if (Strings.isNotBlank(word.getLexicalCategory()) && !row.getCategory().equals(word.getLexicalCategory())) {
				continue;
			}
			if (row.getLanguage().equals(word.getLanguage())) {
				hits++;
			}
			if (row.getCategory().equals(word.getLexicalCategory())) {
				hits++;
			}
			if (hits>topHits) {
				topHits = hits;
				topHit = word;
			}
		}
		
		if (topHit!=null) {
			log.info("Top hit for: "+row+" is "+topHit);
			modelService.get(Word.class, topHit.getId(), operator);
		} else {
			log.info("No hit for: "+row);
		}
		
		return topHit;
	}
		
	public void setWordsModelService(WordsModelService wordsModelService) {
		this.wordsModelService = wordsModelService;
	}
}