package cn.com.glsx.maven.plugin.addversion.scm;
import java.io.File;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.AbstractScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
/**
 * @Title: SCMUtil.java
 * @Description:
 * @author Alvin.zengqi
 * @date 2017年5月3日 上午9:28:33
 * @version V1.0
 * @Company: Didihu.com.cn
 * @Copyright Copyright (c) 2015
 */
public class ScmBuilder {
	private ScmManager scmManager;
	private ScmRepository repository;
	private ScmProvider scmProvider;
	private ScmLogDispatcher logger;
	private String urlScm;
	private String username;
	private String password;
	private int shortRevisionLength = -1;

	public ScmBuilder(String urlScm, String username, String password) {
		this.urlScm = urlScm;
		this.username = username;
		this.password = password;
	}

	public ScmBuilder build() throws ScmRepositoryException, NoSuchScmProviderException {
		this.scmManager = new AbstractScmManager() {
			@Override
			protected ScmLogger getScmLogger() {
				if (logger == null) {
					logger = new ScmLogDispatcher();
				}
				return logger;
			}
		};
		this.scmManager.setScmProvider("svn", new SvnJavaScmProvider());
		this.scmManager.setScmProvider("git", new JGitScmProvider());
		this.repository = this.scmManager.makeScmRepository(this.urlScm);
		ScmProviderRepository scmRepo = this.repository.getProviderRepository();
		if (!StringUtils.isEmpty(this.username)) {
			scmRepo.setUser(this.username);
		}
		if (!StringUtils.isEmpty(this.password)) {
			scmRepo.setPassword(this.password);
		}
		this.scmProvider = this.scmManager.getProviderByRepository(this.repository);
		getLog().info("scm build provider:" + this.repository.getProvider() + " scmType:" + this.scmProvider.getScmType());
		return this;
	}

	public List<InfoItem> list(File scmDirectory, List<File> files) throws ScmException {
		List<InfoItem> items = null;
		InfoScmResult scmResult = info(scmDirectory, files);
		if(scmResult != null && !scmResult.getInfoItems().isEmpty()){
			items = scmResult.getInfoItems();
		}
		return items;
	}
	
	public InfoItem get(File scmDirectory) throws ScmException{
		InfoItem item = null;
		InfoScmResult scmResult = info(scmDirectory);
		if(scmResult != null && !scmResult.getInfoItems().isEmpty()){
			item = scmResult.getInfoItems().get(0);
			if(item != null){
				item.setPath(scmDirectory.getPath());
				if(StringUtils.isEmpty(item.getLastChangedRevision())){
					item.setLastChangedRevision(String.valueOf(System.currentTimeMillis()));
				}
			}
		}
		return item;
	}
	
	public InfoScmResult info(File scmDirectory, List<File> files) throws ScmException {
		CommandParameters commandParameters = new CommandParameters();
		if (("git".equals(this.scmProvider.getScmType())) && (this.shortRevisionLength != -1)) {
			getLog().info("ShortRevision tag detected. The value is '" + this.shortRevisionLength + "'.");
			if ((this.shortRevisionLength >= 0) && (this.shortRevisionLength < 4)) {
				getLog().warn(
						"shortRevision parameter less then 4. ShortRevisionLength is relaying on 'git rev-parese --short=LENGTH' command, accordingly to Git rev-parse specification the LENGTH value is miminum 4. ");
			}
			commandParameters.setInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, this.shortRevisionLength);
		}
		return this.scmProvider.info(repository.getProviderRepository(), new ScmFileSet(scmDirectory, files), commandParameters);
	}
	
	public InfoScmResult info(File scmDirectory) throws ScmException {
		return info(scmDirectory, null);
	}
	
	private static Log log;

	public static Log getLog() {
		if (log == null) {
			log = new SystemStreamLog();
		}
		return log;
	}

	public void setUrlScm(String urlScm) {
		this.urlScm = urlScm;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
