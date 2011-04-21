package arena.form;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.action.RequestState;
import arena.action.ServerSideUserState;
import arena.utils.ReflectionUtils;


public class SessionFormPopulator<F> implements FormPopulator<F> {
    private final Log log = LogFactory.getLog(SessionFormPopulator.class);

    private Class<F> formClass;
    private Map<String, Object> defaultValues;
    private String formName = "form";
    private String requestCanaryName;
    private String[] variableLengthCollectionFields;
    private String[] jqueryArrayFields;
    private String[] checkboxCanaryFields;

    public SessionFormPopulator() {
    }

    public SessionFormPopulator(Class<F> formClass) {
        this();
        setFormClass(formClass);
    }

    @SuppressWarnings("unchecked")
    public F createPopulatedForm(RequestState state) throws Exception {
        ServerSideUserState session = state.getServerSideUserState();
        F form = (F) session.getAttribute(this.formName);
        
        if (form != null && !form.getClass().equals(this.formClass)) {
            log.info("WARNING: Invalid form class:" + form.getClass() + " expected: " + this.formClass);
            session.removeAttribute(this.formName);
            form = null;
        }
        
        // Check form canary value in request to see if we need a new form
        if (this.requestCanaryName != null && form != null) {
            String canary = state.getArg(this.requestCanaryName, "");
            String sessionCanary = (String) session.getAttribute(this.requestCanaryName);
            if (canary.equals("")) {
                log.info("WARNING: No request canary found, ignoring. session=" + sessionCanary);
            } else if (sessionCanary == null || sessionCanary.equals("")) {
                log.info("WARNING: No session canary found, ignoring. request=" + canary);
            } else if (sessionCanary.equals(canary)) {
                log.info("Matching canary detected, continuing");
            } else {
                log.info("Mismatched canary values for session form: " + 
                        this.formName + ", dropping session form. Session=" + 
                        sessionCanary + " request=" + canary);
                session.removeAttribute(this.formName);
                form = null;
            }
        }
        
        if (form == null) {
            form = initializeForm(state);
        }
        prePopulateForm(state, form);
        
        for (Iterator<String> i = state.getArgNames(); i.hasNext(); ) {
            String name = i.next();
            String formVarName = name;
            Object value = null;

            if (this.variableLengthCollectionFields != null && name.endsWith("Count") &&
                    Arrays.binarySearch(this.variableLengthCollectionFields, name.substring(0, name.length() - 5)) >= 0) {
                formVarName = name.substring(0, name.length() - 5);
                String count = state.getArg(name, "");
                if (count.equals("")) {
                    continue;
                }
                try {
                    int countInt = Integer.parseInt(count);
                    if (countInt == 1) {
                        Object rv = state.getArg(formVarName + "0");
                        if (rv != null) {
                            value = rv;
                        }
                    } else {
                        String[] rvs = new String[countInt];
                        for (int n = 0; n < countInt; n++) {
                            Object rv = state.getArg(formVarName + n);
                            if (rv != null) {
                                rvs[n] = rv.toString();
                            }
                        }
                        value = rvs;
                    }
                } catch (Throwable err) {
                    log.warn("Error parsing parameter " + formVarName + " count=" + count, err);
                }
            } else if (this.jqueryArrayFields != null && name.endsWith("[]") &&
                    Arrays.binarySearch(this.jqueryArrayFields, name.substring(0, name.length() - 2)) >= 0) {
                formVarName = name.substring(0, name.length() - 2);
                Object rv = state.getArg(name);
                if (rv != null) {
                    value = rv;
                }
            } else if (this.checkboxCanaryFields != null && name.endsWith("CBCanary") &&
                    Arrays.binarySearch(this.checkboxCanaryFields, name.substring(0, name.length() - 8)) >= 0) {
                formVarName = name.substring(0, name.length() - 8);
                Object suppliedValue = state.getArg(formVarName);
                Object canaryValue = state.getArg(name);
                if (suppliedValue == null) {
                    log.trace("Found canary value for " + formVarName + " - null found, substituting canary value " + canaryValue);
                    value = canaryValue;
                } else {
                    log.trace("Found canary value for " + formVarName + " - supplied value " + suppliedValue + " used, canary not required");
                }
            } else {
                Object rv = state.getArg(name);
                if (rv != null) {
                    value = rv;
                }
            }

            // Overwrite with the request value
            if (value != null) {
                setElement(form, formVarName, value);
            }
        }
        return form;
    }
    
