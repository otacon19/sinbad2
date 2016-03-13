package sinbad2.phasemethod.topsis.unification.ui.view;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.phasemethod.PhasesMethodManager;
import sinbad2.phasemethod.topsis.unification.UnificationPhase;
import sinbad2.phasemethod.topsis.unification.ui.Images;
import sinbad2.phasemethod.topsis.unification.ui.comparator.UnificationTreeViewerComparator;
import sinbad2.phasemethod.topsis.unification.ui.view.provider.AlternativeColumnLabelProvider;
import sinbad2.phasemethod.topsis.unification.ui.view.provider.CriterionColumnLabelProvider;
import sinbad2.phasemethod.topsis.unification.ui.view.provider.DomainColumnLabelProvider;
import sinbad2.phasemethod.topsis.unification.ui.view.provider.EvaluationColumnLabelProvider;
import sinbad2.phasemethod.topsis.unification.ui.view.provider.ExpertColumnLabelProvider;
import sinbad2.phasemethod.topsis.unification.ui.view.provider.TreeViewerContentProvider;
import sinbad2.phasemethod.topsis.unification.ui.view.provider.UnifiedEvaluationColumnLabelProvider;
import sinbad2.resolutionphase.rating.ui.listener.IStepStateListener;
import sinbad2.resolutionphase.rating.ui.view.RatingView;

public class Unification extends ViewPart implements IStepStateListener {
	
	public static final String ID = "flintstones.phasemethod.topsis.unification.ui.view.unification";
	
	private boolean _completed;
	
	private Composite _parent;
	
	private TreeViewer _treeViewer;
	private Tree _tree;
	
	private TreeViewerContentProvider _provider;
	
	private TreeViewerColumn _treeViewerExpertColumn;
	private TreeColumn _treeExpertColumn;
	private TreeViewerColumn _treeViewerAlternativeColumn;
	private TreeColumn _treeAlternativeColumn;
	private TreeViewerColumn _treeViewerCriterionColumn;
	private TreeColumn _treeCriterionColumn;
	private TreeViewerColumn _treeViewerDomainColumn;
	private TreeColumn _treeDomainColumn;
	private TreeViewerColumn _treeViewerEvaluationColumn;
	private TreeColumn _treeEvaluationColumn;
	private TreeViewerColumn _treeViewerUnifiedEvaluationColumn;
	private TreeColumn _treeUnifiedEvaluationColumn;
	
	private FuzzySet _domain;
	
	private Button _saveButton;
	
	private RatingView _ratingView;
	
	private UnificationPhase _unificationPhase;
	
	@Override
	public void createPartControl(Composite parent) {	
		PhasesMethodManager pmm = PhasesMethodManager.getInstance();
		_unificationPhase = (UnificationPhase) pmm.getPhaseMethod(UnificationPhase.ID).getImplementation();
		
		_completed = true;
		
		_parent = parent;
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		_parent.setLayoutData(gridData);
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 15;
		layout.marginLeft = 20;
		layout.marginRight = 20;
		layout.marginBottom = 15;
		layout.marginTop = 20;
		_parent.setLayout(layout);

		createEvaluationsTable();
		createButtons();
		compactTable();
	}
	
