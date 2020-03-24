package com.xiaosong;


import cn.dreampie.quartz.QuartzPlugin;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log4jLogFactory;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.server.undertow.UndertowServer;
import com.jfinal.template.Engine;
import com.jfinal.template.source.ClassPathSourceFactory;
import com.xiaosong.common.server.ServerService;
import com.xiaosong.constant.Constant;
import com.xiaosong.interceptor.WebSocketHandler;
import com.xiaosong.interceptor.XiaoSongInterceptor;
import com.xiaosong.model._MappingKit;
import com.xiaosong.routes.GlobalRoutes;
import org.apache.log4j.Logger;

public class MainConfig extends JFinalConfig {
    private ServerService srv = ServerService.me;
    private static Logger logger = Logger.getLogger(MainConfig.class);

    private static Prop p = loadConfig();

    private static Prop loadConfig() {
        //PropKit 工具类用来操作外部配置文件。
        //PropKit 可以极度方便地在系统任意时空使用
        //生产模式时加载product文件，开发时加载develop文件，避免生产打包时文件漏改，只需改Constant中的dev_mode
        if (Constant.DEV_MODE) {
            return PropKit.use("db_develop.properties").append("config_develop.properties");
        } else {
            return PropKit.use("db_product.properties").append("config_product.properties");
        }

    }


    /**
     * 系统常量加载  属于全局配置
     * 其他系统参数可查看API中Constant定义常量
     */
    @Override
    public void configConstant(Constants me) {
        me.setDevMode(Constant.DEV_MODE);//是否开发模式 上生产是需要改变 与JFInal框架有关
        me.setMaxPostSize(1024 * 1024 * 20);//默认最大上传数据大小
        me.setLogFactory(new Log4jLogFactory());//日志配置
        me.setBaseUploadPath(Constant.BASE_UPLOAD_PATH);//文件上传路径
        me.setBaseDownloadPath(Constant.BASE_DOWNLOAD_PATH);//文件下载路径
    }

    /**
     * 系统控制器路劲配置
     * 具体查看API-Routes
     */
    @Override
    public void configRoute(Routes me) {
        // 此处还可以进行route级别的过滤器配置
        //routes级别拦截器在class级别拦截器之前被调用，具体配置在GlobalRoutes类中进行配置

        //业务层的过滤器可以在Service或Controller中使用Before标签加载，不使用过滤器是可以使用clear清除
        me.add(new GlobalRoutes());//所有的路由配置在GlobalRoutes配置
    }

    /**
     * 插件配置 常用插件配置有以下：
     * 1.DruidPlugin数据库插件
     * 2.ActiveRecordPlugin 数据库操作插件
     * 3.Redis以及Cache缓存插件
     */
    @Override
    public void configPlugin(Plugins me) {
        // 这里是在数据库的配置文件中读取数据库的一些信息
//		System.out.println(PropKit.get("password").trim());
        DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("jdbcUrl").trim(), PropKit.get("user").trim(), PropKit.get("password").trim());
//        // 这里是做映射
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setShowSql(true);
        arp.setShowSql(Constant.DEV_MODE);
        arp.setDialect(new MysqlDialect());
//		arp.setBaseSqlTemplatePath(PathKit.getRootClassPath());//该方法会把资源目录定义到test-class目录下
        arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        arp.addSqlTemplate("jxt.sql");//复杂sql写在这个文件中 以namespace和sqlname获取对应sql，sql条件可用质量写
        //获取sql可以使用dao.getSqlPara或Db.getSqlPara获取
        arp.getSqlKit().getEngine().addSharedObject("sk", new com.jfinal.kit.StrKit());//添加共享sql引擎
        //注意，如果有数据库的话，这里还是需要添加其他的配置的
        //数据库映射统一使用GeneratorModel生成，无需而外配置
        _MappingKit.mapping(arp);
//        //添加到插件列表中
        me.add(druidPlugin);
        me.add(arp);

