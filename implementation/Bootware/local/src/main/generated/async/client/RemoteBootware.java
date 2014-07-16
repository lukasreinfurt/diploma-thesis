
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
import org.simtech.bootware.remote.DeployResponse;
import org.simtech.bootware.remote.SetConfigurationResponse;
import org.simtech.bootware.remote.ShutdownResponse;
import org.simtech.bootware.remote.UndeployResponse;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.0
 * 
 */
@WebService(name = "RemoteBootware", targetNamespace = "http://remote.bootware.simtech.org/")
public interface RemoteBootware {


    /**
     * 
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.remote.ShutdownResponse>
     */
    @WebMethod(operationName = "shutdown")
    @RequestWrapper(localName = "shutdown", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Shutdown")
    @ResponseWrapper(localName = "shutdownResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.ShutdownResponse")
    public Response<ShutdownResponse> shutdownAsync();

    /**
     * 
     * @param asyncHandler
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "shutdown")
    @RequestWrapper(localName = "shutdown", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Shutdown")
    @ResponseWrapper(localName = "shutdownResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.ShutdownResponse")
    public Future<?> shutdownAsync(
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<ShutdownResponse> asyncHandler);

    /**
     * 
     * @throws ShutdownException
     */
    @WebMethod
    @RequestWrapper(localName = "shutdown", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Shutdown")
    @ResponseWrapper(localName = "shutdownResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.ShutdownResponse")
    public void shutdown()
        throws ShutdownException
    ;

    /**
     * 
     * @param configurationList
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.remote.SetConfigurationResponse>
     */
    @WebMethod(operationName = "setConfiguration")
    @RequestWrapper(localName = "setConfiguration", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.SetConfiguration")
    @ResponseWrapper(localName = "setConfigurationResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.SetConfigurationResponse")
    public Response<SetConfigurationResponse> setConfigurationAsync(
        @WebParam(name = "configurationList", targetNamespace = "")
        org.simtech.bootware.remote.SetConfiguration.ConfigurationList configurationList);

    /**
     * 
     * @param asyncHandler
     * @param configurationList
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "setConfiguration")
    @RequestWrapper(localName = "setConfiguration", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.SetConfiguration")
    @ResponseWrapper(localName = "setConfigurationResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.SetConfigurationResponse")
    public Future<?> setConfigurationAsync(
        @WebParam(name = "configurationList", targetNamespace = "")
        org.simtech.bootware.remote.SetConfiguration.ConfigurationList configurationList,
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<SetConfigurationResponse> asyncHandler);

    /**
     * 
     * @param configurationList
     * @throws SetConfigurationException
     */
    @WebMethod
    @RequestWrapper(localName = "setConfiguration", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.SetConfiguration")
    @ResponseWrapper(localName = "setConfigurationResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.SetConfigurationResponse")
    public void setConfiguration(
        @WebParam(name = "configurationList", targetNamespace = "")
        org.simtech.bootware.remote.SetConfiguration.ConfigurationList configurationList)
        throws SetConfigurationException
    ;

    /**
     * 
     * @param context
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.remote.DeployResponse>
     */
    @WebMethod(operationName = "deploy")
    @RequestWrapper(localName = "deploy", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Deploy")
    @ResponseWrapper(localName = "deployResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.DeployResponse")
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
    @RequestWrapper(localName = "deploy", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Deploy")
    @ResponseWrapper(localName = "deployResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.DeployResponse")
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
    @RequestWrapper(localName = "deploy", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Deploy")
    @ResponseWrapper(localName = "deployResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.DeployResponse")
    public InformationListWrapper deploy(
        @WebParam(name = "context", targetNamespace = "")
        Context context)
        throws DeployException
    ;

    /**
     * 
     * @param informationList
     * @return
     *     returns javax.xml.ws.Response<org.simtech.bootware.remote.UndeployResponse>
     */
    @WebMethod(operationName = "undeploy")
    @RequestWrapper(localName = "undeploy", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Undeploy")
    @ResponseWrapper(localName = "undeployResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.UndeployResponse")
    public Response<UndeployResponse> undeployAsync(
        @WebParam(name = "informationList", targetNamespace = "")
        org.simtech.bootware.remote.Undeploy.InformationList informationList);

    /**
     * 
     * @param asyncHandler
     * @param informationList
     * @return
     *     returns java.util.concurrent.Future<? extends java.lang.Object>
     */
    @WebMethod(operationName = "undeploy")
    @RequestWrapper(localName = "undeploy", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Undeploy")
    @ResponseWrapper(localName = "undeployResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.UndeployResponse")
    public Future<?> undeployAsync(
        @WebParam(name = "informationList", targetNamespace = "")
        org.simtech.bootware.remote.Undeploy.InformationList informationList,
        @WebParam(name = "asyncHandler", targetNamespace = "")
        AsyncHandler<UndeployResponse> asyncHandler);

    /**
     * 
     * @param informationList
     * @throws UndeployException
     */
    @WebMethod
    @RequestWrapper(localName = "undeploy", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.Undeploy")
    @ResponseWrapper(localName = "undeployResponse", targetNamespace = "http://remote.bootware.simtech.org/", className = "org.simtech.bootware.remote.UndeployResponse")
    public void undeploy(
        @WebParam(name = "informationList", targetNamespace = "")
        org.simtech.bootware.remote.Undeploy.InformationList informationList)
        throws UndeployException
    ;

}