package sinbad2.phasemethod.heterogeneous.fusion.unification.ui.view;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import sinbad2.domain.Domain;
import sinbad2.domain.DomainSet;
import sinbad2.domain.DomainsManager;
import sinbad2.domain.linguistic.fuzzy.FuzzySet;
import sinbad2.domain.linguistic.fuzzy.ui.jfreechart.LinguisticDomainChart;
import sinbad2.phasemethod.PhasesMethodManager;
import sinbad2.phasemethod.heterogeneous.fusion.unification.UnificationPhase;
import sinbad2.phasemethod.heterogeneous.fusion.unification.ui.dialog.NewBLTSDomainDialog;
import sinbad2.phasemethod.heterogeneous.fusion.unification.ui.nls.Messages;
import sinbad2.resolutionphase.rating.ui.listener.IStepStateListener;
import sinbad2.resolutionphase.rating.ui.view.RatingView;

public class SelectBLTS extends ViewPart implements IStepStateListener {
	
	public static final String ID = "flintstones.phasemethod.heterogeneous.fusion.unification.ui.view.selectblts"; //$NON-NLS-1$
	
	private Composite _parent;
	private Composite _validDomainsPanel;
	private Composite _selectedDomainPanel;
	private Button _createNewButton;
	
	private TableViewer _validDomainsViewer;
	
	private LinguisticDomainChart _chart;
	
	private ControlAdapter _controlListener;
	
	private Domain _BLTSDomainSelected;
	private List<Domain> _BLTSDomains;
	private DomainSet _domainSet;
	
	private boolean _completed;
	private boolean _loaded;
	
	private RatingView _ratingView;
	
	private UnificationPhase _unificationPhase;
	
	@Override
	public void createPartControl(Composite parent) {
		PhasesMethodManager pmm = PhasesMethodManager.getInstance();
		_unificationPhase = (UnificationPhase) pmm.getPhaseMethod(UnificationPhase.ID).getImplementation();
		
		_completed = false;
		_loaded = false;
		
		_chart = null;
		_controlListener = null;
		
		_parent = parent;
		
		_BLTSDomainSelected = null;
		_BLTSDomains = new LinkedList<Domain>();
		
		DomainsManager domainsManager = DomainsManager.getInstance();
		_domainSet = domainsManager.getActiveDomainSet();
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		_parent.setLayoutData(gridData);
		GridLayout layout = new GridLayout(14, true);
		layout.horizontalSpacing = 15;
		_parent.setLayout(layout);
		
		createValidDomainsPanel();
		createSelectedDomainPanel();
	}

	private void createValidDomainsPanel() {
		_validDomainsPanel = new Composite(_parent, SWT.NONE);
		_validDomainsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.verticalSpacing = 20;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		_validDomainsPanel.setLayout(layout);

		_validDomainsViewer = new TableViewer(_validDomainsPanel, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		_validDomainsViewer.getTable().setLayoutData(gridData);
		_validDomainsViewer.getTable().setHeaderVisible(true);
		_validDomainsViewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public void dispose() {}
			
			@SuppressWarnings("unchecked")
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<Domain>) inputElement).toArray();
			}
		});

		_validDomainsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = _validDomainsViewer.getSelection();
				_BLTSDomainSelected = (FuzzySet) ((IStructuredSelection) selection).getFirstElement();
				_unificationPhase.setUnifiedDomain(_BLTSDomainSelected);
	
				disposeFollowingPhase();
				
				_completed = true;
				notifyStepStateChange();
				refreshChart();
			}
		});
		
		TableViewerColumn col = new TableViewerColumn(_validDomainsViewer, SWT.NONE);
		col.getColumn().setWidth(100);
		col.getColumn().setText(Messages.SelectBLTS_Name);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FuzzySet) element).getId();
			}
		});

		final TableViewerColumn descriptionColumn = new TableViewerColumn(_validDomainsViewer, SWT.NONE);
		descriptionColumn.getColumn().setWidth(100);
		descriptionColumn.getColumn().setText(Messages.SelectBLTS_Description);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FuzzySet) element).formatDescriptionDomain();
			}
		});

		_createNewButton = new Button(_validDomainsPanel, SWT.PUSH);
		_createNewButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		_createNewButton.setText(Messages.SelectBLTS_Create_new);	
		_createNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewBLTSDomainDialog dialog = new NewBLTSDomainDialog(_parent.getShell());
				
				if ( dialog.open() != null) {
					FuzzySet fuzzySet = dialog.getDomain();

					int count = 1;
					for(Domain domain: _BLTSDomains) {
						if(domain.getId().contains(Messages.SelectBLTS_Generate)) {
							count++;
						}
					}
					fuzzySet.setId(Messages.SelectBLTS_Generate_ + count);
					_BLTSDomains.add(fuzzySet);
					
					_validDomainsViewer.refresh();
					descriptionColumn.getColumn().pack();
				}
			}
		});
		
		loadDomains();
		
		descriptionColumn.getColumn().pack();
	}
	
	private void loadDomains() {
		List<Domain> domains = _domainSet.getDomains();
		for(Domain d: domains) {
			if(d instanceof FuzzySet) {
				if(((FuzzySet) d).isBLTS()) {
					_BLTSDomains.add(d);
				}
			}
		}
		
		_validDomainsViewer.setInput(_BLTSDomains);
	}

	private void createSelectedDomainPanel() {
		_selectedDomainPanel = new Composite(_parent, SWT.NONE);
		_selectedDomainPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 8, 1));
		
		refreshChart();	
	}
	
	
	private void refreshChart() {
		removeChart();
		_chart = new LinguisticDomainChart();
		Point size = _selectedDomainPanel.getSize();
		_chart.initialize(_BLTSDomainSelected, _selectedDomainPanel, size.x, size.y, SWT.BORDER);
		
		if (_controlListener == null) {
			_controlListener = new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					refreshChart();
				}
			};
			_selectedDomainPanel.addControlListener(_controlListener);
		}
	}
	
	private void removeChart() {
		if (_chart != null) {
			_chart.getChartComposite().dispose();
		}
	}
	
	@Override
	public String getPartName() {
		return Messages.SelectBLTS_Select_BLTS;
	}
	
	@Override
	public void setFocus() {
		_validDomainsViewer.getControl().setFocus();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		_chart = null;
		_completed = false;
		_controlListener = null;
		_BLTSDomains = null;
		_BLTSDomainSelected = null;
	}

	@Override
	public void notifyStepStateChange() {
		if(_completed && !_loaded) {
			_ratingView.loadNextStep();
			_completed = false;
			_loaded = true;
		}
	}

	private void disposeFollowingPhase() {
		_ratingView.disposeFollowingPhases(3);
	}
	
	@Override
	public void setRatingView(RatingView rating) {
		_ratingView = rating;
	}
}
