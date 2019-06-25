# 测试整合结果

## 需求

跟据商品id查询商品信息。sql语句：SELECT * from tb_item WHERE id=536563

## Dao层

可以使用逆向工程生成的mapper文件。这样就不需要自己写sql语句，所有常见的数据库交互操作的mapper文件和对应的pojo对象都自动创建了。

## Service层

接收商品id调用dao查询商品信息。返回商品pojo对象。

com.taotao.service/ItemService.java

```java
// 根据商品id获取商品信息的接口
public interface ItemService {
	TbItem getItemById(long itemId);
}
```

com.taotao.service.impl/ItemServiceImpl.java

```java
@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Override
	public TbItem getItemById(long itemId) {
		
		//TbItem item = itemMapper.selectByPrimaryKey(itemId);
		//添加查询条件
		TbItemExample example = new TbItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andIdEqualTo(itemId);
		List<TbItem> list = itemMapper.selectByExample(example);
		if (list != null && list.size() > 0) {
			TbItem item = list.get(0);
			return item;
		}
		return null;
	}

}
```

## Controller层

接收页面请求商品id，调用service查询商品信息。直接返回一个json数据。需要使用@ResponseBody注解。

```java
@Controller
public class ItemController {

	@Autowired
	private ItemService itemService;
	
	@RequestMapping("/item/{itemId}")
	@ResponseBody
	public TbItem getItemById(@PathVariable Long itemId) {
		TbItem tbItem = itemService.getItemById(itemId);
		return tbItem;
	}
}
```

> 注意非war包中的.xml文件需要进行配置，否则在构建的时候会漏掉.xml文件。比如在这个测试中，如果不对mapper的.xml文件配置，最终生成的classes文件中将没有.xml文件，导致找不到映射文件的异常。

```XML
<!-- 如果不添加此节点mybatis的mapper.xml文件都会被漏掉。 -->
	<build>
		<resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.properties</include>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
        </resources>
	</build>
```

## 测试

启动服务器，在网页中访问http://localhost:8080/item/536563，其中536563是要查询的商品的id。


# 商品列表查询展示

## 后台管理系统页面

我们的目的是打开后台管理工程的首页

分析：先写一个controller进行页面跳转来展示首页

```java
// PageController.java

@Controller
public class PageController {
	/**
	 * 打开首页
	 */
	@RequestMapping("/")
	public String showIndex() {
		return "index";
	}
	/**
	 * 展示其他页面
	 * <p>Title: showpage</p>
	 * <p>Description: </p>
	 * @param page
	 * @return
	 */
	@RequestMapping("/{page}")
	public String showpage(@PathVariable String page) {
		return page;
	}
}

```

首页使用easyUI进行开发。静态页面包含js,css和jsp。

## 商品列表查询

### 需求分析

查询语句：SELECT * from tb_item LIMIT 0,30

请求的参数：<http://localhost:8080/item/list?page=1&rows=30>  分页信息。（请求的参数信息可以通过浏览器审查元素进行查询）

### Dao层-分页插件的使用

分页插件

Github上有一个开源项目Mybatis-PageHelper，是目前最简单的分页插件。

#### 实现原理

MyBatis中有一个SqlsessionFactory工厂类，用来创建SqlSession对象；SqlSession对象中含有很多用户方法，用来执行sql语句；具体来说，SqlSession里面有一个Executor对象，用来执行sql语句；而在mybatis里面，sql语句被封装为MappedStatement；

MyBatis中有一个拦截器——Interceptor接口，它会在在执行sql语句之前执行（一些具有某些功能的插件就会实现这个拦截器接口）；我们可以在sql语句中添加limit语句，实现分页处理。

#### 使用方法

第一步：引入pageHelper的jar包。

第二步：需要在SqlMapConfig.xml中配置插件。

```xml
<!-- 配置分页插件 -->
<plugins>
	<plugin interceptor="com.github.pagehelper.PageHelper">
		<!-- 设置数据库类型 Oracle,Mysql,MariaDB,SQLite,Hsqldb,PostgreSQL六种数据库-->       
        <property name="dialect" value="mysql"/>
	</plugin>
</plugins>
```

第三步：在查询的sql语句执行之前，添加一行代码：PageHelper.startPage(1, 10);第一个参数是page，要显示第几页。第二个参数是rows，每页显示的记录数。

第四步：取查询结果的总数量。创建一个PageInfo类的对象，从对象中取分页信息。

#### 分页测试

```java
// 测试类 位于taotao-manager-web/src/test/java/TestPageHelper

public class TestPageHelper {
	@Test
	public void testPageHelper() {
		//创建一个spring容器
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
		//从spring容器中获得Mapper的代理对象
		TbItemMapper mapper = applicationContext.getBean(TbItemMapper.class);
		//执行查询，并分页
		TbItemExample example = new TbItemExample();
		//分页处理 
        //获取第1页的10条内容
		PageHelper.startPage(1, 10);
        //紧跟着的第一个select方法会被分页
		List<TbItem> list = mapper.selectByExample(example);
		//取商品列表
		for (TbItem tbItem : list) {
			System.out.println(tbItem.getTitle());
		}
		//取分页信息
        //分页后，实际返回的结果list类型是Page<E>，如果想取出分页信息，需要强制转换为Page<E>
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);
		long total = pageInfo.getTotal();
		System.out.println("共有商品："+ total);
		
	}
}
```

>分页插件对逆向工程生成的代码支持不好，不能对有查询条件的查询分页。会抛异常。使用修改过的版本就可以了。（资料中给出了修改过后的分页插件pagehelper-3.4.2-fix）

通过以上配置，Dao已经可以实现逆向工程生成的mapper文件+PageHelper实现，但是我们还需要将分页应用到后台管理的页面上。

### Service层

接收分页参数，一个是page一个是rows，然后根据这些参数调用dao查询商品列表并分页，最后返回商品列表。

Easyui中datagrid控件要求的数据格式为json格式：{total:”2”,rows:[{“id”:”1”,”name”,”张三”},{“id”:”2”,”name”,”李四”}]}，所以我们需要创建一个Pojo来返回一个EasyUIDateGrid支持的json数据格式。这个pojo可能会被多个项目使用，所以此pojo应该放到taotao-common工程中。

```java
// 位于taotao-common/src/main/java/com.taotao.common.pojo

public class EUDataGridResult {

	private long total;
	private List<?> rows;
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<?> getRows() {
		return rows;
	}
	public void setRows(List<?> rows) {
		this.rows = rows;
	}
	
}
```

