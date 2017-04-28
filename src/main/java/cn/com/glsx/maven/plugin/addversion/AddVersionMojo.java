package cn.com.glsx.maven.plugin.addversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import cn.com.glsx.maven.plugin.sfv.element.LinkTag;
import cn.com.glsx.maven.plugin.sfv.element.ScriptTag;
import cn.com.glsx.maven.plugin.sfv.element.TagUtil;
import cn.com.glsx.maven.plugin.sfv.svn.SVNUtil;

/**
 * @Title: AddVersionMojo.java
 * @Description: web app static file add versoin
 * @author Alvin.zengqi  
 * @date 2017年4月17日 下午3:19:18
 * @version V1.0  
 * @Company: Didihu.com.cn
 * @Copyright Copyright (c) 2015
 */
@Mojo(name = "add-version", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
@Execute(goal = "add-version")
public class AddVersionMojo extends AbstractMojo {

	private static final String[] INCLUDES_DEFAULT = {"jsp", "html", "flt"};
	
	@Parameter(property = "maven.addversion.skip", defaultValue = "false")
	private boolean skip;
	
	/**
	 * project build directory
	 */
	@Parameter(property = "project.build.directory", required = true, readonly = true)
	private File directory;
	
	/**
	 * application name
	 */
	@Parameter(property = "project.artifactId", required = true)
	private String applicationName;
	
	/**
	 * include file type, default:{jsp, html, flt}
	 */
	@Parameter
	private String[] includes;
	
	/**
	 * exclude file
	 */
	@Parameter
	private String[] excludes;
	
	/**
	 * SVN Url path
	 */
	@Parameter(required = true)
	private String svnUrl;
	
	/**
	 * SVN account map
	 * key: userName
	 * key: passWord
	 */
	@Parameter(required = true)
	private Map<String, String> svnAuth;
	
	/**
	 * version param name, default "v"
	 */
	@Parameter(defaultValue = "v")
	private String versionParamName;
	
	/**
	 * web app dynamic file catalog, default "WEB-INF"
	 */
	@Parameter(defaultValue = "WEB-INF")
	private String webInf;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		printParams();
		if(skip){
			return;
		}
		if(includes == null || includes.length == 0){
			includes = INCLUDES_DEFAULT;
		}
		String userName = svnAuth.get("userName");
		if(StringUtils.isEmpty(userName)){
			throw new MojoExecutionException("SVN UserName Required.");
		}
		String passWord = svnAuth.get("passWord");
		if(StringUtils.isEmpty(userName)){
			throw new MojoExecutionException("SVN PassWord Required.");
		}
		try{
			SVNClientManager clientManager = SVNUtil.authSvn(svnUrl, userName, passWord);
			if (null == clientManager) {
				getLog().error("SVN login error! >>> url:" + svnUrl + " username:" + userName + " password:" + passWord);
				throw new MojoExecutionException("SVN login error, Please check username or password!");
			}
			List<SVNDirEntry> dirEntries = new ArrayList<SVNDirEntry>();
			dirEntries = SVNUtil.listEntries(dirEntries, SVNUtil.repository, "");
			Map<String, SVNDirEntry> entryMap = new HashMap<String, SVNDirEntry>();
			if(dirEntries != null && !dirEntries.isEmpty()){
				for(SVNDirEntry entry:dirEntries){
					for(String include:includes){
						if(entry.getName().endsWith("." + include)){
							getLog().debug("svn filePath: " + entry.getRelativePath() + "; author: '" + entry.getAuthor() + "'; revision: " + entry.getRevision() + "; date: " + entry.getDate());
							entryMap.put(fileKey(entry.getRelativePath()), entry);
						}
					}
				}
			}
			List<File> files = new ArrayList<File>();
			files = getBuildDirectoryFiles(files, directory);
			if(files != null && !files.isEmpty()){
				for(File file:files){
					addVersion(file, entryMap.get(fileKey(file.getPath())));
				}
			}
		}catch(Exception e){
			throw new MojoExecutionException("SVN Auth Failed or Path Not Found.", e);
		}
	}
	
	/**
	 * @Description: get build directory files, filter same files in includes list
	 * @param files
	 * @param file
	 * @return
	 * @author Alvin.zengqi  
	 * @date 2017年4月17日 下午4:18:43
	 */
	private List<File> getBuildDirectoryFiles(List<File> files, File file){
		if(file.isFile()){
			for(String include:includes){
				if(file.getName().endsWith("." + include)){
					getLog().debug("target filePath: " + file.getPath());
					files.add(file);
					break;
				}
			}
		}else{
			for(File sub : file.listFiles()){
				getBuildDirectoryFiles(files, sub);
			}
		}
		return files;
	}
	
	/**
	 * @Description: add version
	 * @param file
	 * @author Alvin.zengqi  
	 * @date 2017年4月17日 下午4:25:48
	 */
	private void addVersion(File file, SVNDirEntry entry){
		BufferedReader reader = null;
		BufferedWriter writer = null;
		File tmpFile = new File(directory + File.separator + "TMP-F-" + file.getName());
		try {
			reader = new BufferedReader(new FileReader(file));
			writer = new BufferedWriter(new FileWriter(tmpFile));
			while(reader.ready()){
				String line = reader.readLine();
				if(line.startsWith("<script")){
					ScriptTag scriptTag = TagUtil.tagToBean(line, ScriptTag.class);
					if(scriptTag != null && StringUtils.isNotEmpty(scriptTag.getSrc())){
						String src = addVersion(scriptTag.getSrc(), entry.getRevision());
						if(StringUtils.isNotEmpty(src)){
							getLog().debug("add version before:" + line);
							scriptTag.setSrc(src);
							String script = TagUtil.beanToTag(scriptTag);
							line = script;
							getLog().debug("add version after:" + script);
						}
					}
				}else if(line.startsWith("<link")){
					LinkTag linkTag = TagUtil.tagToBean(line, LinkTag.class);
					if(linkTag != null && StringUtils.isNotEmpty(linkTag.getHref())){
						String href = addVersion(linkTag.getHref(), entry.getRevision());
						if(StringUtils.isNotEmpty(href)){
							getLog().debug("add version before:" + line);
							linkTag.setHref(href);
							String link = TagUtil.beanToTag(linkTag);
							line = link;
							getLog().debug("add version after:" + link);
						}
					}
				}
				writer.write(line);
				writer.newLine();
			}
			writer.flush();
			FileUtils.copyFile(tmpFile, file);
		} catch (Exception e) {
			getLog().error("Reader File:" + file.getPath() + " Error:" + e.getLocalizedMessage(), e);
		} finally {
			try {
				if(reader != null){
					reader.close();
				}
				if(writer != null){
					writer.close();
				}
				tmpFile.delete();
			} catch (IOException e) {
				getLog().error("Close Reader Error", e);
			}
		}
	}
	
	/**
	 * @Description: add version for url
	 * @param url
	 * @return
	 * @author Alvin.zengqi  
	 * @date 2017年4月18日 上午10:11:38
	 */
	private String addVersion(String url, long version){
		if(excludes != null && excludes.length > 0){
			for(String exclude:excludes){
				if(url.contains(exclude)){
					return null;
				}
			}
		}
		Map<String, String> params = parsePath(url);
		params.put(versionParamName, String.valueOf(version));
		url = assembleUrl(params);
		return url;
	}
	
	private Map<String, String> parsePath(String path){
    	Map<String, String> params = new HashMap<String, String>();
    	if(path != null && path.trim().length() > 0 && path.indexOf("?") > 0){
    		String paramstr = path.substring(path.indexOf("?") + 1);
    		params.put("contextPath", path.substring(0, path.indexOf("?")));
    		String[] paramsKV = paramstr.split("&");
    		for(String param:paramsKV){
    			params.put(param.split("=")[0], param.split("=")[1]);
    		}
    	}else{
    		params.put("contextPath", path);
    	}
    	return params;
    }
	
	private String assembleUrl(Map<String,String> params){
        String url = params.get("contextPath");
        params.remove("contextPath");
        String paramstr = "";
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
            	paramstr += "&" + param.getKey() + "=" + param.getValue();
            }
        }
        if (StringUtils.isNotEmpty(paramstr)) {
            url = url + "?" + paramstr.substring(1);
        }
        return url;
    }
	
	private String fileKey(String path){
		String key = path.split(webInf)[1];
		key = key.replaceAll("\\/", "-");
		key = key.replaceAll("\\\\", "-");
		return key;
	}
	
	private void printParams(){
		getLog().info("---------------------------static---file---add---version--------------------------");
		getLog().info("--- skip add version: " + skip);
		getLog().info("---        directory: " + directory.getPath());
		getLog().info("---  applicationName: " + applicationName);
		getLog().info("---           svnUrl: " + svnUrl);
		getLog().info("---          svnAuth: " + svnAuth);
		getLog().info("--- versionParamName: " + versionParamName);
		getLog().info("---         includes: " + Arrays.toString(includes));
		getLog().info("---         excludes: " + Arrays.toString(excludes));
		getLog().info("---------------------------static---file---add---version--------------------------");
	}
}