	private void createEvaluationsTable() {
		Composite container = new Composite(_parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.verticalIndent = 0;
		container.setLayoutData(gridData);
		GridLayout gl_container = new GridLayout(1, false);
		gl_container.verticalSpacing = 0;
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.horizontalSpacing = 0;
		container.setLayout(gl_container);

		_treeViewer = new TreeViewer(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		_treeViewer.setComparator(new UnificationTreeViewerComparator());
		_tree = _treeViewer.getTree();
		_tree.setHeaderVisible(true);
		_tree.setLinesVisible(true);
		_tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		_treeViewer.addTreeListener(new ITreeViewerListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (_tree != null) {
							if (!_tree.isDisposed()) {
								for (TreeColumn column : _tree.getColumns()) {
									column.pack();
								}
							}
						}
					}
				});
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (_tree != null) {
							if (!_tree.isDisposed()) {
								for (TreeColumn column : _tree.getColumns()) {
									column.pack();
								}
							}
						}
					}
				});
			}
		});
		
		_treeViewerUnifiedEvaluationColumn = new TreeViewerColumn(_treeViewer, SWT.NONE);
		_treeUnifiedEvaluationColumn = _treeViewerUnifiedEvaluationColumn.getColumn();
		_treeUnifiedEvaluationColumn.setWidth(125);
		_treeUnifiedEvaluationColumn.setText("Unified");
		_treeUnifiedEvaluationColumn.setImage(Images.Valuation);
		_treeUnifiedEvaluationColumn.addSelectionListener(getSelectionAdapter(_treeEvaluationColumn, 5));
		_treeViewerUnifiedEvaluationColumn.setLabelProvider(new UnifiedEvaluationColumnLabelProvider());

		_treeViewerExpertColumn = new TreeViewerColumn(_treeViewer, SWT.NONE);
		_treeExpertColumn = _treeViewerExpertColumn.getColumn();
		_treeExpertColumn.setWidth(125);
		_treeExpertColumn.setText("Expert");
		_treeExpertColumn.setImage(Images.GroupOfExperts);
		_treeExpertColumn.addSelectionListener(getSelectionAdapter(_treeExpertColumn, 0));
		_treeViewerExpertColumn.setLabelProvider(new ExpertColumnLabelProvider());

		_treeViewerAlternativeColumn = new TreeViewerColumn(_treeViewer, SWT.NONE);
		_treeAlternativeColumn = _treeViewerAlternativeColumn.getColumn();
		_treeAlternativeColumn.setWidth(125);
		_treeAlternativeColumn.setText("Alternative");
		_treeAlternativeColumn.setImage(Images.Alternative);
		_treeAlternativeColumn.addSelectionListener(getSelectionAdapter(_treeAlternativeColumn, 1));
		_treeViewerAlternativeColumn.setLabelProvider(new AlternativeColumnLabelProvider());

		_treeViewerCriterionColumn = new TreeViewerColumn(_treeViewer, SWT.NONE);
		_treeCriterionColumn = _treeViewerCriterionColumn.getColumn();
		_treeCriterionColumn.setWidth(125);
		_treeCriterionColumn.setText("Criterion");
		_treeCriterionColumn.setImage(Images.Criteria);
		_treeCriterionColumn.addSelectionListener(getSelectionAdapter(_treeCriterionColumn, 2));
		_treeViewerCriterionColumn.setLabelProvider(new CriterionColumnLabelProvider());

		_treeViewerDomainColumn = new TreeViewerColumn(_treeViewer, SWT.NONE);
		_treeDomainColumn = _treeViewerDomainColumn.getColumn();
		_treeDomainColumn.setWidth(125);
		_treeDomainColumn.setText("Source domain");
		_treeDomainColumn.setImage(Images.Domain);
		_treeDomainColumn.addSelectionListener(getSelectionAdapter(_treeDomainColumn, 3));
		_treeViewerDomainColumn.setLabelProvider(new DomainColumnLabelProvider());

		_treeViewerEvaluationColumn = new TreeViewerColumn(_treeViewer, SWT.NONE);
		_treeEvaluationColumn = _treeViewerEvaluationColumn.getColumn();
		_treeEvaluationColumn.setWidth(125);
		_treeEvaluationColumn.setText("Evaluation");
		_treeEvaluationColumn.setImage(Images.Valuation);
		_treeEvaluationColumn.addSelectionListener(getSelectionAdapter(_treeEvaluationColumn, 4));
		_treeViewerEvaluationColumn.setLabelProvider(new EvaluationColumnLabelProvider());
	
		compactTable();
	}
	
	private SelectionAdapter getSelectionAdapter(final TreeColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UnificationTreeViewerComparator comparator = (UnificationTreeViewerComparator) _treeViewer.getComparator();
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				_treeViewer.getTree().setSortDirection(dir);
				_treeViewer.getTree().setSortColumn(column);
				_treeViewer.refresh();
			}
		};
		return selectionAdapter;
	}
	
	private void createButtons() {
		Composite container = new Composite(_parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.verticalIndent = 0;
		container.setLayoutData(gridData);
		GridLayout gl_container = new GridLayout(1, false);
		gl_container.verticalSpacing = 0;
		gl_container.marginWidth = 0;
		gl_container.marginHeight = 0;
		gl_container.horizontalSpacing = 0;
		container.setLayout(gl_container);
		
		_saveButton = new Button(container, SWT.PUSH);
		_saveButton.setText("Save");
		_saveButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT).createImage());
	}

	private void compactTable() {
		_treeExpertColumn.pack();
		_treeAlternativeColumn.pack();
		_treeCriterionColumn.pack();
		_treeDomainColumn.pack();
		_treeEvaluationColumn.pack();
		_treeUnifiedEvaluationColumn.pack();
	}
	
	@Override
	public String getPartName() {
		return "Unification";
	}
	
	@Override
	public void setFocus() {
		_treeViewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		
		_domain = null;
		_completed = false;
	}
	
	@Override
	public void notifyStepStateChange() {
		_domain = (FuzzySet) SelectBLTS.getBLTSDomain();

		_provider = new TreeViewerContentProvider(_unificationPhase.unification(_domain));
		_treeViewer.setContentProvider(_provider);
		_treeViewer.setInput(_provider.getInput());
		
		if(_completed) {
			_ratingView.loadNextStep();
			_completed = false;
		}
	}

	@Override
	public void notifyRatingView(RatingView rating) {
		_ratingView = rating;
	}
}