在service层的ItemService接口和实现类中，添加获取带分页的查询商品列表的方法

```java
// 查询商品列表方法 位于ItemServiceImpl.java

public EUDataGridResult getItemList(int page, int rows) {
	//查询商品列表
	TbItemExample example=new TbItemExample();
	//分页处理
	PageHelper.startPage(page, rows);
	List<TbItem> list = itemMapper.selectByExample(example);
	//創建一個返回值對象
	EUDataGridResult result=new EUDataGridResult();
	result.setRows(list);
	//取記錄總條數
	PageInfo<TbItem> pageInfo=new PageInfo<>(list);
	result.setTotal(pageInfo.getTotal());
	return result;
}
```

### Controller层

接收页面传递过来的参数page、rows；返回json格式的数据；EUDataGridResult需要使用到@ResponseBody注解。

```java
// 实际上就是传递page和rows参数给service层中的查询商品列表方法
// 位于ItemController.java

@RequestMapping("/item/list")
@ResponseBody
public EUDataGridResult getItemList(Integer page,Integer rows) {
	EUDataGridResult result=itemService.getItemList(page, rows);
	return result;
}
```

# 商品类目选择

## 需求

在商品添加页面，点击“选择类目”显示树状的商品类目列表；

要求是一个动态树，可以自由选择将闭合的节点展开；

使用EasyUI的异步Tree控件来实现这个“选择类目”功能；

异步Tree：在将闭合节点展开的时候，再做一次查询，将该闭合节点的子节点查询显示出来；

EasyUI的异步树的原理：树支持内置的异步加载模式，因此用户可以创建一个空的树，然后指定一个动态返回 JSON 数据的服务器端，用于根据需求异步填充树。子节点依赖于父节点状态被加载。当展开一个关闭的节点时，如果该节点没有子节点加载，它将通过上面定义的 URL 向服务器发送节点的 id 值作为名为 'id' 的 http 请求参数，以便检索子节点。

每个异步Tree的节点的json数据结构：

```json
[{
    "id": 1,
    "text": "Node 1",
    "state": "closed",
},{
    "id": 2,
    "text": "Node 2",
    "state": "open"
}]
```

数据库中的tb_item_cat为商品分类，包含的列有：id,parent_id,name,status,is_parent等等

## 实现步骤

1、  按钮添加点击事件，弹出窗口，加载数据显示tree

2、  将选择类目的组件封装起来，通过TT.iniit()初始化，最终调用initItemCat()方法进行初始化

3、  创建数据库、以及tb _item_cat表，初始化数据

4、  编写Controller、Service、Mapper

## JSP

selectItemCat作为这个jsp的绑定事件；

```html
<!--该代码位于item_add.jsp-->	
<a href="javascript:void(0)" class="easyui-linkbutton selectItemCat">选择类目</a>
```

使用eclipse的file search功能搜索selectItemCat，默认搜索workspace里面的所有内容，可以设置要搜索的文件的后缀名。

发现selectItemCat位于common.js中。

```javascript
initItemCat : function(data){
    $(".selectItemCat").each(function(i,e){
        var _ele = $(e);
        if(data && data.cid){
            _ele.after("<span style='margin-left:10px;'>"+data.cid+"</span>");
        }else{
            _ele.after("<span style='margin-left:10px;'></span>");
        }
        _ele.unbind('click').click(function(){
            $("<div>").css({padding:"5px"}).html("<ul>")
                .window({
                width:'500',
                height:"450",
                modal:true,
                closed:true,
                iconCls:'icon-save',
                title:'选择类目',
                onOpen : function(){
                    var _win = this;
                    $("ul",_win).tree({
                        // 请求初始化树形视图的url
                        url:'/item/cat/list',
                        animate:true,
                        // 点击父节点，请求初始化子节点的动作是tree控件封装好的。每打开一个父节点做以前的ajax请求
                        onClick : function(node){
                            if($(this).tree("isLeaf",node.target)){
                                // 填写到cid中
                                _ele.parent().find("[name=cid]").val(node.id);
                                _ele.next().text(node.text).attr("cid",node.id);
                                $(_win).window('close');
                                if(data && data.fun){
                                    data.fun.call(this,node);
                                }
                            }
                        }
                    });
                },
                onClose : function(){
                    $(this).window("destroy");
                }
            }).window('open');
        });
    });
},
```

请求初始化树形控件的url为'/item/cat/list'；

## Dao层

sql语句：SELECT * FROM  tb_item_cat WHERE parent_id=父节点id;（每次点击展开父节点的时候会执行这段sql语句）

单表查询：可以使用逆向工程生成的代码

## Service层

功能：接受parentid参数，根据parentId查询子类目类别，返回一个分类列表。所以我们需要创建一个pojo来描述json格式的节点，然后返回一个pojo的列表。

pojo的内容很简单，包含id,text和state属性。因为其他工程也可能用到这个pojo，所以应该放到taotao-common工程中。

```java
// 子类目的pojo，位于taotao-common工程中
public class EUTreeNode {
	private long id;
	private String text;
	private String state;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
}
```

然后将从数据库中根据parentid查询到的数据有选择的封装成EUTreeNode类；

```java
@Service
public class ItemCatServiceImpl implements ItemCatService {
	
	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Override
	public List<EUTreeNode> getCatList(long parentId) {
		// 创建查询条件
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria=example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		// 根据条件查询
		List<TbItemCat> list=itemCatMapper.selectByExample(example);
		List<EUTreeNode> resultList=new ArrayList<>();
		// 把列表转换为treeNodeList
		for(TbItemCat tbItemCat:list) {
			EUTreeNode node=new EUTreeNode();
			node.setId(tbItemCat.getId());
			node.setText(tbItemCat.getName());
			node.setState(tbItemCat.getIsParent()?"closed":"open");
			resultList.add(node);
		}
		return resultList;
	}

}
```

## Controller层

功能：接受页面请求的参数，名为id。调用service查询分类列表，返回json格式的列表，需要使用@ResponseBody注解

```java
@Controller
@RequestMapping("/item/cat")
public class ItemCatController {
	@Autowired
	private ItemCatService itemCatService;
	@RequestMapping("/list")
	@ResponseBody
	private List<EUTreeNode> getItemCatList(@RequestParam(value="id",defaultValue="0")Long parentId){
		List<EUTreeNode> list = itemCatService.getItemCatList(parentId);
		return list;
	}
}
```

## 问题记录

