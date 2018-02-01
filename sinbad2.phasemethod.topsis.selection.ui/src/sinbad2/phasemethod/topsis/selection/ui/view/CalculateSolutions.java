package sinbad2.phasemethod.topsis.selection.ui.view;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import sinbad2.element.alternative.Alternative;
import sinbad2.element.criterion.Criterion;
import sinbad2.phasemethod.PhasesMethodManager;
import sinbad2.phasemethod.topsis.selection.SelectionPhase;
import sinbad2.phasemethod.topsis.selection.ui.Images;
import sinbad2.phasemethod.topsis.selection.ui.nls.Messages;
import sinbad2.phasemethod.topsis.selection.ui.view.provider.CollectiveValuationsTableViewerContentProvider;
import sinbad2.phasemethod.topsis.selection.ui.view.provider.IdealSolutionTableViewerContentProvider;
import sinbad2.phasemethod.topsis.selection.ui.view.provider.NoIdealSolutionTableViewerContentProvider;
import sinbad2.resolutionphase.rating.ui.listener.IStepStateListener;
import sinbad2.resolutionphase.rating.ui.view.RatingView;
import sinbad2.valuation.twoTuple.TwoTuple;

public class CalculateSolutions extends ViewPart implements IStepStateListener {
	
	public static final String ID = "flintstones.phasemethod.topsis.selection.ui.view.aggregationexperts"; //$NON-NLS-1$

	private Composite _parent;
	
	private TableViewer _tableViewerIdealSolution;
	private TableViewer _tableViewerNoIdealSolution;
	private TableViewer _tableViewerCollectiveValuations;
	
	private CollectiveValuationsTableViewerContentProvider _collectiveValuationsProvider;
	private IdealSolutionTableViewerContentProvider _idealSolutionProvider;
	private NoIdealSolutionTableViewerContentProvider _noIdealSolutionProvider;
	
	private boolean _completed;
	
	private RatingView _ratingView;
	
	private SelectionPhase _selectionPhase;

	@Override
	public void createPartControl(Composite parent) {	
		PhasesMethodManager pmm = PhasesMethodManager.getInstance();
		_selectionPhase = (SelectionPhase) pmm.getPhaseMethod(SelectionPhase.ID).getImplementation();
		_selectionPhase.execute();
		
		_completed = true;

		_parent = parent;
		
		createContent();
	}

	private void createContent() {
		_parent.setLayout(new GridLayout(3, false));
		_parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		createCollectiveValuationsTable();
		createIdealSolutionTable();
		createNoIdealSolutionTable();
	}
	
