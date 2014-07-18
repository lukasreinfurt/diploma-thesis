
package async.client;

import java.util.concurrent.Future;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.Response;
import javax.xml.ws.ResponseWrapper;
import org.simtech.bootware.core.Context;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.local.DeployResponse;
import org.simtech.bootware.local.SetConfigurationResponse;
import org.simtech.bootware.local.ShutdownResponse;
import org.simtech.bootware.local.UndeployResponse;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.0
 * 
 */
@WebService(name = "LocalBootware", targetNamespace = "http://local.bootware.simtech.org/")
public interface LocalBootware {


    /**
     * 
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.local.ShutdownResponse>
     */
    @WebMethod(operationName = "shutdown")
    @RequestWrapper(localName = "shutdown", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Shutdown")
    @ResponseWrapper(localName = "shutdownResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.ShutdownResponse")
    public Response<ShutdownResponse> shutdownAsync();

    /**
     * 
     * @param asyncHandler
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "shutdown")
    @RequestWrapper(localName = "shutdown", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Shutdown")
    @ResponseWrapper(localName = "shutdownResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.ShutdownResponse")
    public Future<?> shutdownAsync(
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<ShutdownResponse> asyncHandler);

    /**
     * 
     * @throws ShutdownException
     */
    @WebMethod
    @RequestWrapper(localName = "shutdown", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Shutdown")
    @ResponseWrapper(localName = "shutdownResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.ShutdownResponse")
    public void shutdown()
        throws ShutdownException
    ;

    /**
     * 
     * @param context
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.local.DeployResponse>
     */
    @WebMethod(operationName = "deploy")
    @RequestWrapper(localName = "deploy", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Deploy")
    @ResponseWrapper(localName = "deployResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.DeployResponse")
    public Response<DeployResponse> deployAsync(
        @WebParam(name = "context", targetNamespace = "")
        Context context);

    /**
     * 
     * @param context
     * @param asyncHandler
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "deploy")
    @RequestWrapper(localName = "deploy", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Deploy")
    @ResponseWrapper(localName = "deployResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.DeployResponse")
    public Future<?> deployAsync(
        @WebParam(name = "context", targetNamespace = "")
        Context context,
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<DeployResponse> asyncHandler);

    /**
     * 
     * @param context
     * @return
     *     returns org.simtech.bootware.core.InformationListWrapper
     * @throws DeployException
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "deploy", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Deploy")
    @ResponseWrapper(localName = "deployResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.DeployResponse")
    public InformationListWrapper deploy(
        @WebParam(name = "context", targetNamespace = "")
        Context context)
        throws DeployException
    ;

    /**
     * 
     * @param informationList
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.local.UndeployResponse>
     */
    @WebMethod(operationName = "undeploy")
    @RequestWrapper(localName = "undeploy", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Undeploy")
    @ResponseWrapper(localName = "undeployResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.UndeployResponse")
    public Response<UndeployResponse> undeployAsync(
        @WebParam(name = "informationList", targetNamespace = "")
        org.simtech.bootware.local.Undeploy.InformationList informationList);

    /**
     * 
     * @param asyncHandler
     * @param informationList
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "undeploy")
    @RequestWrapper(localName = "undeploy", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Undeploy")
    @ResponseWrapper(localName = "undeployResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.UndeployResponse")
    public Future<?> undeployAsync(
        @WebParam(name = "informationList", targetNamespace = "")
        org.simtech.bootware.local.Undeploy.InformationList informationList,
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<UndeployResponse> asyncHandler);

    /**
     * 
     * @param informationList
     * @throws UndeployException
     */
    @WebMethod
    @RequestWrapper(localName = "undeploy", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.Undeploy")
    @ResponseWrapper(localName = "undeployResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.UndeployResponse")
    public void undeploy(
        @WebParam(name = "informationList", targetNamespace = "")
        org.simtech.bootware.local.Undeploy.InformationList informationList)
        throws UndeployException
    ;

    /**
     * 
     * @param configurationList
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.local.SetConfigurationResponse>
     */
    @WebMethod(operationName = "setConfiguration")
    @RequestWrapper(localName = "setConfiguration", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.SetConfiguration")
    @ResponseWrapper(localName = "setConfigurationResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.SetConfigurationResponse")
    public Response<SetConfigurationResponse> setConfigurationAsync(
        @WebParam(name = "configurationList", targetNamespace = "")
        org.simtech.bootware.local.SetConfiguration.ConfigurationList configurationList);

    /**
     * 
     * @param asyncHandler
     * @param configurationList
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "setConfiguration")
    @RequestWrapper(localName = "setConfiguration", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.SetConfiguration")
    @ResponseWrapper(localName = "setConfigurationResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.SetConfigurationResponse")
    public Future<?> setConfigurationAsync(
        @WebParam(name = "configurationList", targetNamespace = "")
        org.simtech.bootware.local.SetConfiguration.ConfigurationList configurationList,
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<SetConfigurationResponse> asyncHandler);

    /**
     * 
     * @param configurationList
     * @throws SetConfigurationException
     */
    @WebMethod
    @RequestWrapper(localName = "setConfiguration", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.SetConfiguration")
    @ResponseWrapper(localName = "setConfigurationResponse", targetNamespace = "http://local.bootware.simtech.org/", className = "org.simtech.bootware.local.SetConfigurationResponse")
    public void setConfiguration(
        @WebParam(name = "configurationList", targetNamespace = "")
        org.simtech.bootware.local.SetConfiguration.ConfigurationList configurationList)
        throws SetConfigurationException
    ;

}