1、商品类目选择没有出现效果：在controller层没有添加@Controller注解；

2、启动服务器访问后出现http 500错误：在service层没有添加@Service注解；

# 图片上传

## 传统项目中的图片管理

传统项目中，可以在web项目中添加一个文件夹，来存放上传的图片。例如在工程的根目录WebRoot下创建一个images文件夹。把图片存放在此文件夹中就可以直接使用在工程中引用。

优点：引用方便，便于管理

缺点：

1、如果是分布式环境图片引用会出现问题：当使用tomcat集群的时候，图片上传到一个tomcat上，访问图片的时候链接到另一个tomcat的时候会找不到图片。

2、图片的下载会给服务器增加额外的压力。

## 分布式环境的图片管理

分布式环境一般都有一个专门的图片服务器存放图片。

我们使用虚拟机搭建一个专门的服务器来存放图片。在此服务器上安装一个nginx来提供http服务，安装一个ftp服务器来提供图片上传服务。

## 搭建图片服务器

### 需要安装的软件

1、linux CentOS6.4

2、Nginx

3、Vsftpd

第一步：安装vsftpd提供ftp服务

第二步：安装nginx提供http服务

### nginx的安装

参考nginx的安装手册

1、安装nginx的编译环境；

2、把nginx的代码上传到linux；

3、解压代码；

4、配置makefile；

> 1、使用yum命令将nginx的编译环境搭建好；
>
> 2、将nginx源码放到/opt文件夹中解压；
>
> 3、./configure \配置安装路径；
>
> 4、make和make install进行安装
>
> 5、在nginx可执行文件安装目录下命令./nginx启动nginx；
>
> 6、在linux浏览器中输入linux的ip地址，出现欢迎界面，说明安装成功；

启动nginx命令：./nginx

关闭nginx命令：./nginx -s stop

如果需要修改nginx的配置文件，执行命令：./nginx -s reload

### vsftpd的安装

参考ftp的安装手册

1、安装vsftpd组件

安装完后，有/etc/vsftpd/vsftpd.conf 文件，是vsftp的配置文件。

[root@bogon ~]# yum -y install vsftpd

2、添加一个ftp用户

此用户就是用来登录ftp服务器用的。

[root@bogon ~]# useradd ftpuser

这样一个用户建完，可以用这个登录，记得用普通登录不要用匿名了。登录后默认的路径为 /home/ftpuser.   

3、给ftp用户添加密码

[root@bogon ~]# passwd ftpuser

输入两次密码后修改密码。

4、防火墙开启21端口

因为ftp默认的端口为21，而centos默认是没有开启的，所以要修改iptables文件

[root@bogon ~]# vim /etc/sysconfig/iptables

在行上面有22 -j ACCEPT 下面另起一行输入跟那行差不多的，只是把22换成21，然后：wq保存。

还要运行下,重启iptables

[root@bogon ~]# service iptables restart

5、修改selinux

6、关闭匿名访问

修改/etc/vsftpd/vsftpd.conf文件

重启ftp服务：

[root@bogon ~]# service vsftpd restart

7、开启被动模式

默认是开启的，但是要指定一个端口范围，打开vsftpd.conf文件，在后面加上

pasv_min_port=30000

pasv_max_port=30999

表示端口范围为30000~30999，这个可以随意改。改完重启一下vsftpd

由于指定这段端口范围，iptables也要相应的开启这个范围，所以像上面那样打开iptables文件。

也是在21上下面另起一行，更那行差不多，只是把21 改为30000:30999,然后:wq保存，重启下iptables。这样就搞定了。

8、设置开机启动vsftpd ftp服务

[root@bogon ~]# chkconfig vsftpd on

### 访问ftp服务

<https://blog.csdn.net/csdn_lqr/article/details/53334583>

### 使用java代码访问ftp服务

使用apache的FTPClient工具访问ftp服务器。需要在pom文件中添加依赖。

```xml
<dependency>
    <groupId>commons-net</groupId>
    <artifactId>commons-net</artifactId>
    <version>${commons-net.version}</version>
</dependency>
```

在eclipse中上传文件到ftp服务器的测试

```java
public class FTPTest {
	@Test
	public void testFtpClient() throws Exception{
		// 创建一个FtpClient对象
		FTPClient ftpClient=new FTPClient();
		// 创建一个ftp连接
		ftpClient.connect("192.168.222.128",21);
		// 登录ftp服务器，使用用户名和密码
		ftpClient.login("ftpuser", "123");
		// 上传文件
		//读取本地文件
		FileInputStream inputStream=new FileInputStream(new File("D:\\Picture\\test.jpg"));
		// 设置上传的路径
		ftpClient.changeWorkingDirectory("/home/ftpuser/www/images");
		// 修改上传文件的格式
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		// 第一个参数：服务器端文件名
		// 第二个参数：上传文档的inputStream
		ftpClient.storeFile("hello1.jpg", inputStream);
		// 关闭连接
		ftpClient.logout();
	}
}
```

将ftp上传功能封装成一个工具类，可以供其他项目使用，提高代码的复用性。

封装类用教程里给的工具类。

## 图片上传实现

### 图片上传Service

1、上传图片的url：/pic/upload

2、上传图片参数名称：uploadFile

3、返回结果数据类型json：参考文档<http://kindeditor.net/docs/upload.html>

```json
//成功时
{
        "error" : 0,
        "url" : "http://www.example.com/path/to/file.ext"
}
//失败时
{
        "error" : 1,
        "message" : "错误信息"
}

```

图片上传不涉及数据库，所以不需要写Dao层。

**Service层**

接受Controller传递过来的参数——一个文件MultiPartFile对象，然后把文件上传到ftp服务器，生成一个新的文件名，并返回文件url路径，需要保证满足图片上传插件要求的格式。

使用Map来实现插件要求的json格式（可以省去写pojo）：

Key                         value

------

Error                      1、0

Url                          图片的url（成功时）

Message                图片信息（错误时）

```properties
# 图片服务器的一些配置
FTP_ADDRESS=192.168.222.128
FTP_PORT=21
FTP_USERNAME=ftpuser
FTP_PASSWORD=123
FTP_BASE_PATH=/home/ftpuser/www/images
IMAGE_BASE_URL=http://192.168.222.128/images
```