        // 配置log插件
        Log4jFilter logFilter = new Log4jFilter();
        logFilter.setStatementLogEnabled(false);
        logFilter.setStatementLogErrorEnabled(true);
        logFilter.setStatementExecutableSqlLogEnable(true);
        druidPlugin.addFilter(logFilter);
//		// 添加到插件列表中
        me.add(druidPlugin);
        // 缓存
        me.add(new EhCachePlugin());
        // ORM
//		me.add(arp);

        //Redis 以及缓存Cache结合使用配置
        //后面使用Redis获取数据，接口CacheAPI进行各种操作
        String cacheType = PropKit.get("cache.type").trim();
        if ("redis".equals(cacheType)) {
//            RedisPlugin redisPlugin = new ESRedisPlugin().config();
            RedisPlugin redisPlugin = new RedisPlugin("xiaosong", "127.0.0.1", 6379/*,"123456"*/);
            me.add(redisPlugin);
        }
        //cron4j 定时任务
//        Cron4jPlugin cp = new Cron4jPlugin();
//        cp.addTask("* * * * *", new Runnable() {
//            @Override
//            public void run() {
//                System.out.println(new Date()+"....");
//            }
//        });
//        me.add(cp);

        //quartz定时任务
        QuartzPlugin quartz = new QuartzPlugin();
        quartz.setJobs("job.properties");
        me.add(quartz);
    }


    /**
     * 添加过滤器
     * 该地方添加普通过滤器以及全局过滤器还有，这两个过滤器只是针对控制层的
     */
    @Override
    public void configInterceptor(Interceptors me) {
        //1.me.add();全局控制层过滤器，功能与addGlobalActionInterceptor完全一样，为兼容老版本而保留
        //2.me.addGlobalActionInterceptor(globalActionInterceptor);//全局过滤器，针对项目所有控制器都会引用，针对某些不需要使用过滤器
        //的控制层时可以使用注解clear注解取消
        //3.me.addGlobalServiceInterceptor(globalServiceInterceptor);//该方法添加的是Service层的全局控制器
        me.addGlobalActionInterceptor(new XiaoSongInterceptor());
//        me.addGlobalActionInterceptor(new LoginInterceptor());
//        me.add(new LoginInterceptor())
    }


    /**
     * 目前暂时不知道这个由啥用
     * 源码说明：Handler可以接管所有web请求，并对应用拥有完全的控制权，可以很方便地实现更高层的功能性扩展。
     * 后续待完善
     */
    @Override
    public void configHandler(Handlers me) {
        //监控
        //DruidStatViewHandler dvh =  new DruidStatViewHandler("/druid");
        //me.add(dvh);
        me.add(new WebSocketHandler("^/websocket"));
    }


    //配置模板引擎 学习参考https://blog.csdn.net/zhiguoliu11/article/details/82724527
    //公共模板的使用
    @Override
    public void configEngine(Engine me) {
        me.setDevMode(Constant.DEV_MODE);
        me.addSharedObject("sk", new com.jfinal.kit.StrKit());
    }

    //项目启动后操作，常用场景可以加载一些定时任务JOB类可在此处加载启动
    @Override
    public void afterJFinalStart() {
//        TbServerinfo ser = srv.findSer();
//        String netappValue = ser.getNetappValue();
//        String toKen = ser.getToKen();
//        if (netappValue.equals("true")) {
//            try {
//                String s= "nohup ./natapp -authtoken="+toKen+" -log=stdout &";
//                System.out.println(s);
//                int i = Runtime.getRuntime().exec(s).waitFor();
//                    if (i == 1) {
//                    logger.info("内网穿透开启成功~");
//                } else {
//                    logger.error("内网穿透开启失败~");
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }

    public static void main(String[] args) {
//        //jfinal 3.3 启动
//        JFinal.start("src/main/webapp", 8080, "/");
        //jfinal 4.8 启动
        UndertowServer.start(MainConfig.class,8080,false);

    }

}