    protected F initializeForm(RequestState state) throws Exception {
        ServerSideUserState session = state.getServerSideUserState();

        log.info("Creating new form instance: " + this.formClass + " for session form: " + this.formName);
        F form = this.formClass.newInstance();
        session.setAttribute(this.formName, form);
        applyDefaultsToForm(form);
        return form;
    }
    protected F prePopulateForm(RequestState state, F form) throws Exception {
        return form;
    }
    
    protected void applyDefaultsToForm(F form) {
        if (this.defaultValues != null) {
            String[] names = ReflectionUtils.getAttributeNamesUsingGetter(this.formClass);
            for (String name : names) {
                Object value = this.defaultValues.get(name);
                if (value != null) {
                    setElement(form, name, value);
                }
            }
        }
    }

    public void releaseForm(RequestState state, F form) throws Exception {
        ServerSideUserState session = state.getServerSideUserState();
        session.removeAttribute(this.formName);
        if (this.requestCanaryName != null) {
            session.removeAttribute(this.requestCanaryName);
        }
    }

    protected void setElement(F form, String name, Object value) {
        log.debug("Attempting type for " + form.getClass().toString() + "." + name);
        Class<?> type = null;
        try {
            type = ReflectionUtils.getAttributeTypeUsingGetter(name, form.getClass());
        } catch (Throwable err) {
            log.trace("Skipping " + form.getClass().toString() + "." + name, err);
        }
        if (type != null) {
            if (type.isArray() && type.getComponentType().isAssignableFrom(value.getClass())) {
                Object arr = Array.newInstance(type.getComponentType(), 1);
                Array.set(arr, 0, value);
                ReflectionUtils.setAttributeUsingSetter(name, form, arr);
            } else {
                ReflectionUtils.setAttributeUsingSetter(name, form, value);
            }
        }
    }

    public void setFormClass(Class<F> formClass) {
        this.formClass = formClass;
    }

    @SuppressWarnings("unchecked")
    public void setFormClassName(String formClassName) throws Exception {
        setFormClass((Class<F>) Class.forName(formClassName));
    }

    public void setDefaultValues(Map<String, Object> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public void setRequestCanaryName(String requestCanaryName) {
        this.requestCanaryName = requestCanaryName;
    }

    public void setVariableLengthCollectionFields(String[] variableLengthCollectionFields) {
        this.variableLengthCollectionFields = variableLengthCollectionFields;
        if (this.variableLengthCollectionFields != null) {
            Arrays.sort(this.variableLengthCollectionFields);
        }
    }

    public void setJqueryArrayFields(String[] jqueryArrayFields) {
        this.jqueryArrayFields = jqueryArrayFields;
        if (this.jqueryArrayFields != null) {
            Arrays.sort(this.jqueryArrayFields);
        }
    }

    public void setCheckboxCanaryFields(String[] cbCanaryFields) {
        this.checkboxCanaryFields = cbCanaryFields;
        if (this.checkboxCanaryFields != null) {
            Arrays.sort(this.checkboxCanaryFields);
        }
    }

    public Class<F> getFormClass() {
        return formClass;
    }

    public Map<String, Object> getDefaultValues() {
        return defaultValues;
    }

    public String getFormName() {
        return formName;
    }

    public String getRequestCanaryName() {
        return requestCanaryName;
    }

    public String[] getVariableLengthCollectionFields() {
        return variableLengthCollectionFields;
    }

    public String[] getJqueryArrayFields() {
        return jqueryArrayFields;
    }
}
