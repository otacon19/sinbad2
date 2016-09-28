package sinbad2.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import sinbad2.core.utils.Pair;
import sinbad2.domain.linguistic.fuzzy.function.types.TrapezoidalFunction;
import sinbad2.domain.linguistic.fuzzy.label.LabelLinguisticDomain;
import sinbad2.element.ProblemElementsManager;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.element.expert.Expert;
import sinbad2.valuation.Valuation;
import sinbad2.valuation.twoTuple.TwoTuple;
import sinbad2.valuation.valuationset.ValuationKey;
import sinbad2.valuation.valuationset.ValuationSetManager;

public class ExcelUtil {

	private static final String[] FILTER_NAMES = { "Excel files (*.xlsx)" }; //$NON-NLS-1$
	private static final String[] FILTER_EXTS = { "*.xlsx" }; //$NON-NLS-1$
	
	private static final String FUZZY =  "Fuzzy"; //$NON-NLS-1$
	private static final String NUMERIC_INTEGER =  "Integer numeric"; //$NON-NLS-1$
	private static final String NUMERIC_REAL =  "Real numeric"; //$NON-NLS-1$
	
	private XSSFWorkbook _workbook;
	private XSSFSheet _sheet;

	private CellStyle _styleExperts;
	private CellStyle _styleAlternatives;
	private CellStyle _styleCriteria;
	private CellStyle _styleTitles;

	private List<Expert> _experts;
	private List<Criterion> _criteria;
	private List<Alternative> _alternatives;
	private Map<ValuationKey, Valuation> _unifiedValuations;
	private List<Double> _expertsWeights;
	private Map<Pair<Alternative, Criterion>, Valuation> _decisionMatrix;
	private Map<Pair<Expert, Criterion>, Double> _thresholdValues;

	private ProblemElementsSet _elementsSet;

	public ExcelUtil() {
		ProblemElementsManager elementsManager = ProblemElementsManager.getInstance();
		_elementsSet = elementsManager.getActiveElementSet();

		_experts = _elementsSet.getExperts();
		_alternatives = _elementsSet.getAlternatives();
		_criteria = _elementsSet.getAllCriteria();

		_workbook = new XSSFWorkbook();

		_styleExperts = _workbook.createCellStyle();
		_styleExperts.setFillForegroundColor(IndexedColors.RED.getIndex());
		_styleExperts.setFillPattern(CellStyle.SOLID_FOREGROUND);

		_styleAlternatives = _workbook.createCellStyle();
		_styleAlternatives.setFillForegroundColor(IndexedColors.CORAL.getIndex());
		_styleAlternatives.setFillPattern(CellStyle.SOLID_FOREGROUND);

		_styleCriteria = _workbook.createCellStyle();
		_styleCriteria.setFillForegroundColor(IndexedColors.AQUA.getIndex());
		_styleCriteria.setFillPattern(CellStyle.SOLID_FOREGROUND);
		_styleCriteria.setAlignment(CellStyle.ALIGN_CENTER);
		_styleCriteria.setBorderLeft(BorderStyle.THIN);
		_styleCriteria.setBorderRight(BorderStyle.THIN);

		_styleTitles = _workbook.createCellStyle();
		_styleTitles.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		_styleTitles.setFillPattern(CellStyle.SOLID_FOREGROUND);
		_styleTitles.setAlignment(CellStyle.ALIGN_CENTER);
	}

