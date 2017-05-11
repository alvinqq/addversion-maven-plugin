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
import org.apache.maven.scm.command.info.InfoItem;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import cn.com.glsx.maven.plugin.addversion.element.LinkTag;
import cn.com.glsx.maven.plugin.addversion.element.ScriptTag;
import cn.com.glsx.maven.plugin.addversion.element.TagUtil;
import cn.com.glsx.maven.plugin.addversion.scm.ScmBuilder;

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
	 * project basedir
	 */
	@Parameter(property = "project.basedir", required = true, readonly = true)
	private File basedir;
	
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
	 * SCM Url path
	 * current support scm:svn:svnurl, scm:git:giturl
	 */
	@Parameter(required = true)
	private String urlScm;
	
	/**
	 * using the last committed version of the file
	 */
	@Parameter(property="maven.addversion.useLastCommittedRevision", defaultValue="false")
	private boolean useLastCommittedRevision;
	
	/**
	 * The username that is used when connecting to the SCM system.
	 */
	@Parameter(property="username")
	private String username;

	/**
	 * The password that is used when connecting to the SCM system.
	 */
	@Parameter(property="password")
	private String password;
	
	/**
	 * version param name, default "v"
	 */
	@Parameter(defaultValue = "v")
	private String versionParamName;
	
	/**
	 * web directory, default "src/main/webapp"
	 */
	@Parameter(defaultValue = "src/main/webapp")
	private String webDirectory;
	
	private Map<String, InfoItem> itemMap = new HashMap<String, InfoItem>();
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		printParams();
		if(skip){
			return;
		}
		if(includes == null || includes.length == 0){
			includes = INCLUDES_DEFAULT;
		}
		try{
			ScmBuilder scmBuilder = new ScmBuilder(urlScm, username, password).build();
			
			List<File> webFileList = new ArrayList<File>();
			webFileList = getWebDirectoryFiles(webFileList, basedir);
			if(useLastCommittedRevision && !webFileList.isEmpty()){
				for(File f:webFileList){
					InfoItem item = scmBuilder.get(f);
					if(item != null){
						itemMap.put(f.getName(), item);
					}
				}
			}
			
			List<File> files = new ArrayList<File>();
			files = getBuildDirectoryFiles(files, directory);
			if(files != null && !files.isEmpty()){
				for(File file:files){
					addVersion(file);
				}
			}
		}catch(Exception e){
			throw new MojoExecutionException("Scm Build Failed.", e);
		}
	}
	
	private List<File> getWebDirectoryFiles(List<File> files, File file){
		String[] includes = {"js", "css"};
		if(file.isFile()){
			for(String include:includes){
				if(file.getName().endsWith("." + include)){
					getLog().debug("static file path: " + file.getPath());
					files.add(file);
					break;
				}
			}
		}else{
			for(File sub : file.listFiles()){
				if(sub.getPath().startsWith(basedir.getPath() + "\\" + webDirectory.replaceAll("\\/", "\\\\"))){
					getWebDirectoryFiles(files, sub);
				}
			}
		}
		return files;
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
	private void addVersion(File file){
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
						String src = addUrlVersion(scriptTag.getSrc());
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
						String href = addUrlVersion(linkTag.getHref());
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
	private String addUrlVersion(String url){
		if(excludes != null && excludes.length > 0){
			for(String exclude:excludes){
				if(url.contains(exclude)){
					return null;
				}
			}
		}
		String version = String.valueOf(System.currentTimeMillis());
		Map<String, String> params = parsePath(url);
		if(useLastCommittedRevision){
			String contextPath = params.get("contextPath");
			String[] catalogs = contextPath.split("\\/");
			String fileName = catalogs[catalogs.length -1];
			InfoItem item = itemMap.get(fileName);
			if(item != null){
				version = item.getLastChangedRevision();
			}
		}
		params.put(versionParamName, version);
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
	
	private void printParams(){
		getLog().info("---------------------------static---file---add---version--------------------------");
		getLog().info("--- skip add version: " + skip);
		getLog().info("---        directory: " + directory.getPath());
		getLog().info("---          basedir: " + basedir.getPath());
		getLog().info("---  applicationName: " + applicationName);
		getLog().info("---           urlScm: " + urlScm);
		getLog().info("---         username: " + username);
		getLog().info("---         password: " + password);
		getLog().info("--- versionParamName: " + versionParamName);
		getLog().info("---         includes: " + Arrays.toString(includes));
		getLog().info("---         excludes: " + Arrays.toString(excludes));
		getLog().info("---------------------------static---file---add---version--------------------------");
	}
}
