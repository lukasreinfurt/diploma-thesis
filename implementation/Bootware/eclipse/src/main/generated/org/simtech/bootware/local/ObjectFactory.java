
package org.simtech.bootware.local;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;
import org.simtech.bootware.core.exceptions.UndeployException;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.simtech.bootware.local package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DeployException_QNAME = new QName("http://local.bootware.simtech.org/", "DeployException");
    private final static QName _Shutdown_QNAME = new QName("http://local.bootware.simtech.org/", "shutdown");
    private final static QName _Deploy_QNAME = new QName("http://local.bootware.simtech.org/", "deploy");
    private final static QName _UndeployException_QNAME = new QName("http://local.bootware.simtech.org/", "UndeployException");
    private final static QName _SetConfigurationResponse_QNAME = new QName("http://local.bootware.simtech.org/", "setConfigurationResponse");
    private final static QName _ShutdownResponse_QNAME = new QName("http://local.bootware.simtech.org/", "shutdownResponse");
    private final static QName _Undeploy_QNAME = new QName("http://local.bootware.simtech.org/", "undeploy");
    private final static QName _UndeployResponse_QNAME = new QName("http://local.bootware.simtech.org/", "undeployResponse");
    private final static QName _SetConfigurationException_QNAME = new QName("http://local.bootware.simtech.org/", "SetConfigurationException");
    private final static QName _DeployResponse_QNAME = new QName("http://local.bootware.simtech.org/", "deployResponse");
    private final static QName _SetConfiguration_QNAME = new QName("http://local.bootware.simtech.org/", "setConfiguration");
    private final static QName _ShutdownException_QNAME = new QName("http://local.bootware.simtech.org/", "ShutdownException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.simtech.bootware.local
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Undeploy }
     * 
     */
    public Undeploy createUndeploy() {
        return new Undeploy();
    }

    /**
     * Create an instance of {@link Undeploy.InformationList }
     * 
     */
    public Undeploy.InformationList createUndeployInformationList() {
        return new Undeploy.InformationList();
    }

    /**
     * Create an instance of {@link SetConfiguration }
     * 
     */
    public SetConfiguration createSetConfiguration() {
        return new SetConfiguration();
    }

    /**
     * Create an instance of {@link SetConfiguration.ConfigurationList }
     * 
     */
    public SetConfiguration.ConfigurationList createSetConfigurationConfigurationList() {
        return new SetConfiguration.ConfigurationList();
    }

    /**
     * Create an instance of {@link DeployResponse }
     * 
     */
    public DeployResponse createDeployResponse() {
        return new DeployResponse();
    }

    /**
     * Create an instance of {@link UndeployResponse }
     * 
     */
    public UndeployResponse createUndeployResponse() {
        return new UndeployResponse();
    }

    /**
     * Create an instance of {@link ShutdownResponse }
     * 
     */
    public ShutdownResponse createShutdownResponse() {
        return new ShutdownResponse();
    }

    /**
     * Create an instance of {@link SetConfigurationResponse }
     * 
     */
    public SetConfigurationResponse createSetConfigurationResponse() {
        return new SetConfigurationResponse();
    }

    /**
     * Create an instance of {@link Deploy }
     * 
     */
    public Deploy createDeploy() {
        return new Deploy();
    }

    /**
     * Create an instance of {@link Shutdown }
     * 
     */
    public Shutdown createShutdown() {
        return new Shutdown();
    }

    /**
     * Create an instance of {@link Undeploy.InformationList.Entry }
     * 
     */
    public Undeploy.InformationList.Entry createUndeployInformationListEntry() {
        return new Undeploy.InformationList.Entry();
    }

    /**
     * Create an instance of {@link SetConfiguration.ConfigurationList.Entry }
     * 
     */
    public SetConfiguration.ConfigurationList.Entry createSetConfigurationConfigurationListEntry() {
        return new SetConfiguration.ConfigurationList.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeployException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "DeployException")
    public JAXBElement<DeployException> createDeployException(DeployException value) {
        return new JAXBElement<DeployException>(_DeployException_QNAME, DeployException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Shutdown }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "shutdown")
    public JAXBElement<Shutdown> createShutdown(Shutdown value) {
        return new JAXBElement<Shutdown>(_Shutdown_QNAME, Shutdown.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Deploy }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "deploy")
    public JAXBElement<Deploy> createDeploy(Deploy value) {
        return new JAXBElement<Deploy>(_Deploy_QNAME, Deploy.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UndeployException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "UndeployException")
    public JAXBElement<UndeployException> createUndeployException(UndeployException value) {
        return new JAXBElement<UndeployException>(_UndeployException_QNAME, UndeployException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetConfigurationResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "setConfigurationResponse")
    public JAXBElement<SetConfigurationResponse> createSetConfigurationResponse(SetConfigurationResponse value) {
        return new JAXBElement<SetConfigurationResponse>(_SetConfigurationResponse_QNAME, SetConfigurationResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ShutdownResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "shutdownResponse")
    public JAXBElement<ShutdownResponse> createShutdownResponse(ShutdownResponse value) {
        return new JAXBElement<ShutdownResponse>(_ShutdownResponse_QNAME, ShutdownResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Undeploy }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "undeploy")
    public JAXBElement<Undeploy> createUndeploy(Undeploy value) {
        return new JAXBElement<Undeploy>(_Undeploy_QNAME, Undeploy.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UndeployResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "undeployResponse")
    public JAXBElement<UndeployResponse> createUndeployResponse(UndeployResponse value) {
        return new JAXBElement<UndeployResponse>(_UndeployResponse_QNAME, UndeployResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetConfigurationException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "SetConfigurationException")
    public JAXBElement<SetConfigurationException> createSetConfigurationException(SetConfigurationException value) {
        return new JAXBElement<SetConfigurationException>(_SetConfigurationException_QNAME, SetConfigurationException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeployResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "deployResponse")
    public JAXBElement<DeployResponse> createDeployResponse(DeployResponse value) {
        return new JAXBElement<DeployResponse>(_DeployResponse_QNAME, DeployResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetConfiguration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "setConfiguration")
    public JAXBElement<SetConfiguration> createSetConfiguration(SetConfiguration value) {
        return new JAXBElement<SetConfiguration>(_SetConfiguration_QNAME, SetConfiguration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ShutdownException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://local.bootware.simtech.org/", name = "ShutdownException")
    public JAXBElement<ShutdownException> createShutdownException(ShutdownException value) {
        return new JAXBElement<ShutdownException>(_ShutdownException_QNAME, ShutdownException.class, null, value);
    }

}