	private void createCollectiveValuationsTable() {
		GridLayout colectiveValuationsLayout = new GridLayout(1, true);
		Composite collectiveValuationsPanel = new Composite(_parent, SWT.NONE);
		collectiveValuationsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		collectiveValuationsPanel.setLayout(colectiveValuationsLayout);
		
		Label collectiveValuationsLabel = new Label(collectiveValuationsPanel, SWT.NONE);
		collectiveValuationsLabel.setText(Messages.AggregationExperts_Colective_valuations);
		FontData fontData = collectiveValuationsLabel.getFont().getFontData()[0];
		collectiveValuationsLabel.setFont(new Font(collectiveValuationsLabel.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD)));
		collectiveValuationsLabel.setLayoutData( new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		
		_tableViewerCollectiveValuations = new TableViewer(collectiveValuationsPanel, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		_tableViewerCollectiveValuations.getTable().setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_tableViewerCollectiveValuations.getTable().setHeaderVisible(true);
		
		_collectiveValuationsProvider = new CollectiveValuationsTableViewerContentProvider();
		_tableViewerCollectiveValuations.setContentProvider(_collectiveValuationsProvider);

		TableViewerColumn alternativeColumn = new TableViewerColumn(_tableViewerCollectiveValuations, SWT.NONE);
		alternativeColumn.getColumn().setText(Messages.AggregationExperts_Alternative);
		alternativeColumn.getColumn().setResizable(false);
		alternativeColumn.getColumn().setMoveable(false);
		alternativeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object[] data = (Object[]) element;
				return ((Alternative) data[0]).getId();
			}
		});
		
		TableViewerColumn criterionColumn = new TableViewerColumn(_tableViewerCollectiveValuations, SWT.NONE);
		criterionColumn.getColumn().setText(Messages.AggregationExperts_Criterion);
		criterionColumn.getColumn().setResizable(false);
		criterionColumn.getColumn().setMoveable(false);
		criterionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object[] data = (Object[]) element;
				return ((Criterion) data[1]).getCanonicalId();
			}
		});
		
		TableViewerColumn typeColumn = new TableViewerColumn(_tableViewerCollectiveValuations, SWT.NONE);
		typeColumn.getColumn().setText(Messages.AggregationExperts_Type);
		typeColumn.getColumn().setResizable(false);
		typeColumn.getColumn().setMoveable(false);
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			}
			
			@Override
			public Image getImage(Object element) {
				Object[] data = (Object[]) element;
				if(((Criterion) data[1]).isCost()) {
					return Images.Cost;
				} else {
					return Images.Benefit;
				}
			}
		});
		
		TableViewerColumn valuationColumn = new TableViewerColumn(_tableViewerCollectiveValuations, SWT.NONE);
		valuationColumn.getColumn().setText(Messages.AggregationExperts_Colective_valuation);
		valuationColumn.getColumn().setResizable(false);
		valuationColumn.getColumn().setMoveable(false);
		valuationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object[] data = (Object[]) element;
				TwoTuple valuation = (TwoTuple) data[2];

				String labelName = valuation.getLabel().getName();
				String alpha = Double.toString(valuation.getAlpha());
				
				if(alpha.equals("-0.0") || alpha.equals("0.0")) { //$NON-NLS-1$ //$NON-NLS-2$
					alpha = "0"; //$NON-NLS-1$
				}

				int size = 4;
				if(alpha.startsWith("-")) { //$NON-NLS-1$
					size = 5;
				}
				
				if(alpha.length() > size) {
					alpha = alpha.substring(0, size);
				}
				
				if(alpha.length() > 1) {
					if(alpha.endsWith("0")) { //$NON-NLS-1$
						alpha = alpha.substring(0, size - 1);
					}
				}
				return "(" + labelName + ", " + alpha + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
		
		_collectiveValuationsProvider.setInput(_selectionPhase.getDecisionMatrix());
		_tableViewerCollectiveValuations.setInput(_collectiveValuationsProvider.getInput());
		
		criterionColumn.getColumn().pack();
		alternativeColumn.getColumn().pack();
		typeColumn.getColumn().pack();
		valuationColumn.getColumn().pack();
	}
	
	private void createIdealSolutionTable() {
		GridLayout idealSolutionValuationsLayout = new GridLayout(1, true);
		Composite idealSolutionComposite = new Composite(_parent, SWT.NONE);
		idealSolutionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		idealSolutionComposite.setLayout(idealSolutionValuationsLayout);
		
		Label idealSolutionLabel = new Label(idealSolutionComposite, SWT.NONE);
		idealSolutionLabel.setText(Messages.AggregationExperts_Ideal_solution);
		FontData fontData = idealSolutionLabel.getFont().getFontData()[0];
		idealSolutionLabel.setFont(new Font(idealSolutionLabel.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD)));
		idealSolutionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		
		_tableViewerIdealSolution = new TableViewer(idealSolutionComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		_tableViewerIdealSolution.getTable().setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_tableViewerIdealSolution.getTable().setHeaderVisible(true);
		
		_idealSolutionProvider = new IdealSolutionTableViewerContentProvider();
		_tableViewerIdealSolution.setContentProvider(_idealSolutionProvider);

		TableViewerColumn criterionColumn = new TableViewerColumn(_tableViewerIdealSolution, SWT.NONE);
		criterionColumn.getColumn().setText(Messages.AggregationExperts_Criterion);
		criterionColumn.getColumn().pack();
		criterionColumn.getColumn().setResizable(false);
		criterionColumn.getColumn().setMoveable(false);
		criterionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object[] data = (Object[]) element;
				return ((Criterion) data[0]).getCanonicalId();
			}
		});
		
		TableViewerColumn typeColumn = new TableViewerColumn(_tableViewerIdealSolution, SWT.NONE);
		typeColumn.getColumn().setText(Messages.AggregationExperts_Type);
		typeColumn.getColumn().pack();
		typeColumn.getColumn().setResizable(false);
		typeColumn.getColumn().setMoveable(false);
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			}
			
			@Override
			public Image getImage(Object element) {
				Object[] data = (Object[]) element;
				if(((Criterion) data[0]).isCost()) {
					return Images.Cost;
				} else {
					return Images.Benefit;
				}
			}
		});
		
		TableViewerColumn valuationColumn = new TableViewerColumn(_tableViewerIdealSolution, SWT.NONE);
		valuationColumn.getColumn().setText(Messages.AggregationExperts_Valuation);
		valuationColumn.getColumn().pack();
		valuationColumn.getColumn().setResizable(false);
		valuationColumn.getColumn().setMoveable(false);
		valuationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object[] data = (Object[]) element;
				TwoTuple valuation = (TwoTuple) data[1];

				String labelName = valuation.getLabel().getName();
				String alpha = Double.toString(valuation.getAlpha());
				
				if(alpha.equals("-0.0") || alpha.equals("0.0")) { //$NON-NLS-1$ //$NON-NLS-2$
					alpha = "0"; //$NON-NLS-1$
				}

				int size = 4;
				if(alpha.startsWith("-")) { //$NON-NLS-1$
					size = 5;
				}
				
				if(alpha.length() > size) {
					alpha = alpha.substring(0, size);
				}
				
				if(alpha.length() > 1) {
					if(alpha.endsWith("0")) { //$NON-NLS-1$
						alpha = alpha.substring(0, size - 1);
					}
				}
				return "(" + labelName + ", " + alpha + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
		
		_idealSolutionProvider.setInput(_selectionPhase.getIdealSolution());
		_tableViewerIdealSolution.setInput(_idealSolutionProvider.getInput());
		
		criterionColumn.getColumn().pack();
		typeColumn.getColumn().pack();
		valuationColumn.getColumn().pack();
	}
	
	private void createNoIdealSolutionTable() {
		GridLayout noIdealSolutionLayout = new GridLayout(1, true);
		Composite noIdealSolutionComposite = new Composite(_parent, SWT.NONE);
		noIdealSolutionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		noIdealSolutionComposite.setLayout(noIdealSolutionLayout);
		
		Label noIdealSolutionLabel = new Label(noIdealSolutionComposite, SWT.NONE);
		FontData fontData = noIdealSolutionLabel.getFont().getFontData()[0];
		noIdealSolutionLabel.setFont(new Font(noIdealSolutionLabel.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD)));
		noIdealSolutionLabel.setText(Messages.AggregationExperts_No_ideal_solution);
		noIdealSolutionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		
		_tableViewerNoIdealSolution = new TableViewer(noIdealSolutionComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		_tableViewerNoIdealSolution.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_tableViewerNoIdealSolution.getTable().setHeaderVisible(true);
		
		_noIdealSolutionProvider = new NoIdealSolutionTableViewerContentProvider();
		_tableViewerNoIdealSolution.setContentProvider(_noIdealSolutionProvider);

		TableViewerColumn criterionColumn = new TableViewerColumn(_tableViewerNoIdealSolution, SWT.NONE);
		criterionColumn.getColumn().setText(Messages.AggregationExperts_Criterion);
		criterionColumn.getColumn().pack();
		criterionColumn.getColumn().setResizable(false);
		criterionColumn.getColumn().setMoveable(false);
		criterionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object[] data = (Object[]) element;
				return ((Criterion) data[0]).getCanonicalId();
			}
		});
		
		TableViewerColumn typeColumn = new TableViewerColumn(_tableViewerNoIdealSolution, SWT.NONE);
		typeColumn.getColumn().setText(Messages.AggregationExperts_Type);
		typeColumn.getColumn().pack();
		typeColumn.getColumn().setResizable(false);
		typeColumn.getColumn().setMoveable(false);
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ""; //$NON-NLS-1$
			}
			
			@Override
			public Image getImage(Object element) {
				Object[] data = (Object[]) element;
				if(((Criterion) data[0]).isCost()) {
					return Images.Cost;
				} else {
					return Images.Benefit;
				}
			}
		});
		
		TableViewerColumn valuationColumn = new TableViewerColumn(_tableViewerNoIdealSolution, SWT.NONE);
		valuationColumn.getColumn().setText(Messages.AggregationExperts_Valuation);
		valuationColumn.getColumn().pack();
		valuationColumn.getColumn().setResizable(false);
		valuationColumn.getColumn().setMoveable(false);
		valuationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Object[] data = (Object[]) element;
				TwoTuple valuation = (TwoTuple) data[1];

				String labelName = valuation.getLabel().getName();
				String alpha = Double.toString(valuation.getAlpha());
				
				if(alpha.equals("-0.0") || alpha.equals("0.0")) { //$NON-NLS-1$ //$NON-NLS-2$
					alpha = "0"; //$NON-NLS-1$
				}

				int size = 4;
				if(alpha.startsWith("-")) { //$NON-NLS-1$
					size = 5;
				}
				
				if(alpha.length() > size) {
					alpha = alpha.substring(0, size);
				}
				
				if(alpha.length() > 1) {
					if(alpha.endsWith("0")) { //$NON-NLS-1$
						alpha = alpha.substring(0, size - 1);
					}
				}
				return "(" + labelName + ", " + alpha + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		});
		
		_noIdealSolutionProvider.setInput(_selectionPhase.getNoIdealSolution());
		_tableViewerNoIdealSolution.setInput(_noIdealSolutionProvider.getInput());
		
		criterionColumn.getColumn().pack();
		typeColumn.getColumn().pack();
		valuationColumn.getColumn().pack();
	}

	@Override
	public String getPartName() {
		return Messages.AggregationExperts_Calculate_solutions;
	}

	@Override
	public void notifyStepStateChange() {
		if(_completed) {
			_ratingView.loadNextStep();
			_completed = false;
		}	
	}

	@Override
	public void setRatingView(RatingView rating) {
		_ratingView = rating;
		notifyStepStateChange();
	}

	@Override
	public void setFocus() {}
}
