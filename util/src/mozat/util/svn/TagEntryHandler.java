package mozat.util.svn;

import java.util.ArrayList;
import java.util.List;

import mozat.build.info.BaselineInfo;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class TagEntryHandler implements ISVNDirEntryHandler {

	public List<BaselineInfo> getEntries() {
		return entries;
	}

	private List<BaselineInfo> entries = new ArrayList<BaselineInfo>();

	@Override
	public void handleDirEntry(SVNDirEntry entry) throws SVNException {
		if (entry.getKind() == SVNNodeKind.DIR) {
			String name = entry.getName();
			if (name != null) {
				String[] parts = name.split("-");
				if (parts != null && parts.length == 3) {
					BaselineInfo info = new BaselineInfo();
					info.setId(entry.getName());
					info.setUrl(entry.getURL().toString());
					info.setVersion(parts[1]);
					info.setDesc(entry.getCommitMessage());
					SVNRevision revision = SVNRevision.create(entry
							.getRevision());
					info.setRevision(revision.getNumber());
					entries.add(info);
				}
			}
		}

	}
}