```java
// 将图片上传到ftp服务器
// com.taotao.service.impl.PictureServiceImpl

@Service
public class PictureServiceImpl implements PictureService {
	// 将图片服务器菩配置通过Value注解导入
	@Value("${FTP_ADDRESS}")
	private String FTP_ADDRESS;
	@Value("${FTP_PORT}")
	private int FTP_PORT;
	@Value("${FTP_USERNAME}")
	private String FTP_USERNAME;
	@Value("${FTP_PASSWORD}")
	private String FTP_PASSWORD;
	@Value("${FTP_BASE_PATH}")
	private String FTP_BASE_PATH;
	@Value("${IMAGES_BASE_URL}")
	private String IMAGES_BASE_URL;

	@Override
	public Map uploadPicture(MultipartFile uploadFile){
		Map resultMap=new HashMap<>();
		try {
			// 生成一个新的文件名
			// 取原始文件名
			String oldName=uploadFile.getOriginalFilename();
			// 生成新的文件名
			// UUID.randomUUID();
			String newName=IDUtils.genImageName();
			// 截取扩展名
			newName=newName+oldName.substring(oldName.lastIndexOf("."));
			// 图片上传
			String imagePath=new DateTime().toString("/yyyy/MM/dd");
			boolean result=FtpUtil.uploadFile(FTP_ADDRESS, FTP_PORT, FTP_USERNAME, FTP_PASSWORD, FTP_BASE_PATH, imagePath, newName, uploadFile.getInputStream());
			// 返回结果
			if(!result) {
				resultMap.put("error",1);
				resultMap.put("message","文件上传失败");
				return resultMap;
			}
			resultMap.put("error",0);
			resultMap.put("url", IMAGES_BASE_URL+imagePath+"/"+newName);
			return resultMap;
			
		}catch (Exception e) {
			// TODO: handle exception
			resultMap.put("error",1);
			resultMap.put("message","文件上传发生异常");
			return resultMap;
		}

	}

}
```

### 图片上传Controller

功能：接收页面传递过来的图片，调用service上传到图片服务器，然后返回json数据格式的结果（使用@ResponseBody注解）。

参数：MultiPartFile uploadFile

返回值：返回json数据，可以返回一个pojo，PictureResult对象，也可以返回一个map。

```java
// com.taotao.controller.PictureController

@Controller
public class PictureController {
	@Autowired
	private PictureService pictureService;
	
	@RequestMapping("/pic/upload")
	@ResponseBody
	public Map pictureUpload(MultipartFile uploadFile) {
		Map result=pictureService.uploadPicture(uploadFile);
		return result;
	}
}
```

需要引入file-upload和common-io包（在pom文件中已经添加了依赖）；

需要在SpringMVC.xml中配置多部件解析器，添加如下内容：

```xml
<!-- 定义文件上传解析器 -->
	<bean id="multipartResolver"
		class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
		<!-- 设定默认编码 -->
		<property name="defaultEncoding" value="UTF-8"></property>
		<!-- 设定文件上传的最大值5MB，5*1024*1024 -->
		<property name="maxUploadSize" value="5242880"></property>
	</bean>
```

为了解决浏览器兼容性的问题，需要将map转换为json格式，才能使上传图片插件在多个浏览器中可用。

```java
@Controller
public class PictureController {
	@Autowired
	private PictureService pictureService;
	
	@RequestMapping("/pic/upload")
	@ResponseBody
	public String pictureUpload(MultipartFile uploadFile) {
		Map result=pictureService.uploadPicture(uploadFile);
		// 为了保证功能的兼容性，需要把Result转换为json格式的字符串
		String json=JsonUtils.objectToJson(result);
		return json;
	}
}
```

# 商品添加实现

## 富文本编辑器

功能：添加商品描述的文本编辑窗口；（前端知识）

第一步：在jsp中加入富文本编辑器js的引用。

第二步：在富文本编辑器出现的位置添加一个input 类型为textarea

第三步：调用js方法初始化富文本编辑器。

第四步：提交表单时，调用富文本编辑器的同步方法sync，把富文本编辑器中的内容同步到textarea中。

## 商品添加实现

### 需求分析

1、请求的url：/item/save

2、返回结果，自定义一个TaotaoResult类，类的具体内容参见参考资料。

### Dao层

数据库中分为商品表、商品描述表；分开的目的是为了提高查询效率。

单表查询，可以直接使用逆向工程生成的mapper。

### Service层

功能分析：接收controller传递过来的item对象，并根据这个对象把不为空的字段都补全（比如生成商品id），然后向item表中插入数据。

参数：TbItem

返回值：TaotaoResult

```java
// 接受controller传递过来的TbItem对象，然后根据这个对象把不为空的字段补全，最后向item表中插入数据
// com.taotao.service.impl.ItemServiceImpl.addItem(TbItem)

	@Override
	public TaotaoResult addItem(TbItem item) {
		try {
			//生成商品id
			//可以使用redis的自增长key，在没有redis之前使用时间+随机数策略生成
			Long itemId = IDUtils.genItemId();
			//补全不完整的字段
			item.setId(itemId);
			item.setStatus((byte) 1);
			Date date = new Date();
			item.setCreated(date);
			item.setUpdated(date);
			//把数据插入到商品表
			itemMapper.insert(item);
			
		} catch (Exception e) {
			e.printStackTrace();
			return TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		
		return TaotaoResult.ok();
	}
```

### Controller层

```java
// 功能分析：接收页面传递过来的数据包括商品和商品描述。
// 参数：TbItem。
// 返回值：TaotaoResult
// com.taotao.controller.ItemController.addItem(TbItem)

	@RequestMapping("/item/save")
	@ResponseBody
	public TaotaoResult addItem(TbItem item) {
		TaotaoResult result = itemService.addItem(item);
		return result;
	}
```

## 商品描述的添加

### 需求分析

后台要接收前台页面提交的商品信息，及商品描述；商品信息保存的同时还要保存商品描述。并且再数据库中商品信息和商品描述也是分开存储的，这样便于修改。

商品描述：html文档元素；

### Service层

```java
// com.taotao.service.impl.ItemServiceImpl
// 与上面相比多了商品描述的添加

	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemDescMapper itemDescMapper;
	@Override
	public TaotaoResult addItem(TbItem item, String desc) throws Exception{
		//生成商品id
		//可以使用redis的自增长key，在没有redis之前使用时间+随机数策略生成
		Long itemId = IDUtils.genItemId();
		//补全不完整的字段
		item.setId(itemId);
		item.setStatus((byte) 1);
		Date date = new Date();
		item.setCreated(date);
		item.setUpdated(date);
		//把数据插入到商品表
		itemMapper.insert(item);
		//添加商品描述信息
		TaotaoResult result = insertItemDesc(itemId, desc);
		if (result.getStatus() != 200) {
			throw new Exception();
		}
		return TaotaoResult.ok();
	}

	private TaotaoResult insertItemDesc(Long itemId, String desc) {
		TbItemDesc itemDesc = new TbItemDesc();
		itemDesc.setItemId(itemId);
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(new Date());
		itemDesc.setUpdated(new Date());
		itemDescMapper.insert(itemDesc);
		return TaotaoResult.ok();
	}
```

