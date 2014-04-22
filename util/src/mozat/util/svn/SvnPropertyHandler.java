package mozat.util.svn;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNPropertyData;

public class SvnPropertyHandler implements ISVNPropertyHandler {

	private String log = null;

	private boolean fetching = false;

	public boolean isFetching() {
		return fetching;
	}

	public void setFetching(boolean fetching) {
		this.fetching = fetching;
	}

	public String getLog() {
		return log;
	}

	@Override
	public void handleProperty(File arg0, SVNPropertyData arg1)
			throws SVNException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleProperty(SVNURL arg0, SVNPropertyData arg1)
			throws SVNException {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleProperty(long arg0, SVNPropertyData arg1)
			throws SVNException {
		log = arg1.getValue().getString();
		fetching = false;
	}

}
