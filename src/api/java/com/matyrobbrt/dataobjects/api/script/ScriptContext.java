package com.matyrobbrt.dataobjects.api.script;

import java.util.Map;

/**
 * Represents a context used for scripting.
 */
public interface ScriptContext {
    /**
     * Gets the string representation of the script.
     *
     * @return the string representation of the script
     */
    String getScript();

    /**
     * Adds a variable to this context.
     *
     * @param name the name of the variable
     * @param var  the variable to add
     */
    void addVariable(String name, Object var);

    /**
     * Gets all the variables in this context.
     *
     * @return all the variables in this context
     */
    Map<String, Object> getVariables();
}