### Controller层

```java
// com.taotao.controller.ItemController
// 比商品添加多了商品描述的添加功能

	@RequestMapping(value="/item/save", method=RequestMethod.POST)
	@ResponseBody
	public TaotaoResult addItem(TbItem item, String desc) throws Exception {
		TaotaoResult result = itemService.addItem(item, desc);
		return result;
	}
```

# 商品规格参数

## 数据库存储

数据库中有两个表：规格参数模板表、商品的规格参数表；

优点：

1、不需要做多表管理。

2、如果要求新添加的商品规格项发生改变，之前的商品不变是很简单的。

缺点：

复杂的表单和json之间的转换。对js的编写要求很高。

## 添加商品规格参数模板

业务逻辑：在规格参数界面选择类目后可以为不同的类目新增规格参数模板（同一个类目的商品具有同一个规格参数模板）；

选择商品分类后根据选择的商品分类到tb_item_param规格参数模板表中取规格模板，取到了说明此商品分类的规格模板已经添加提示不能添加，如果没有取得则正常添加。

### 需求分析

请求的url：/item/param/query/itemcatid/{itemCatId}

### Dao层

从tb_item_param表中根据商品分类id查询内容，这是单表操作，可以使用逆向工程的代码。

### Service层

功能：接收商品分类id，调用mapper查询tb_item_param表，返回结果TaotaoResult。

```java
// com.taotao.service.impl.ItemParamServiceImpl

@Service
public class ItemParamServiceImpl implements ItemParamService {

	@Autowired
	private TbItemParamMapper itemParamMapper;

	
	@Override
	public TaotaoResult getItemParamByCid(long cid) {
		TbItemParamExample example = new TbItemParamExample();
		Criteria criteria = example.createCriteria();
		criteria.andItemCatIdEqualTo(cid);
		List<TbItemParam> list = itemParamMapper.selectByExampleWithBLOBs(example);
		//判断是否查询到结果
		if (list != null && list.size() > 0) {
			return TaotaoResult.ok(list.get(0));
		}
		
		return TaotaoResult.ok();
	}
}
```

### Controller层

接收cid参数，调用Service查询规格参数模板，返回TaotaoResult，最后返回json数据。

```java
@Controller
@RequestMapping("/item/param")
public class ItemParamController {

	@Autowired
	private ItemParamService itemParamService;
	
	@RequestMapping("/query/itemcatid/{itemCatId}")
	@ResponseBody
	public TaotaoResult getItemParamByCid(@PathVariable Long itemCatId) {
		TaotaoResult result = itemParamService.getItemParamByCid(itemCatId);
		return result;
	}

}
```

## 提交商品规格参数模板

### 需求分析

首先把页面中所有文本框中的内容转换成json数据。把json字符串提交给后台。保存到规格参数表中。

### Dao层

保存规格参数模板，向tb_item_param表添加一条记录。可以使用逆向工程生成的代码。

### Service层

功能：接收TbItemParam对象，把对象调用mapper插入到tb_item_param表中，最后返回TaotaoResult。

```java
// com.taotao.service.impl.ItemParamServiceImpl
	@Override
	public TaotaoResult insertItemParam(TbItemParam itemParam) {
		//补全pojo
		itemParam.setCreated(new Date());
		itemParam.setUpdated(new Date());
		//插入到规格参数模板表
		itemParamMapper.insert(itemParam);
		return TaotaoResult.ok();
	}
```

### Controller层

功能：接收cid、规格参数模板，创建一TbItemParam对象，并调用Service返回TaotaoResult，最后返回json数据。

```java
// com.taotao.controller.ItemParamController
	@RequestMapping("/save/{cid}")
	@ResponseBody
	public TaotaoResult insertItemParam(@PathVariable Long cid, String paramData) {
		//创建pojo对象
		TbItemParam itemParam = new TbItemParam();
		itemParam.setItemCatId(cid);
		itemParam.setParamData(paramData);
		TaotaoResult result = itemParamService.insertItemParam(itemParam);
		return result;
	}
```

##  根据规格参数模板生成表单

在商品添加功能中，读取此商品对应的规格模板，生成表单。供使用者添加规格参数。

Service层的修改：

```java
// com.taotao.service.impl.ItemParamServiceImpl
	@Override
	public TaotaoResult getItemParamByCid(long cid) {
		TbItemParamExample example = new TbItemParamExample();
		Criteria criteria = example.createCriteria();
		criteria.andItemCatIdEqualTo(cid);
		List<TbItemParam> list = itemParamMapper.selectByExampleWithBLOBs(example);
		//判断是否查询到结果
		if (list != null && list.size() > 0) {
			return TaotaoResult.ok(list.get(0));
		}
		
		return TaotaoResult.ok();
	}
```

##  保存商品的规格参数

### 需求分析

提交表单之前，先把规格参数表单中的内容转换成json数据然后跟商品基本信息、商品描述同时提交给后台。保存至数据库。

主要是item_add.jsp中的修改；

### Dao层

需要向tb_item_param_item表中添加数据。

### Service层

接收规格参数的内容，和商品id，拼装成pojo调用mapper 的方法tb_item_param_item表中添加数据返回TaotaoResult。

```java
// com.taotao.service.impl.ItemServiceImpl

	@Override
	public TaotaoResult insertItemParamItem(Long itemId, String itemParam) {
		//创建一个pojo
		TbItemParamItem itemParamItem = new TbItemParamItem();
		itemParamItem.setItemId(itemId);
		itemParamItem.setParamData(itemParam);
		itemParamItem.setCreated(new Date());
		itemParamItem.setUpdated(new Date());
		//向表中插入数据
		itemParamItemMapper.insert(itemParamItem);
		
		return TaotaoResult.ok();
		
	}
```

### Controller层

```java
// com.taotao.controller.ItemController

	@RequestMapping(value="/item/save", method=RequestMethod.POST)
	@ResponseBody
	public TaotaoResult addItem(TbItem item, String desc, String itemParams) throws Exception {
		TaotaoResult result = itemService.addItem(item, desc, itemParams);
		return result;
	}
```

