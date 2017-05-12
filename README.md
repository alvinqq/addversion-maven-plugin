# addversion-maven-plugin

 添加版本号Maven插件（在Web工程打包阶段，对视图文件引用的静态文件添加版本号）

pom.xml添加插件

  	<plugin>
		<groupId>com.github.alvinqq</groupId>
		<artifactId>addversion-maven-plugin</artifactId>
		<version>1.0.1</version>
		<executions>
			<execution>
				<id>static-file-addversion</id>
				<phase>package</phase>
				<goals>
					<goal>add-version</goal>
				</goals>
			</execution>
		</executions>
		<configuration>
    		<!-- 当前工程svn完整路径 -->
	  		<urlScm>scm:svn:svn://192.168.3.233/common/trunk/site</urlScm>
			<username>******</username>
			<password>******</password>
    		<!-- 版本号参数名，默认:v -->
    		<versionParamName>v</versionParamName>
    		<!-- 包含视图文件类型：jsp、flt等，默认:jsp、html、flt -->
			<includes>
				<include>jsp</include>
			</includes>
    		<!-- 排除静态文件列表，打包时不添加版本号，一般应用于第三方的js库或css库 -->
			<excludes>
				<exclude>amazeui.min.css</exclude>
				<exclude>amazeui.min.js</exclude>
				<exclude>jquery.min.js</exclude>
			</excludes>
                        <useLastCommittedRevision>true</useLastCommittedRevision>
		</configuration>
  	</plugin>
            
common.jsp打包前：

	<head>
		<link href="../static/css/amazeui.min.css" type="text/css" rel="stylesheet"/>
		<link href="../static/css/common.css" type="text/css" rel="stylesheet"/>
		<script src="../static/js/common/jquery.min.js" type="text/javascript"></script>
		<script src="../static/js/common/amazeui.min.js" type="text/javascript"></script>
		<script src="../static/js/common/model.js" type="text/javascript"></script>
	</head>

common.jsp打包后:
	
	<head>
		<link href="../static/css/amazeui.min.css" type="text/css" rel="stylesheet"/>
		<link href="../static/css/common.css?v=6587" type="text/css" rel="stylesheet"/>
		<script src="../static/js/common/jquery.min.js" type="text/javascript"></script>
		<script src="../static/js/common/amazeui.min.js" type="text/javascript"></script>
		<script src="../static/js/common/model.js?v=6537" type="text/javascript"></script>
	</head>

