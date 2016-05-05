package sinbad2.resolutionscheme.ui.nls;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "sinbad2.resolutionscheme.ui.nls.messages"; //$NON-NLS-1$
	public static String MultiplePerspectives_You_will_lose_all_information;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