	public void createExcelFile(Map<ValuationKey, Valuation> unifiedValuations) {

		_unifiedValuations = unifiedValuations;

		createProblemInformation();

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		FileDialog dlg = new FileDialog(shell, SWT.SAVE);
		dlg.setFilterNames(FILTER_NAMES);
		dlg.setFilterExtensions(FILTER_EXTS);
		String fn = dlg.open();

		try (FileOutputStream outputStream = new FileOutputStream(fn)) {
			_workbook.write(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createProblemInformation() {
		Map<ValuationKey, Valuation> valuations = ValuationSetManager.getInstance().getActiveValuationSet().getValuations();
		for (Expert e : _experts) {
			_sheet = _workbook.createSheet(e.getCanonicalId());
			createExpertAssessments(e, valuations);
		}
	}

	private void createExpertAssessments(Expert e, Map<ValuationKey, Valuation> valuations) {
		int rowCountExpertsCriteria = 2, rowCountAlternatives = 3, columnCountCriteria = 3, columnCountValuation = 3;

		Row rowExpertCriterion = _sheet.createRow(rowCountExpertsCriteria);
		Cell cell = rowExpertCriterion.createCell(2);
		cell.setCellValue(e.getId());
		cell.setCellStyle(_styleExperts);

		rowCountAlternatives = rowCountExpertsCriteria + 1;

		CreationHelper factory = _workbook.getCreationHelper();
		
		for (Alternative a : _alternatives) {

			Row rowAlternative = _sheet.createRow(rowCountAlternatives);
			cell = rowAlternative.createCell(2);
			cell.setCellValue(a.getId());
			cell.setCellStyle(_styleAlternatives);

			for (Criterion c : _criteria) {
				for (ValuationKey vk : valuations.keySet()) {
					if (vk.getExpert().equals(e) && vk.getAlternative().equals(a) && vk.getCriterion().equals(c)) {

						Valuation v = valuations.get(vk);

						cell = rowAlternative.createCell(columnCountValuation);
						cell.setCellValue(v.changeFormatValuationToString());

						Drawing drawing = _sheet.createDrawingPatriarch();

						ClientAnchor anchor = factory.createClientAnchor();
						anchor.setCol1(cell.getColumnIndex());
						anchor.setCol2(cell.getColumnIndex() + 4);
						anchor.setRow1(rowAlternative.getRowNum());
						anchor.setRow2(rowAlternative.getRowNum() + 4);
						
						String type = v.getDomain().getType();
						if(type.contains("linguistic")) {
							type = FUZZY;
						} else if(type.contains("integer")) {
							type = NUMERIC_INTEGER;
						} else {
							type = NUMERIC_REAL;
						}
						
					    Comment comment = drawing.createCellComment(anchor);
					    RichTextString str = factory.createRichTextString("Domain: " + v.getDomain().getId() + "\n" +
					    		"Domain type: " + type);
					    comment.setString(str);
					    comment.setAuthor("Flintstones");

						columnCountValuation++;
					}
				}
			}

			columnCountValuation = 3;

			rowCountAlternatives++;
		}

		for (Criterion c : _criteria) {
			cell = rowExpertCriterion.createCell(columnCountCriteria);
			cell.setCellValue(c.getId());
			cell.setCellStyle(_styleCriteria);

			columnCountCriteria++;
		}
	}

	public void createExcelFileEmergencyProblemStructure(Map<ValuationKey, Valuation> unifiedValuations,
			List<Double> expertsWeights, Map<Pair<Alternative, Criterion>, Valuation> decisionMatrix,
			Map<Pair<Expert, Criterion>, Double> thresholdValues) {

		_unifiedValuations = unifiedValuations;
		_expertsWeights = expertsWeights;
		_decisionMatrix = decisionMatrix;
		_thresholdValues = thresholdValues;

		_sheet = _workbook.createSheet("Flintstones problem");

		createEmergencyProblemInformation();

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		FileDialog dlg = new FileDialog(shell, SWT.SAVE);
		dlg.setFilterNames(FILTER_NAMES);
		dlg.setFilterExtensions(FILTER_EXTS);
		String fn = dlg.open();

		try (FileOutputStream outputStream = new FileOutputStream(fn)) {
			_workbook.write(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createEmergencyProblemInformation() {

		createExpertsTablesInformation();
		createExpertsWeightsInformation();
		createEmergencyProblemOverallInformation();
		createThresholdValues();
	}

	private void createExpertsTablesInformation() {

		int rowCountExpertsCriteria = 9, rowCountAlternatives = 10, columnCountCriteria = 3, columnCountValuation = 3;

		for (Expert e : _experts) {

			Row rowExpertCriterion = _sheet.createRow(rowCountExpertsCriteria);
			Cell cell = rowExpertCriterion.createCell(2);
			cell.setCellValue(e.getId());
			cell.setCellStyle(_styleExperts);

			rowCountAlternatives = rowCountExpertsCriteria + 1;

			for (Alternative a : _alternatives) {

				Row rowAlternative = _sheet.createRow(rowCountAlternatives);
				cell = rowAlternative.createCell(2);
				cell.setCellValue(a.getId());
				cell.setCellStyle(_styleAlternatives);

				rowCountAlternatives++;

				for (Criterion c : _criteria) {
					for (ValuationKey vk : _unifiedValuations.keySet()) {
						if (vk.getExpert().equals(e) && vk.getAlternative().equals(a) && vk.getCriterion().equals(c)) {

							TwoTuple v = (TwoTuple) _unifiedValuations.get(vk);
							LabelLinguisticDomain label = v.getLabel();
							TrapezoidalFunction semantic = (TrapezoidalFunction) label.getSemantic();
							double limits[] = semantic.getLimits();

							cell = rowAlternative.createCell(columnCountValuation);
							cell.setCellValue(Double.toString(limits[0]));

							columnCountValuation++;

							cell = rowAlternative.createCell(columnCountValuation);
							cell.setCellValue(Double.toString(limits[1]));

							columnCountValuation++;

							cell = rowAlternative.createCell(columnCountValuation);
							cell.setCellValue(Double.toString(limits[2]));

							columnCountValuation++;

							cell = rowAlternative.createCell(columnCountValuation);
							cell.setCellValue(Double.toString(limits[3]));

							columnCountValuation++;
						}
					}
				}

				columnCountValuation = 3;
			}

			for (Criterion c : _criteria) {
				cell = rowExpertCriterion.createCell(columnCountCriteria);
				cell.setCellValue(c.getId());
				cell.setCellStyle(_styleCriteria);

				CellRangeAddress cellRangeAddress = new CellRangeAddress(rowCountExpertsCriteria,
						rowCountExpertsCriteria, columnCountCriteria, columnCountCriteria + 3);
				_sheet.addMergedRegion(cellRangeAddress);

				columnCountCriteria += 4;
			}

			rowCountExpertsCriteria += _alternatives.size() + 2;
			columnCountCriteria = 3;
		}
	}

	private void createExpertsWeightsInformation() {
		Row rowAggregationWeights = _sheet.createRow(1);
		Cell cell = rowAggregationWeights.createCell(2);
		cell.setCellStyle(_styleTitles);

		CellRangeAddress cellRangeAddress = new CellRangeAddress(1, 1, 2, 1 + _experts.size());
		_sheet.addMergedRegion(cellRangeAddress);

		if (_expertsWeights.isEmpty()) {
			cell.setCellValue("Aggregation operator without weights");
		} else {
			cell.setCellValue("Aggregation weights");

			int columnCount = 2;
			Row rowExperts = _sheet.createRow(2);
			Row rowWeights = _sheet.createRow(3);
			for (int i = 0; i < _experts.size(); ++i) {
				cell = rowExperts.createCell(columnCount);
				cell.setCellStyle(_styleExperts);
				cell.setCellValue(_experts.get(i).getCanonicalId());

				cell = rowWeights.createCell(columnCount);
				cell.setCellValue(_expertsWeights.get(i));

				columnCount++;
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createEmergencyProblemOverallInformation() {

		int rowCountAlternatives = 10 + _elementsSet.getAllExperts().size() * 2
				+ _elementsSet.getAllExperts().size() * _elementsSet.getAlternatives().size(), columnCountValuation = 3;

		Row rowOverallOpinion = _sheet.createRow(rowCountAlternatives);
		Cell cell = rowOverallOpinion.createCell(2);
		cell.setCellValue("Overall opinion");
		cell.setCellStyle(_styleTitles);

		rowCountAlternatives++;

		for (Alternative a : _alternatives) {

			Row rowAlternative = _sheet.createRow(rowCountAlternatives);
			cell = rowAlternative.createCell(2);
			cell.setCellValue(a.getId());
			cell.setCellStyle(_styleAlternatives);

			rowCountAlternatives++;

			for (Criterion c : _criteria) {

				TwoTuple v = (TwoTuple) _decisionMatrix.get(new Pair(a, c));
				LabelLinguisticDomain label = v.getLabel();
				TrapezoidalFunction semantic = (TrapezoidalFunction) label.getSemantic();
				double limits[] = semantic.getLimits();

				cell = rowAlternative.createCell(columnCountValuation);
				cell.setCellValue(Double.toString(limits[0]));

				columnCountValuation++;

				cell = rowAlternative.createCell(columnCountValuation);
				cell.setCellValue(Double.toString(limits[1]));

				columnCountValuation++;

				cell = rowAlternative.createCell(columnCountValuation);
				cell.setCellValue(Double.toString(limits[2]));

				columnCountValuation++;

				cell = rowAlternative.createCell(columnCountValuation);
				cell.setCellValue(Double.toString(limits[3]));

				columnCountValuation++;
			}

			columnCountValuation = 3;
		}

		rowCountAlternatives = 10 + _elementsSet.getAllExperts().size() * 2
				+ _elementsSet.getAllExperts().size() * _elementsSet.getAlternatives().size();

		int columnCountCriterion = 3;
		for (Criterion c : _criteria) {
			cell = rowOverallOpinion.createCell(columnCountCriterion);
			cell.setCellValue(c.getCanonicalId());
			cell.setCellStyle(_styleCriteria);
			CellRangeAddress cellRangeAddress = new CellRangeAddress(rowCountAlternatives, rowCountAlternatives,
					columnCountCriterion, columnCountCriterion + 3);
			_sheet.addMergedRegion(cellRangeAddress);

			columnCountCriterion += 4;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createThresholdValues() {
		int thresholdTitleRow = 96, thresholdTitleColumn = 2, thresholdCriterionColumn = 3;

		for (Expert e : _experts) {
			Row thresholdTitle = _sheet.createRow(thresholdTitleRow);
			Cell cell = thresholdTitle.createCell(thresholdTitleColumn);
			cell.setCellStyle(_styleTitles);
			cell.setCellValue("Threshold values");

			Row thresholdValues = _sheet.createRow(thresholdTitleRow + 1);
			for (Criterion criterion : _criteria) {
				cell = thresholdTitle.createCell(thresholdCriterionColumn);
				cell.setCellStyle(_styleCriteria);
				cell.setCellValue(criterion.getId());

				cell = thresholdValues.createCell(thresholdCriterionColumn);
				cell.setCellValue(Double.toString(_thresholdValues.get(new Pair(e, criterion))));

				thresholdCriterionColumn++;
			}

			thresholdTitleRow++;
			cell = thresholdValues.createCell(thresholdTitleColumn);
			cell.setCellStyle(_styleExperts);
			cell.setCellValue(e.getId());

			thresholdTitleRow += 3;
			thresholdCriterionColumn = 3;
		}

	}
}