## 展示规格参数

当现实商品详情页面时，需要把商品的规格参数根据商品id取出来，生成html展示到页面。（本节只需要通过网址发送商品id显示规格参数即可）

### Dao层

根据商品id查询规格参数，单表查询，使用逆向工程生成的文件。

### Service层

接收商品id查询规格参数表。根据返回的规格参数生成html返回html。

```java
// com.taotao.service.impl.ItemParamItemServiceImpl

@Service
public class ItemParamItemServiceImpl implements ItemParamItemService {

	@Autowired
	private TbItemParamItemMapper itemParamItemMapper;
	
	@Override
	public String getItemParamByItemId(Long itemId) {
		//根据商品id查询规格参数
		TbItemParamItemExample example = new TbItemParamItemExample();
		Criteria criteria = example.createCriteria();
		criteria.andItemIdEqualTo(itemId);
		//执行查询
		List<TbItemParamItem> list = itemParamItemMapper.selectByExampleWithBLOBs(example);
		if (list == null || list.size() == 0) {
			return "";
		}
		//取规格参数信息
		TbItemParamItem itemParamItem = list.get(0);
		String paramData = itemParamItem.getParamData();
		//生成html
		// 把规格参数json数据转换成java对象
		List<Map> jsonList = JsonUtils.jsonToList(paramData, Map.class);
		StringBuffer sb = new StringBuffer();
		sb.append("<table cellpadding=\"0\" cellspacing=\"1\" width=\"100%\" border=\"1\" class=\"Ptable\">\n");
		sb.append("    <tbody>\n");
		for(Map m1:jsonList) {
			sb.append("        <tr>\n");
			sb.append("            <th class=\"tdTitle\" colspan=\"2\">"+m1.get("group")+"</th>\n");
			sb.append("        </tr>\n");
			List<Map> list2 = (List<Map>) m1.get("params");
			for(Map m2:list2) {
				sb.append("        <tr>\n");
				sb.append("            <td class=\"tdTitle\">"+m2.get("k")+"</td>\n");
				sb.append("            <td>"+m2.get("v")+"</td>\n");
				sb.append("        </tr>\n");
			}
		}
		sb.append("    </tbody>\n");
		sb.append("</table>");
		return sb.toString();
	}

}
```

### Controller层

接收商品id调用Service查询规格参数信息，得到规格参数的html。返回一个逻辑视图。把html展示到页面。

```java
@Controller
public class ItemParamItemController {

	@Autowired
	private ItemParamItemService itemParamItemService;
	
	@RequestMapping("/showitem/{itemId}")
	public String showItemParam(@PathVariable Long itemId, Model model) {
		String string = itemParamItemService.getItemParamByItemId(itemId);
		model.addAttribute("itemParam", string);
		return "item";
	}
}
```

# 前台系统搭建

## 前台系统系统架构

在互联网系统开发当中，我们一般都是采用了分层的方式来架构系统，但是为什么我们需要分层进行架构呢？

采用分层架构有利于系统的维护，系统的扩展。这其实就是系统的可维护性和可扩展性。分层就是按照功能把系统切分细分，细分之后就能分布式部署，就能引入伸缩性，就能提高性能。

好处：

1、基于soa理念将服务层抽出对外提供服务

2、可以实现灵活的分布式部署

## 搭建服务系统

服务形式：对外提供rest形式的服务，供其他系统调用。使用http协议传递json数据。

### 使用的技术

1、Mybatis

2、spring

3、springmvc

### 创建maven工程

### Pom文件

### web.xml

### 整合ssm

### Tomcat插件配置

### 安装taotao-manager到本地仓库

## 商品分类展示

### 需求分析

首页左侧有一个商品分类。当鼠标分类上，需要展示出此分类下的子分类。

当鼠标滑动到连接上触发mousemove事件，页面做一个ajax请求，请求json数据包含分类信息，得到json数据后初始化分类菜单并展示。

taotao-portal的前端代码文件是从京东上拷下来的，可以使用浏览器的审查元素来观察请求发送的url或代码在文件中的位置。

### Json数据

在webapp下建立一个category.json文件，将分类json数据放进去，这时候使用http://localhost:8082/category.json可以访问这个json文件。

### 使用ajax访问本工程的json数据

将js中的getJson的参数URL_Serv设置为http://localhost:8082/category.json，然后启动taotao-rest服务，就可以使用商品分类展示的功能了。

### Ajax跨域请求

这是另一种商品分类展示的方法，由客户端直接访问taotao-rest来显示商品分类。

Js是不能跨域请求的，出于安全考虑，js设计时不可以跨域。

什么是跨域：

1、域名不同时。

2、域名相同，端口不同。

只有域名相同、端口相同时，才可以访问；可以使用jsonp解决跨域问题。

### 什么是jsonp

Jsonp其实就是一个跨域解决方案。Js跨域请求数据是不可以的，但是js跨域请求js脚本是可以的。可以把数据封装成一个js语句，做一个方法的调用。跨域请求js脚本可以得到此脚本。得到js脚本之后会立即执行。可以把数据做为参数传递到方法中。就可以获得数据。从而解决跨域问题。

### jsonp的原理

浏览器在js请求中，是允许通过script标签的src跨域请求，可以在请求的结果中添加回调方法名，在请求页面中定义方法，既可获取到跨域请求的数据。

### 跨域请求的实现

将category.json文件放到taotao-rest下，这时候可以通过http://localhost:8081/category.json访问；在js中使用getJsonp方法，并将json数据改为js脚本。

### 从数据库中取商品分类列表

相比之前在category.json中定义的静态商品分类数据，这里从数据库取动态的商品分类列表，在首页展示的效果是类似的。

# 后台内容管理

## 删除节点

### 需求分析

请求的url：/content/category/delete/

参数：Id（从jsp中获取）

返回值：TaotaoResult

### Dao层

在mapper xml文件中添加以下的查询语句：

根据parentId删除

```sql
  <delete id="deleteByParentId" parameterType="java.lang.Long">
  	delete from tb_content_category
  	where parent_id=#{id,jdbcType=BIGINT}
  </delete>
```

根据parentId查询父结点的子节点个数

```sql
  <select id="countByParentId" parameterType="java.lang.Long" resultType="java.lang.Integer">
  	select count(*) from tb_content_category where parent_id=#{id ,jdbcType=BIGINT}
  </select>
```

其实可以直接使用逆向工程的精确查询，不过直接添加查询语句也很方便。

