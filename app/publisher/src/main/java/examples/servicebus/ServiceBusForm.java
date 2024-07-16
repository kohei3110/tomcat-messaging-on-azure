package examples.servicebus;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ServiceBusForm extends ActionForm {

    // ------------------------------------------------------ Instance Variables

    /** Name */
    private String name = null;

    // ------------------------------------------------------------ Constructors

    /**
     * Constructor for MultiboxActionForm.
     */
    public ServiceBusForm() {
        super();
    }

    // ---------------------------------------------------------- Public Methods
    public void reset(ActionMapping mapping, HttpServletRequest request) {

        this.name = null;
    }

    public ActionErrors validate(
        ActionMapping mapping,
        HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        // Name must be entered
        if ((name == null) || (name.length() < 1)) {
            errors.add("name", new ActionMessage("errors.name.required"));
        }

        return (errors);

    }

    // -------------------------------------------------------------- Properties
    /**
     * Returns the name.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}