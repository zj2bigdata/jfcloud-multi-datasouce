# 引入并配置
```
    <dependency>
        <groupId>com.github</groupId>
        <artifactId>jfcloud-multi-datasouce</artifactId>
        <version>kxxx</version>
    </dependency>
```
```
yml配置
# 租户表维护
jfcloud:
  tenant:
    #租户数据库前缀，根据租户形成 jfcloud_sims_sample_1、jfcloud_sims_sample_2 ...
    dynamic:
      dbs: jfcloud_sims_sample,jfcloud_sims_warning
        #host
      dbhost: jfcloud-v4-mysql-sims
       #默认不带前缀租户
      defaultid: 1

  //配置默认数据源，匹配不到数据库则使用默认数据源

   @Configuration
   public class DefaultDatasourceConfig {
   
       @Bean("defaultSource")
        @Primary
       @ConfigurationProperties(prefix = "spring.datasource.druid")
       public DataSource master() {
           return new DruidDataSource();
       }
   }

//2.配置动态数据源

@EnableTransactionManagement
@Configuration
public class MybatisSqlSessionFactoryBeanAutoConfiguration {

    @Bean
    public MybatisSqlSessionFactoryBean sqlSessionFactoryBean(DynamicDataSource dynamicDataSource,MybatisPlusProperties mybatisPlusProperties) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        if (ArrayUtil.isNotEmpty(mybatisPlusProperties.resolveMapperLocations())) {
            sessionFactory.setMapperLocations(mybatisPlusProperties.resolveMapperLocations());
        }
        if (mybatisPlusProperties.getTypeAliasesPackage() != null) {
            sessionFactory.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());
        }
        //配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource作为数据源则不能实现切换
        sessionFactory.setDataSource(dynamicDataSource);
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DynamicDataSource dynamicDataSource) {
        // 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
        return new DataSourceTransactionManager(dynamicDataSource);
    }

//3.配置upms的权限插件


@AutoConfigureAfter(MybatisPlusInterceptor.class)
@Configuration
public class MybatisInterceptorAutoConfiguration implements InitializingBean {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;
    @Autowired
    MybatisPlusInterceptor mybatisPlusInterceptor;

    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        //创建自定义mybatis拦截器，添加到chain的最后面
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
            //自己添加
            configuration.addInterceptor(mybatisPlusInterceptor);
        }
    }

}
```
# 接口使用
####1. 方法参数，优于注解
```
   //方法使用，优于注解
    void handleExcelImportData(MethodTenantParam methodTenantParam,
                    List<ArchiveInfo> archiveInfos, Map<String, Object> param);
```
####2. 注解
```
@RestController
@RequestMapping("/sample")
@AutowiredDataSource
public class SampleController {

    @Autowired
    private SampleService sampleService;

    @ChangeWeb
    @GetMapping("/info")
    @AutowiredDataSource(tenandId = "1")
    public CustomResult<Sample> getInfo() {
        List<Sample> userList = sampleService.list();
        return CustomResult.Ok(userList);
    }
}

curl --request GET 'localhost:8080/sample/info' --header 'TENANT_ID: 2' 
```