### Service层

这里有一个bug，对于拥有两层及以上的子节点的节点来说，如果直接将这个节点删除，那么只会删除它的第一层的子节点，而不会删除更下层的节点。这个问题待解决，用递归删除应该能解决。

```java
	@Override
	public TaotaoResult deleteContentCategory(long id) {
		// 获取当前节点的父结点id
		TbContentCategory contentCategory=contentCategoryMapper.selectByPrimaryKey(id);
		long parentId=contentCategory.getParentId();
		// 删除子结点
		contentCategoryMapper.deleteByPrimaryKey(id);
		contentCategoryMapper.deleteByParentId(id);
		// 统计父节点的子节点数，如果父节点没有子结点了，设置isParent为false
		int childNum = contentCategoryMapper.countByParentId(parentId);
		if (childNum == 0) {
			TbContentCategory parentCat = contentCategoryMapper.selectByPrimaryKey(parentId);
			parentCat.setIsParent(false);
			contentCategoryMapper.updateByPrimaryKey(parentCat);
		}

		return TaotaoResult.ok();
	}
```

### Controller层

```java
	@RequestMapping("/delete")
	@ResponseBody
	public TaotaoResult deleteContentCategory(Long id) {
		TaotaoResult result=contentCategoryService.deleteContentCategory(id);
		return result;
	}
```

## 内容列表查询展示

### 内容管理表

在数据库中创建内容管理表；

