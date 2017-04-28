package cn.com.glsx.maven.plugin.addversion.svn;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
/**
 * @Title: SVNUtil.java
 * @Description:
 * @author Alvin.zengqi
 * @date 2017年4月17日 上午11:04:07
 * @version V1.0
 * @Company: Didihu.com.cn
 * @Copyright Copyright (c) 2015
 */
public class SVNUtil {
	
	private static Log log;
	
	public static Log getLog() {
		if (log == null) {
			log = new SystemStreamLog();
		}
		return log;
	}
	
	public static SVNRepository repository = null;

	/**
	 * init repository library by defferent protocol
	 */
	public static void setupLibrary() {
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
	}

	/**
	 * auth and login svn
	 */
	public static SVNClientManager authSvn(String svnRoot, String username, String password) {
		// init library
		setupLibrary();
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnRoot));
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
			return null;
		}
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password.toCharArray());
		repository.setAuthenticationManager(authManager);
		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		SVNClientManager clientManager = SVNClientManager.newInstance(options, authManager);
		return clientManager;
	}

	/**
	 * Make directory in svn repository
	 * @param clientManager
	 * @param url
	 * @param commitMessage
	 * @return
	 * @throws SVNException
	 */
	public static SVNCommitInfo makeDirectory(SVNClientManager clientManager, SVNURL url, String commitMessage) {
		try {
			return clientManager.getCommitClient().doMkDir(new SVNURL[]{url}, commitMessage);
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
		return null;
	}

	/**
	 * Imports an unversioned directory into a repository location denoted by a
	 * destination URL
	 * 
	 * @param clientManager
	 * @param localPath
	 *            a local unversioned directory or singal file that will be
	 *            imported into a repository;
	 * @param dstURL
	 *            a repository location where the local unversioned
	 *            directory/file will be imported into
	 * @param commitMessage
	 * @param isRecursive
	 * @return
	 */
	public static SVNCommitInfo importDirectory(SVNClientManager clientManager, File localPath, SVNURL dstURL, String commitMessage, boolean isRecursive) {
		try {
			return clientManager.getCommitClient().doImport(localPath, dstURL, commitMessage, null, true, true, SVNDepth.fromRecurse(isRecursive));
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
		return null;
	}

	/**
	 * Puts directories and files under version control
	 * 
	 * @param clientManager
	 *            SVNClientManager
	 * @param wcPath
	 *            work copy path
	 */
	public static void addEntry(SVNClientManager clientManager, File wcPath) {
		try {
			clientManager.getWCClient().doAdd(new File[]{wcPath}, false, false, false, SVNDepth.fromRecurse(true), false, false, true);
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
	}

	/**
	 * Collects status information on a single Working Copy item
	 * 
	 * @param clientManager
	 * @param wcPath
	 *            local item's path
	 * @param remote
	 *            true to check up the status of the item in the repository,
	 *            that will tell if the local item is out-of-date (like '-u'
	 *            option in the SVN client's 'svn status' command), otherwise
	 *            false
	 * @return
	 * @throws SVNException
	 */
	public static SVNStatus showStatus(SVNClientManager clientManager, File wcPath, boolean remote) {
		SVNStatus status = null;
		try {
			status = clientManager.getStatusClient().doStatus(wcPath, remote);
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
		return status;
	}

	/**
	 * Commit work copy's change to svn
	 * 
	 * @param clientManager
	 * @param wcPath
	 *            working copy paths which changes are to be committed
	 * @param keepLocks
	 *            whether to unlock or not files in the repository
	 * @param commitMessage
	 *            commit log message
	 * @return
	 * @throws SVNException
	 */
	public static SVNCommitInfo commit(SVNClientManager clientManager, File wcPath, boolean keepLocks, String commitMessage) {
		try {
			return clientManager.getCommitClient().doCommit(new File[]{wcPath}, keepLocks, commitMessage, null, null, false, false, SVNDepth.fromRecurse(true));
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
		return null;
	}

	/**
	 * Updates a working copy (brings changes from the repository into the
	 * working copy).
	 * 
	 * @param clientManager
	 * @param wcPath
	 *            working copy path
	 * @param updateToRevision
	 *            revision to update to
	 * @param depth
	 *            update
	 * @return
	 * @throws SVNException
	 */
	public static long update(SVNClientManager clientManager, File wcPath, SVNRevision updateToRevision, SVNDepth depth) {
		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		/*
		 * sets externals not to be ignored during the update
		 */
		updateClient.setIgnoreExternals(false);
		/*
		 * returns the number of the revision wcPath was updated to
		 */
		try {
			return updateClient.doUpdate(wcPath, updateToRevision, depth, false, false);
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
		return 0;
	}

	/**
	 * recursively checks out a working copy from url into wcDir
	 * 
	 * @param clientManager
	 * @param url
	 *            a repository location from where a Working Copy will be
	 *            checked out
	 * @param revision
	 *            the desired revision of the Working Copy to be checked out
	 * @param destPath
	 *            the local path where the Working Copy will be placed
	 * @param depth
	 *            checkout
	 * @return
	 * @throws SVNException
	 */
	public static long checkout(SVNClientManager clientManager, SVNURL url, SVNRevision revision, File destPath, SVNDepth depth) {
		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		/*
		 * sets externals not to be ignored during the checkout
		 */
		updateClient.setIgnoreExternals(false);
		/*
		 * returns the number of the revision at which the working copy is
		 */
		try {
			return updateClient.doCheckout(url, destPath, revision, revision, depth, false);
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
		return 0;
	}

	/**
	 * confirm path is workspace
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isWorkingCopy(File path) {
		if (!path.exists()) {
			log.warn("'" + path + "' not exist!");
			return false;
		}
		try {
			if (null == SVNWCUtil.getWorkingCopyRoot(path, false)) { return false; }
		} catch (SVNException e) {
			log.error(e.getErrorMessage().getFullMessage(), e);
		}
		return true;
	}
	
	/**
	 * @Description: Gets the directory and file list for the specified path
	 * @param dirEntries
	 * @param repository
	 * @param path
	 * @return
	 * @throws SVNException
	 * @author Alvin.zengqi  
	 * @date 2017年4月17日 下午3:32:58
	 */
	@SuppressWarnings("unchecked")
	public static List<SVNDirEntry> listEntries(List<SVNDirEntry> dirEntries, SVNRepository repository, String path) throws SVNException{
		Collection<SVNDirEntry> entries = repository.getDir(path, -1, null, (Collection<SVNDirEntry>) null);
		Iterator<SVNDirEntry> iterator = entries.iterator();
		while (iterator.hasNext()) {
			SVNDirEntry entry = (SVNDirEntry) iterator.next();
			entry.setRelativePath((path.equals("") ? "" : path + "/") + entry.getName());
			dirEntries.add(entry);
			if (entry.getKind() == SVNNodeKind.DIR) {
				listEntries(dirEntries, repository, (path.equals("")) ? entry.getName() : path + "/" + entry.getName());
			}
		}
		return dirEntries;
	}
}
