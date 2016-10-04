package sinbad2.phasemethod.todim.resolution.ui.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import sinbad2.core.utils.Pair;
import sinbad2.core.workspace.Workspace;
import sinbad2.element.ProblemElementsManager;
import sinbad2.element.ProblemElementsSet;
import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.phasemethod.PhasesMethodManager;
import sinbad2.phasemethod.todim.resolution.ResolutionPhase;
import sinbad2.phasemethod.todim.resolution.ui.Images;
import sinbad2.phasemethod.todim.resolution.ui.nls.Messages;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.AnotherAlternativeColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.CriteriaTableContentProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.CriterionColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.CriterionIdColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.CriterionWeightColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.DominanceDegreeAlternativesColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.DominanceDegreeAlternativesContentProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.DominanceDegreeColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.DominanceDegreeTableContentProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.GlobalDominanceDegreeColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.MainAlternativeColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.RankingColumnLabelProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.RankingTableContentProvider;
import sinbad2.phasemethod.todim.resolution.ui.view.provider.RelativeWeightCriterionColumnLabelProvider;
import sinbad2.resolutionphase.ResolutionPhasesManager;
import sinbad2.resolutionphase.rating.ui.listener.IStepStateListener;
import sinbad2.resolutionphase.rating.ui.view.RatingView;
import sinbad2.resolutionphase.sensitivityanalysis.SensitivityAnalysis;

public class CalculateRanking extends ViewPart implements IStepStateListener {

	public static final String ID = "flintstones.phasemethod.todim.resolution.ui.view.calculateranking"; //$NON-NLS-1$

	private static final String[] FILTER_NAMES = { "Text files (*.txt)" }; //$NON-NLS-1$
	private static final String[] FILTER_EXTS = { "*.txt" }; //$NON-NLS-1$

	private Composite _parent;
	private Combo _matrixType;
	private Button _loadFuzzyNumbers;

	private DecisionMatrixEditableTable _dmTable;
	private TableViewer _criteriaTableViewer;
	private TableViewer _dominanceDegreeTableViewer;
	private TableViewer _dominanceDegreeAlternativesTableViewer;
	private TableViewer _rankingTableViewer;
	
	private boolean _loaded;

	private ResolutionPhase _resolutionPhase;
	private SensitivityAnalysis _sensitivityAnalysis;

	private ProblemElementsSet _elementsSet;

	@SuppressWarnings("rawtypes")
	public static class DataComparator implements Comparator {
		@Override
		public int compare(Object d1, Object d2) {
			String a1 = ((String[]) d1)[0];
			String a2 = ((String[]) d2)[0];

			int compare = a1.compareTo(a2);
			if (compare != 0) {
				return compare;
			} else {
				String aS1 = ((String[]) d1)[1];
				String aS2 = ((String[]) d2)[1];
				return aS1.compareTo(aS2);
			}
		}
	}

	public CalculateRanking() {
		ProblemElementsManager elementsManager = ProblemElementsManager.getInstance();
		_elementsSet = elementsManager.getActiveElementSet();

		PhasesMethodManager pmm = PhasesMethodManager.getInstance();
		_resolutionPhase = (ResolutionPhase) pmm.getPhaseMethod(ResolutionPhase.ID).getImplementation();
		
		_sensitivityAnalysis = (SensitivityAnalysis) ResolutionPhasesManager.getInstance().getResolutionPhase(SensitivityAnalysis.ID).getImplementation();
	
		_loaded = false;
	}