```sql
CREATE TABLE `tb_content` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_id` bigint(20) NOT NULL COMMENT '内容类目ID',
  `title` varchar(200) DEFAULT NULL COMMENT '内容标题',
  `sub_title` varchar(100) DEFAULT NULL COMMENT '子标题',
  `title_desc` varchar(500) DEFAULT NULL COMMENT '标题描述',
  `url` varchar(500) DEFAULT NULL COMMENT '链接',
  `pic` varchar(300) DEFAULT NULL COMMENT '图片绝对路径',
  `pic2` varchar(300) DEFAULT NULL COMMENT '图片2',
  `content` text COMMENT '内容',
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  KEY `updated` (`updated`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8;
```

### 需求分析

**数据库查询语句**：SELECT * from tb_content where id=#{categoryId}

**请求的url**：/content/query/list

**请求参数**：http://localhost:8080/content/query/list?categoryId=117&page=1&rows=20  

包含内容类别id：categoryId（对应内容页的id）；分页信息：page和rows；

根据categoryId从tb_content下找到所有categoryId等于传进来的categoryId参数的广告；

**响应的数据格式**：EasyUIResult

### Dao层

使用分页插件，分页插件在商品列表展示的地方配置过滤，这里只要直接使用。

### Service层

接收分页参数，一个是page一个是rows，然后根据这些参数和categoryId调用dao查询内容列表并分页，最后返回内容列表。

Easyui中datagrid控件要求的数据格式为json格式：{total:”2”,rows:[{“id”:”1”,”name”,”张三”},{“id”:”2”,”name”,”李四”}]}，所以我们需要创建一个Pojo来返回一个EasyUIDateGrid支持的json数据格式。这个pojo可能会被多个项目使用，所以此pojo应该放到taotao-common工程中。（在商品列表展示的过程中已经创建了这个pojo，所以不用再创建，直接使用）

```java
// 位于taotao-common/src/main/java/com.taotao.common.pojo

public class EUDataGridResult {

	private long total;
	private List<?> rows;
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<?> getRows() {
		return rows;
	}
	public void setRows(List<?> rows) {
		this.rows = rows;
	}
	
}
```

根据categoryId，page，rows参数去数据库中查询符合categoryId的内容，并封装成为EUDataGridResult类的形式，然后返回给Controller。

```java
// com.taotao.service.impl.ContentServiceImpl

@Autowired
private TbContentMapper tbContentMapper;

public EUDataGridResult getContentList(Long categoryId, int page, int rows) {
    // 设置查询 条件 categoryId
    TbContentExample example = new TbContentExample();
    Criteria criteria = example.createCriteria();
    criteria.andCategoryIdEqualTo(categoryId);
    // 查询前分页
    PageHelper.startPage(page, rows);
    // 按条件查询
    List<TbContent> list = tbContentMapper.selectByExample(example);
    // 将查询条件封装到EasyUI能接受的格式
    EUDataGridResult result = new EUDataGridResult();
    result.setRows(list);
    PageInfo<TbContent> info=new PageInfo<TbContent>(list);
    result.setTotal(info.getTotal());
    return result;
}
```

### Controller层

调用service层的方法，然后返回。

```java
@Controller
public class ContentController {
	
	@Autowired
	ContentService contentService;
	
	@RequestMapping("/content/query/list")
	@ResponseBody
	public EUDataGridResult getContentList(Long categoryId, int page ,int rows) {
		EUDataGridResult result=contentService.getContentList(categoryId, page, rows);
		return result;
	}

}
```

# redis缓存

## redis介绍

redis是C语言开发，建议在linux上运行，本教程使用Centos6.4作为安装环境。

### gcc环境

安装redis需要先将官网下载的源码进行编译，编译依赖gcc环境，如果没有gcc环境，需要安装gcc：yum install -y gcc-c++

安装位置：使用yum安装，对于不同的软件有不同的安装位置。

本教程使用redis3.0版本，3.0版本主要增加了redis集群功能。

### 安装

将redis源码包上传到linux服务器，然后解压、编译和安装；

- 将redis-3.0.0.tar.gz拷贝到/usr/local下；
- 解压：tar -zxvf redis-3.0.0.tar.gz；
- 编译：cd /usr/local/redis-3.0.0  make；
- 安装：安装到指定目录cd /usr/local/redis-3.0.0 make PREFIX=/usr/local/redis install；

### 启动

前端模式启动：./redis-server；前端模式启动的缺点是ssh命令窗口关闭则redis-server程序结束，不推荐使用此方法。

后端模式启动：在redis解压的包中找到redis.conf，然后拷到redis安装文件夹中，修改redis.conf配置文件， daemonize yes 以后端模式启动。执行如下命令启动redis：cd /usr/local/redis；./bin/redis-server；./redis.conf

查看是否启动成功：ps aux|grep redis

### 测试

启动redis客户端 ./redis-cli

常用测试命令：ping；set a 10；get a；

## redis集群

### redis-cluster架构

- 所有的redis节点彼此互联(PING-PONG机制),内部使用二进制协议优化传输速度和带宽.
- 节点的fail是通过集群中超过半数的节点检测失效时才生效。redis-cluster投票:容错
- 客户端与redis节点直连,不需要中间proxy层.客户端不需要连接集群所有节点,连接集群中任何一个可用节点即可
- redis-cluster把所有的物理节点映射到[0-16383]slot上,cluster 负责维护node<->slot<->value
- Redis 集群中内置了 16384 个哈希槽，当需要在 Redis 集群中放置一个 key-value 时，redis 先对 key 使用 crc16 算法算出一个结果，然后把结果对 16384 求余数，这样每个 key 都会对应一个编号在 0-16383 之间的哈希槽，redis 会根据节点数量大致均等的将哈希槽映射到不同的节点
- 什么时候整个集群不可用(cluster_state:fail)? a:如果集群任意master挂掉,且当前master没有slave.集群进入fail状态,也可以理解成集群的slot映射[0-16383]不完成时进入fail状态. ps : redis-3.0.0.rc1加入cluster-require-full-coverage参数,默认关闭,打开集群兼容部分失败.b:如果集群超过半数以上master挂掉，无论是否有slave集群进入fail状态.

> ps:当集群不可用时,所有对集群的操作做都不可用，收到((error) CLUSTERDOWN The cluster is down)错误

### 我们的集群架构

  集群中有三个节点的集群，每个节点有一主一备，需要6台虚拟机。搭建一个伪分布式的集群，使用6个redis实例来模拟。

### 搭建集群需要的环境

搭建集群需要使用官方提供的ruby脚本，需要安装ruby的环境。

> rubygems：ruby的包管理器；

安装ruby：yum install ruby；yum install rubygems；

> redis集群管理工具redis-trib.rb（ruby脚本）：位于redis安装包的src里面；

redis集群管理工具所依赖的ruby包：redis-3.0.0.gem；上传到服务器的/usr/local/下，执行gem install redis-3.0.0.gem

### 搭建集群

（1）在/usr/local/下建立一个空文件夹redis-cluster，然后将redis的bin文件夹内容复制到该文件下的redis01文件夹中

```
cp -r bin ../redis-cluster/redis01
```

删除redis01下的redis持久化文件dump.rdb，并修改配置文件redis.conf中的端口号为7001，并打开cluster-enabled。如法炮制总共6个redis文件夹。

（2）把创建集群的ruby脚本拷贝到redis-cluster下

（3）启动六个redis实例：可以创建一个脚本，startall.sh

```sh
  cd redis01
  ./redis-server redis-conf
  cd ..
  cd redis02
  ./redis-server redis-conf
  cd ..
  cd redis03
  ./redis-server redis-conf
  cd ..
  cd redis04
  ./redis-server redis-conf
  cd ..
  cd redis05
  ./redis-server redis-conf
  cd ..
  cd redis06
  ./redis-server redis-conf
  cd ..
```

改变脚本的运行权限：

```
chmod +x startall.sh
```

然后启动脚本：

```
./startall.sh
```

检查redis的启动情况：

```
ps aux|grep redis
```

（4）创建集群

执行命令：

```sh
 ./redis-trib.rb create --replicas 1 192.168.222.128:7001 192.168.222.128:7002 192.168.222.128:7003 192.168.222.128:7004 192.168.222.128:7005  192.168.222.128:7006
```

### 测试集群

```
redis01/redis-cli -h 192.168.222.128 -p 7002 -c
```

redis-cli表示redis客户端

### 关闭redis

单机版：在redis-cli中执行命令shutdown

集群：bin/redis-cli -p 7001 shutdown

可以写一个关闭脚本shutdown.sh

## redis客户端

### 自带客户端

redis-cli

### 图形化客户端

Redis Desktop Manager 只支持单机版，不支持集群；

redis有16个库，库的个数可以在redis-conf中设置；集群版只有一个库；

### Jedis客户端

（1）单机版

导入jedis客户端，使用maven加入redis客户端的坐标。

通过创建单实例jedis对象连接redis服务；

```java
//com.taotao.rest.jedis.TestJedisSingle

	public void testJedisSingle() {
		Jedis jedis=new Jedis("192.168.222.128", 6379);
		jedis.set("name","jack");
		String name=jedis.get("name");
		System.out.println(name);
		jedis.close();
	}
```

使用连接池连接

```java
//com.taotao.rest.jedis.TestJedisSingle

	public void pool() {
		JedisPool jedisPool=new JedisPool("192.168.222.128", 6379);
		Jedis jedis=jedisPool.getResource();
		jedis.set("student","mary");
		String name=jedis.get("student");
		System.out.println(name);
		jedis.close();
		jedisPool.close();
	}
```

（2）集群版

```java
//com.taotao.rest.jedis.TestJedisSingle

	public void testJedisCluster() {
		HashSet<HostAndPort> set=new HashSet<HostAndPort>();
		set.add(new HostAndPort("192.168.222.128", 7001));
		set.add(new HostAndPort("192.168.222.128", 7002));
		set.add(new HostAndPort("192.168.222.128", 7003));
		set.add(new HostAndPort("192.168.222.128", 7004));
		set.add(new HostAndPort("192.168.222.128", 7005));
		set.add(new HostAndPort("192.168.222.128", 7006));
		JedisCluster cluster=new JedisCluster(set);
		cluster.set("key1","2");
		String key1=cluster.get("key1");
		System.out.println(key1);
		cluster.close();
	}
```

## 业务逻辑中添加缓存

### 业务逻辑

接受到一个用户请求，先根据key（内容分类id）到redis中去查， 查得到直接返回，查不到再到mysql中寻找，放入redis中并返回结果；

需要在taotao-rest工程中添加缓存；

### Jedis整合spring

单机版整合：将JedisPool整合到spring里面；

bean的配置文件：

```xml
<!--applicationContext-jedis.xml-->

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
<bean id="redisClient" class="redis.clients.jedis.JedisPool">
	<constructor-arg name="host" value="192.168.222.128"></constructor-arg>
	<constructor-arg name="port" value="6379"></constructor-arg>
</bean>

</beans>
```

测试：

```java
//com.taotao.rest.jedis.TestJedisSingle

	public void testSpringSingle() {
		ApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring/applicationContext-jedis.xml");
		JedisPool jedisPool=(JedisPool) applicationContext.getBean("redisClient");
		Jedis jedis=jedisPool.getResource();
		jedis.set("spring","bingo");
		System.out.println(jedis.get("student"));
		jedis.close();
		jedisPool.close();
	}
```

