package sinbad2.domain.ui.view.domains.provider;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import sinbad2.domain.Domain;
import sinbad2.domain.ui.DomainUIsManager;

public class DomainTypeLabelProvider extends ColumnLabelProvider {
	
	private DomainUIsManager _manager = DomainUIsManager.getInstance();
	
	@Override
	public String getText(Object element) {
		return null;
	}
	
	@Override
	public Image getImage(Object element) {
		return _manager.getDomainUI(((Domain) element).getType()).getIcon();
	}

}
