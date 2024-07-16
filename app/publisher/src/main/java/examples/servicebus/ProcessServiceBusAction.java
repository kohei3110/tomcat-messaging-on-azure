package examples.servicebus;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

public class ProcessServiceBusAction extends Action {

    // ------------------------------------------------------------ Constructors

    /**
     * Constructor for ProcessFormAction.
     */
    public ProcessServiceBusAction() {
        super();
    }

    // ---------------------------------------------------------- Action Methods
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response)
        throws Exception {

        ServiceBusForm serviceBusForm = (ServiceBusForm) form;
        String name = serviceBusForm.getName();

        TokenCredential credential = new ClientSecretCredentialBuilder()
            .clientId(System.getenv("AZURE_CLIENT_ID"))
            .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
            .tenantId(System.getenv("AZURE_TENANT_ID"))
            .build();
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .credential(System.getenv("SERVICE_BUS_FULLY_QUALIFIED_NAMESPACE"), credential)
            .sender()
            .queueName("lower-case")
            .buildClient();
        
        sender.sendMessage(new ServiceBusMessage(name));
        System.out.println("Sent a single message to the queue: " + name);
        sender.close();
        
        return mapping.findForward("success");
    }    
}