	@Override
	public void createPartControl(Composite parent) {
		_parent = parent;
		_parent.setLayout(new GridLayout(1, true));
		_parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite decisionMatrixComposite = new Composite(_parent, SWT.NONE);
		decisionMatrixComposite.setLayout(new GridLayout(1, true));
		decisionMatrixComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		_dmTable = new DecisionMatrixEditableTable(decisionMatrixComposite);
		_dmTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_dmTable.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (_dmTable.isCompleted()) {
					_resolutionPhase.setConsensusMatrix(_dmTable.getTrapezoidalConsensusMatrix());
					_resolutionPhase.setTrapezoidalConsensusMatrix(_dmTable.getTrapezoidalConsensusMatrix());
					
					refreshTODIMTables();

					notifyStepStateChange();
					
					_sensitivityAnalysis.setDecisionMatrix(_resolutionPhase.calculateConsensusMatrixCenterOfGravity(new Double[_elementsSet.getAlternatives().size()][_elementsSet.getAllCriteria().size()]));
					
					_matrixType.setEnabled(true);
				}
			}
		});

		Composite buttonsComposite = new Composite(decisionMatrixComposite, SWT.READ_ONLY);
		buttonsComposite.setLayout(new GridLayout(2, false));
		buttonsComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));

		_matrixType = new Combo(buttonsComposite, SWT.NONE);
		_matrixType.setItems(new String[] { Messages.CalculateRanking_Fuzzy_TODIM, Messages.CalculateRanking_Center_of_Gravity });
		_matrixType.select(0);
		_matrixType.setEnabled(false);
		_matrixType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int indexSelected = ((Combo) e.widget).getSelectionIndex();
				if (indexSelected == 0) {
					_resolutionPhase.setConsensusMatrix(_resolutionPhase.getTrapezoidalConsensusMatrix());
				} else if (indexSelected == 1) {
					_resolutionPhase.setConsensusMatrix(_resolutionPhase.calculateConsensusMatrixCenterOfGravity());
				}
				
				refreshConsensusMatrixTable();
				refreshTODIMTables();
			}
		});

		_loadFuzzyNumbers = new Button(buttonsComposite, SWT.NONE);
		_loadFuzzyNumbers.setImage(Images.File);
		_loadFuzzyNumbers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				FileDialog dlg = new FileDialog(shell, SWT.OPEN);
				dlg.setFilterNames(FILTER_NAMES);
				dlg.setFilterExtensions(FILTER_EXTS);
				String fn = dlg.open();
				
				if(fn != null) {
					readFuzzyNumbers(fn);
					
					refreshTODIMTables();
					
					_sensitivityAnalysis.setDecisionMatrix(_resolutionPhase.calculateConsensusMatrixCenterOfGravity(new Double[_elementsSet.getAlternatives().size()][_elementsSet.getAllCriteria().size()]));
					
					_matrixType.setEnabled(true);
					
					notifyStepStateChange();
				}
			}

			private void readFuzzyNumbers(String path) {
				if(path != null) {
					BufferedReader br;
					
					try {
						br = new BufferedReader(new FileReader(path));
						String line = br.readLine();
	
						String[][] trapezoidalMatrix = new String[_elementsSet.getAlternatives().size()][_elementsSet.getAllCriteria().size()];
						
						int numAlternative = 0, numCriterion = 0;
						while (line != null) {
							line = line.replace(",", ".");
		
							String[] trapezoidalNumbers = line.split(" ");
							for(int i = 0; i < trapezoidalNumbers.length; i+=4) {
								trapezoidalMatrix[numAlternative][numCriterion] = "(" + trapezoidalNumbers[i] + "," +
										trapezoidalNumbers[i + 1] + "," + trapezoidalNumbers[i + 2] + "," + trapezoidalNumbers[i + 3] + ")";
								
								numCriterion++;
							}
							
							line = br.readLine();
		
							numCriterion = 0;
							numAlternative++;
						}
						
						_resolutionPhase.setConsensusMatrix(trapezoidalMatrix);
						_resolutionPhase.setTrapezoidalConsensusMatrix(trapezoidalMatrix);
						refreshConsensusMatrixTable();
			
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		Composite tablesComposite = new Composite(_parent, SWT.NONE);
		tablesComposite.setLayout(new GridLayout(4, true));
		tablesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		_criteriaTableViewer = new TableViewer(tablesComposite);
		_criteriaTableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_criteriaTableViewer.setContentProvider(new CriteriaTableContentProvider());
		_criteriaTableViewer.getTable().setHeaderVisible(true);

		TableViewerColumn criterionId = new TableViewerColumn(_criteriaTableViewer, SWT.NONE);
		criterionId.getColumn().setText(Messages.CalculateRanking_Criterion);
		criterionId.setLabelProvider(new CriterionIdColumnLabelProvider());
		criterionId.getColumn().pack();

		TableViewerColumn criterionWeight = new TableViewerColumn(_criteriaTableViewer, SWT.NONE);
		criterionWeight.getColumn().setText(Messages.CalculateRanking_Weight);
		criterionWeight.setLabelProvider(new CriterionWeightColumnLabelProvider());
		criterionWeight.getColumn().pack();

		TableViewerColumn relativeWeight = new TableViewerColumn(_criteriaTableViewer, SWT.NONE);
		relativeWeight.getColumn().setText(Messages.CalculateRanking_Relative_weight);
		relativeWeight.setLabelProvider(new RelativeWeightCriterionColumnLabelProvider());
		relativeWeight.getColumn().pack();

		_dominanceDegreeTableViewer = new TableViewer(tablesComposite);
		_dominanceDegreeTableViewer.setContentProvider(new DominanceDegreeTableContentProvider());
		_dominanceDegreeTableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_dominanceDegreeTableViewer.getTable().setHeaderVisible(true);

		TableViewerColumn mainAlternative = new TableViewerColumn(_dominanceDegreeTableViewer, SWT.NONE);
		mainAlternative.getColumn().setText(Messages.CalculateRanking_Alternative);
		mainAlternative.setLabelProvider(new MainAlternativeColumnLabelProvider());
		mainAlternative.getColumn().pack();

		TableViewerColumn anotherAlternative = new TableViewerColumn(_dominanceDegreeTableViewer, SWT.NONE);
		anotherAlternative.getColumn().setText(Messages.CalculateRanking_Alternative);
		anotherAlternative.setLabelProvider(new AnotherAlternativeColumnLabelProvider());
		anotherAlternative.getColumn().pack();

		TableViewerColumn criterion = new TableViewerColumn(_dominanceDegreeTableViewer, SWT.NONE);
		criterion.getColumn().setText(Messages.CalculateRanking_Criterion);
		criterion.setLabelProvider(new CriterionColumnLabelProvider());
		criterion.getColumn().pack();

		TableViewerColumn dominanceDegree = new TableViewerColumn(_dominanceDegreeTableViewer, SWT.NONE);
		dominanceDegree.getColumn().setText(Messages.CalculateRanking_Dominance_degree);
		dominanceDegree.setLabelProvider(new DominanceDegreeColumnLabelProvider());
		dominanceDegree.getColumn().pack();

		_dominanceDegreeAlternativesTableViewer = new TableViewer(tablesComposite);
		_dominanceDegreeAlternativesTableViewer.setContentProvider(new DominanceDegreeAlternativesContentProvider());
		_dominanceDegreeAlternativesTableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_dominanceDegreeAlternativesTableViewer.getTable().setHeaderVisible(true);

		TableViewerColumn mainAlternative2 = new TableViewerColumn(_dominanceDegreeAlternativesTableViewer, SWT.NONE);
		mainAlternative2.getColumn().setText(Messages.CalculateRanking_Alternative);
		mainAlternative2.setLabelProvider(new MainAlternativeColumnLabelProvider());
		mainAlternative2.getColumn().pack();

		TableViewerColumn anotherAlternative2 = new TableViewerColumn(_dominanceDegreeAlternativesTableViewer, SWT.NONE);
		anotherAlternative2.getColumn().setText(Messages.CalculateRanking_Alternative);
		anotherAlternative2.setLabelProvider(new AnotherAlternativeColumnLabelProvider());
		anotherAlternative2.getColumn().pack();

		TableViewerColumn dominanceDegreeAlternative = new TableViewerColumn(_dominanceDegreeAlternativesTableViewer, SWT.NONE);
		dominanceDegreeAlternative.getColumn().setText(Messages.CalculateRanking_Dominance_degree);
		dominanceDegreeAlternative.setLabelProvider(new DominanceDegreeAlternativesColumnLabelProvider());
		dominanceDegreeAlternative.getColumn().pack();

		_rankingTableViewer = new TableViewer(tablesComposite);
		_rankingTableViewer.setContentProvider(new RankingTableContentProvider());
		_rankingTableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_rankingTableViewer.getTable().setHeaderVisible(true);

		TableViewerColumn alternative = new TableViewerColumn(_rankingTableViewer, SWT.NONE);
		alternative.getColumn().setText(Messages.CalculateRanking_Alternative);
		alternative.setLabelProvider(new MainAlternativeColumnLabelProvider());
		alternative.getColumn().pack();

		TableViewerColumn globalDominanceDegree = new TableViewerColumn(_rankingTableViewer, SWT.NONE);
		globalDominanceDegree.getColumn().setText(Messages.CalculateRanking_Global_dominance_degree);
		globalDominanceDegree.setLabelProvider(new GlobalDominanceDegreeColumnLabelProvider());
		globalDominanceDegree.getColumn().pack();

		TableViewerColumn ranking = new TableViewerColumn(_rankingTableViewer, SWT.NONE);
		ranking.getColumn().setText(Messages.CalculateRanking_Ranking);
		ranking.setLabelProvider(new RankingColumnLabelProvider());
		ranking.getColumn().pack();

		refreshConsensusMatrixTable();

		setInputCriteriaTable();
	}

	private void refreshConsensusMatrixTable() {
		String[] alternatives = new String[_elementsSet.getAlternatives().size()];
		for (int a = 0; a < alternatives.length; ++a) {
			alternatives[a] = _elementsSet.getAlternatives().get(a).getId();
		}

		String[] criteria = new String[_elementsSet.getCriteria().size()];
		for (int c = 0; c < criteria.length; ++c) {
			criteria[c] = _elementsSet.getCriteria().get(c).getId();
		}

		_dmTable.setModel(alternatives, criteria, _resolutionPhase.getConsensusMatrix());
	}

	private void refreshTODIMTables() {

		setInputCriteriaTable();

		if (_matrixType.getSelectionIndex() == 1) {
			setInputDominanceDegreeTable(1);
		} else {
			setInputDominanceDegreeTable(0);
		}

		setInputDominaceAlternativeDegreeTable();
		setInputRankingTable();
	}

	private void setInputCriteriaTable() {
		List<Criterion> criteria = _elementsSet.getAllCriteria();
		List<String[]> result = new LinkedList<String[]>();

		Map<Criterion, Double> criteriaWeights = _resolutionPhase.getImportanceCriteriaWeights();
		Map<String, Double> relativeWeights = _resolutionPhase.calculateRelativeWeights();

		for (Criterion c : criteria) {
			String[] row = new String[4];
			row[0] = c.getCanonicalId();
			row[1] = Double.toString(criteriaWeights.get(c));
			if (relativeWeights.isEmpty()) {
				row[2] = "0"; //$NON-NLS-1$
			} else {
				row[2] = Double.toString(relativeWeights.get(c.getCanonicalId()));
			}

			result.add(row);
		}

		_criteriaTableViewer.setInput(result);
	}

	@SuppressWarnings("unchecked")
	private void setInputDominanceDegreeTable(int mode) {
		List<String[]> input = new LinkedList<String[]>();

		Map<Criterion, Map<Pair<Alternative, Alternative>, Double>> dominanceDegreeByCriterion;
		if (mode == 1) {
			dominanceDegreeByCriterion = _resolutionPhase.calculateDominanceDegreeByCriterionCenterOfGravity();
		} else {
			dominanceDegreeByCriterion = _resolutionPhase.calculateDominanceDegreeByCriterionFuzzyNumber();
		}

		for (Criterion c : _elementsSet.getAllCriteria()) {
			Map<Pair<Alternative, Alternative>, Double> pairAlternativesDominance = dominanceDegreeByCriterion.get(c);
			for (Pair<Alternative, Alternative> pair : pairAlternativesDominance.keySet()) {
				String[] data = new String[4];
				data[0] = pair.getLeft().getId();
				data[1] = pair.getRight().getId();
				data[2] = c.getCanonicalId();
				data[3] = Double.toString(Math.round(pairAlternativesDominance.get(pair) * 1000) / 1000d);
				input.add(data);
			}
		}

		Collections.sort(input, new DataComparator());

		_dominanceDegreeTableViewer.setInput(input);
	}

	@SuppressWarnings("unchecked")
	private void setInputDominaceAlternativeDegreeTable() {
		List<String[]> input = new LinkedList<String[]>();

		Map<Pair<Alternative, Alternative>, Double> pairAlternativesDominance = _resolutionPhase.calculateDominaceDegreeAlternatives();
		for (Pair<Alternative, Alternative> pair : pairAlternativesDominance.keySet()) {
			String[] data = new String[3];
			data[0] = pair.getLeft().getCanonicalId();
			data[1] = pair.getRight().getCanonicalId();
			data[2] = Double.toString(Math.round(pairAlternativesDominance.get(pair) * 1000) / 1000d);

			input.add(data);
		}

		Collections.sort(input, new DataComparator());

		_dominanceDegreeAlternativesTableViewer.setInput(input);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setInputRankingTable() {
		List<String[]> inputAux = new LinkedList<String[]>();
		List<String[]> input = new LinkedList<String[]>();

		Map<Alternative, Double> globalDominance = _resolutionPhase.calculateGlobalDominance();
	
		int ranking = globalDominance.size();
		for (Alternative a : globalDominance.keySet()) {
			String[] data = new String[3];
			data[0] = a.getId();
			data[1] = Double.toString(Math.round(globalDominance.get(a) * 1000) / 1000d);
			data[2] = Integer.toString(ranking);

			inputAux.add(data);

			ranking--;
		}

		for (int i = inputAux.size() - 1; i >= 0; i--) {
			input.add(inputAux.get(i));
		}

		_rankingTableViewer.setInput(input);
		
		Double[] dominances = new Double[globalDominance.size()];
		int alternative = 0;
		for(Alternative a: _elementsSet.getAlternatives()) {
			dominances[alternative] = globalDominance.get(a);
			alternative++;
		}
		
		_sensitivityAnalysis.setAlternativesFinalPreferences(dominances);
		
		Map<Pair<Alternative, Alternative>, Double> dominancePairAlternatives = _resolutionPhase.getDominanceDegreeAlternatives();
		List<Alternative> alternatives = _elementsSet.getAlternatives();
		Double[][] dominancesPair = new Double[alternatives.size()][alternatives.size()];
		for(int i = 0; i < alternatives.size() - 1; ++i) {
			for(int j = i + 1; j < alternatives.size(); ++j) {
				dominancesPair[i][j] = dominancePairAlternatives.get(new Pair(alternatives.get(i), alternatives.get(j)));
			}
		}
		
		_sensitivityAnalysis.setAlternativesRatioFinalPreferences(dominancesPair);
		
		notifyStepStateChange();
	}

	@Override
	public void setFocus() {
		_criteriaTableViewer.getTable().setFocus();
	}

	@Override
	public void notifyStepStateChange() {

		if (!_loaded) {
			_loaded = true;

			Double[] w = new Double[_elementsSet.getAllCriteria().size()];
			int cont = 0;
			for (Criterion c : _elementsSet.getAllCriteria()) {
				w[cont] = _resolutionPhase.getImportanceCriteriaWeights().get(c);
				cont++;
			}
			
			_sensitivityAnalysis.setWeights(w);
		}

		Workspace.getWorkspace().updatePhases();
	}

	@Override
	public void setRatingView(RatingView rating) {
	}

	@Override
	public String getPartName() {
		return "TODIM"; //$NON-NLS-1$
	}
}
