# Introduction

The general web error(we) handler abstraction based on Spring MVC Framework. 

# Why & How

As we know, the Restful API is the  standard for web development, 
Spring MVC which is the powerful framework for REST API development but the default exception handler is not good enough . 
Mostly we expect the unified error format in json if any exception thrown by the web application based on Spring MVC framework.

For example :

    {
        errorCode: "400",
        errorMessage: "Bad Request",
        developerMessage: "[org.springframework.web.method.annotation.ModelAttributeMethodProcessor.resolveArgument(ModelAttributeMethodProcessor.java:114), org.springframework.web.method.support.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:77), org.springframework.web.method.support.InvocableHandlerMethod.getMethodArgumentValues(InvocableHandlerMethod.java:162), org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:129), org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:110), org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandleMethod(RequestMappingHandlerAdapter.java:777), org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:706), org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85), org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:943), org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:877), org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:966), org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:857), javax.servlet.http.HttpServlet.service(HttpServlet.java:618), org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:842), javax.servlet.http.HttpServlet.service(HttpServlet.java:725), org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:291), org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206), org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:88), org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107), org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:239), org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206), org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:77), org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107), org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:239), org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206), org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:219), org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:106), org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:501), org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:142), org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:79), org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:88), org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:537), org.apache.coyote.http11.AbstractHttp11Processor.process(AbstractHttp11Processor.java:1085), org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:658), org.apache.coyote.http11.Http11NioProtocol$Http11ConnectionHandler.process(Http11NioProtocol.java:222), org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1556), org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.run(NioEndpoint.java:1513), java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142), java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617), org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61), java.lang.Thread.run(Thread.java:745)]",
        moreInfo: "org.springframework.validation.BeanPropertyBindingResult: 1 errors
        Field error in object 'foo' on field 'name': rejected value [null]; codes [NotNull.foo.name,NotNull.name,NotNull.java.lang.String,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [foo.name,name]; arguments []; default message [name]]; default message [may not be null]",
        formError: [
            {
                field: "name",
                message: "may not be null"
            }
        ]
    }

The `errorCode` indicates that the client can determine which error flow will be triggered based on the returned errorCode.

The `errorMessage` supplies what message may be prompted to the end user.

And the `developerMessage` provides more information to developer which can help to locate what caused the exception, the `moreInfo` shows help message as the supplement for developerMessage.

If the `@Valid` is enabled in Spring MVC configuration ,the formError can tell the detail of what happened , the client can parse the formError (in json format) to render the html form error.

Here is the reason we override the `org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver` to send unified error response
if any exception thrown by the `@Controller` from Spring MVC framework.


# Dependencies

* Spring Framework Web 4.x

# Usage

So far `1.1.0` is available 

## Maven

    <dependency>
        <groupId>in.clouthink.daas</groupId>
        <artifactId>daas-we</artifactId>
        <version>${daas.we.version}</version>
    </dependency>

## Gradle

    compile "in.clouthink.daas:daas-we:${daas_we_version}"


## Spring Configuration

How to enable the new Rest Response Exception Handler implementation.

    private List<HttpMessageConverter<?>> converters;
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        this.converters = converters;
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        CustomExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver = new CustomExceptionHandlerExceptionResolver(true);
        exceptionHandlerExceptionResolver.setMessageConverters(converters);
        exceptionHandlerExceptionResolver.setContentNegotiationManager(new ContentNegotiationManager());
	    exceptionHandlerExceptionResolver.afterPropertiesSet();
        exceptionResolvers.add(exceptionHandlerExceptionResolver);
    }
    
    
How to run the daas we sample :
    
    cd module/daas-we-sample
    mvn clean spring-boot:run
    
Follow the sample `in.clouthink.daas.we.sample.web.SampleRestApi` in module/daas-we-sample.
